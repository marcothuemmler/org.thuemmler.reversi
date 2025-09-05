package reversi.websocket

import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import reversi.controller.dto.MoveRequest
import reversi.controller.dto.NewGameRequest
import reversi.model.CellState
import reversi.model.Game
import reversi.model.PlayerType
import reversi.service.GameService
import reversi.util.MoveCommand
import reversi.util.UndoManager
import reversi.websocket.dto.*
import java.util.*
import java.util.concurrent.ConcurrentMap

@Component
class GameWebSocketHandler(
    private val gameService: GameService,
    private val undoManagers: ConcurrentMap<String, UndoManager>,
    private val sessions: SessionRegistry,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : TextWebSocketHandler() {

    companion object {
        private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }
        private val logger = LoggerFactory.getLogger(GameWebSocketHandler::class.java.name)
    }

    init {
        gameService.subscribe(::broadcastGameUpdate)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        scope.launch {
            try {
                when (val event = json.decodeFromString<ClientMessage>(message.payload)) {
                    is CreateGame -> createGame(session, event.payload)
                    is MakeMove -> makeMove(session, event.gameId, event.payload)
                    is Undo -> undoMove(event.gameId)
                    is Redo -> redoMove(event.gameId)
                    is Join -> joinGame(session, event.gameId)
                }
            } catch (e: Exception) {
                logger.error("Failed to handle message from session=${session.id}: ${e.message}", e)
                sendError(session, "Failed to handle message: ${e.message}")
            }
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val gameId = sessions.remove(session) ?: return
        logger.info("Session closed: session=${session.id}, gameId=$gameId, status=$status")

        if (!sessions.hasOpenSessions(gameId)) {
            undoManagers.remove(gameId)
            gameService.removeGame(gameId)
        }
    }

    private suspend fun createGame(session: WebSocketSession, newGame: NewGameRequest) {
        val id = newGame.id ?: UUID.randomUUID().toString()
        getUndoManager(id)

        val humanSides = newGame.playerTypes.filter { it.value == PlayerType.HUMAN }.keys
        val assignedSide = newGame.preferredSide?.takeIf { it in humanSides } ?: humanSides.first()
        sessions.register(session, id, assignedSide)

        logger.info("Game created: gameId=$id, session=${session.id}, side=$assignedSide")

        gameService.createGame(newGame.copy(id = id))
    }

    private fun joinGame(session: WebSocketSession, gameId: String) {
        gameService.getGame(gameId)?.let { game ->

            val assignedSide = assignSide(game) ?: return sendError(session, "All sides taken")

            sessions.register(session, gameId, assignedSide)

            logger.info("Player joined: gameId=$gameId, session=${session.id}, side=$assignedSide")

            val payload = GameUpdate.fromGame(game)
            sendServerMessage(session, MessageType.JOIN, gameId, payload)
        } ?: sendError(session, "Game not found")
    }

    @Synchronized
    private fun assignSide(game: Game): CellState? {
        val humanSides = game.playerTypes.filterValues { it == PlayerType.HUMAN }.keys
        val takenSides = sessions.openSessionsForGame(game.id).mapNotNull { sessions.assignedSide(it) }.toSet()
        return humanSides.firstOrNull { it !in takenSides }
    }

    private suspend fun makeMove(session: WebSocketSession, gameId: String, move: MoveRequest) {
        val game = gameService.getGame(gameId) ?: run {
            logger.warn("Move rejected: game not found, session=${session.id}, gameId=$gameId")
            return sendError(session, "Game not found")
        }

        val side = sessions.assignedSide(session)
        if (side != game.currentPlayer) {
            logger.warn("Move rejected: out of turn, session=${session.id}, side=$side, currentPlayer=${game.currentPlayer}")
            return sendError(session, "It's not your turn")
        }

        val (row, col) = move
        val command = MoveCommand(gameService, gameId, row, col)
        logger.debug("Move made: gameId={}, session={}, side={}, row={}, col={}", gameId, session.id, side, row, col)
        getUndoManager(gameId).doStep(command)
    }

    private fun undoMove(gameId: String) {
        getUndoManager(gameId).undoStep()
    }

    private fun redoMove(gameId: String) {
        getUndoManager(gameId).redoStep()
    }

    private fun broadcastGameUpdate(game: Game, messageType: MessageType) {
        sessions.forEachOpenSession(game.id) { session ->
            scope.launch {
                val includeValidMoves = sessions.assignedSide(session) == game.currentPlayer
                val payload = GameUpdate.fromGame(game, includeValidMoves)
                sendServerMessage(session, messageType, game.id, payload)
            }
        }
    }

    private inline fun <reified T> sendServerMessage(
        session: WebSocketSession, type: MessageType, gameId: String? = null, payload: T
    ) {
        safeSend(session, ServerMessage(type, gameId, payload))
    }

    private fun sendError(session: WebSocketSession, errorMsg: String) {
        sendServerMessage(session, MessageType.ERROR, payload = ErrorMessage(errorMsg))
    }

    private inline fun <reified T> safeSend(session: WebSocketSession, message: ServerMessage<T>) {
        try {
            val text = json.encodeToString(message)
            session.sendMessage(TextMessage(text))
        } catch (e: Exception) {
            logger.warn("Failed to send message to session=${session.id}: ${e.message}", e)
        }
    }

    private fun getUndoManager(gameId: String) = undoManagers.computeIfAbsent(gameId) { UndoManager() }

    @PreDestroy
    @Suppress("unused")
    private fun closeScope() {
        scope.cancel()
    }
}
