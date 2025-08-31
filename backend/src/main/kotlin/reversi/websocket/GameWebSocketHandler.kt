package reversi.websocket

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
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
import reversi.websocket.dto.ClientMessage
import reversi.websocket.dto.MessageType
import reversi.websocket.dto.ServerMessage
import java.util.*

@Component
class GameWebSocketHandler(
    private val gameService: GameService, private val undoManagers: MutableMap<String, UndoManager>
) : TextWebSocketHandler() {

    companion object {
        private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }
        private val logger = LoggerFactory.getLogger(GameWebSocketHandler::class.java.name)
        private val sessions = SessionRegistry()
    }

    init {
        gameService.eventPublisher.subscribe(::broadcastGameUpdate)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            val event = json.decodeFromString<ClientMessage>(message.payload)
            val gameId = event.gameId
            val payload = event.payload

            when (event.type) {
                MessageType.CREATE -> createGame(session, payload)
                MessageType.MAKE_MOVE -> makeMove(session, gameId, payload)
                MessageType.UNDO -> undoMove(gameId)
                MessageType.REDO -> redoMove(gameId)
                MessageType.JOIN -> joinGame(session, gameId)
                MessageType.ERROR -> sendError(session, "Client sent ERROR type")
            }
        } catch (e: Exception) {
            logger.error("Failed to handle message from session=${session.id}: ${e.message}", e)
            sendError(session, "Failed to handle message: ${e.message}")
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val gameId = sessions.remove(session) ?: return
        logger.info("Session closed: session=${session.id}, gameId=$gameId, status=$status")

        if (sessions.sessionsForGame(gameId).isEmpty()) {
            undoManagers.remove(gameId)
        }
    }

    private fun createGame(session: WebSocketSession, payload: JsonElement?) {
        requireNotNull(payload)
        val newGame = json.decodeFromJsonElement<NewGameRequest>(payload)

        val id = newGame.id ?: UUID.randomUUID().toString()
        getUndoManager(id)

        val humanSides = newGame.playerTypes.filter { it.value == PlayerType.HUMAN }.keys
        val assignedSide = newGame.preferredSide?.takeIf { it in humanSides } ?: humanSides.first()
        sessions.register(session, id, assignedSide)

        logger.info("Game created: gameId=$id, session=${session.id}, side=$assignedSide")

        gameService.createGame(newGame.copy(id = id))
    }

    private fun joinGame(session: WebSocketSession, gameId: String?) {
        requireNotNull(gameId) { "Missing gameId" }
        val game = gameService.getGame(gameId)
        requireNotNull(game) { "Game not found" }

        val assignedSide = assignSide(game) ?: return sendError(session, "All sides taken")

        sessions.register(session, gameId, assignedSide)

        logger.info("Player joined: gameId=$gameId, session=${session.id}, side=$assignedSide")

        sendServerMessage(session, MessageType.JOIN, gameId, game)
    }

    private fun assignSide(game: Game): CellState? {
        val sessionsForGame = sessions.sessionsForGame(game.id)
        val possibleSides = game.playerTypes.filterValues { it == PlayerType.HUMAN }
        val takenSides = sessionsForGame.mapNotNull { sessions.assignedSide(it) }.toSet()
        return possibleSides.keys.firstOrNull { it !in takenSides }
    }

    private fun makeMove(session: WebSocketSession, gameId: String?, payload: JsonElement?) {
        requireNotNull(gameId)
        requireNotNull(payload)

        val game = gameService.getGame(gameId) ?: run {
            logger.warn("Move rejected: game not found, session=${session.id}, gameId=$gameId")
            sendError(session, "Game not found")
            return
        }

        val side = sessions.assignedSide(session)
        if (side != game.currentPlayer) {
            logger.warn("Move rejected: out of turn, session=${session.id}, side=$side, currentPlayer=${game.currentPlayer}")
            sendError(session, "It's not your turn")
            return
        }

        val (row, col) = json.decodeFromJsonElement<MoveRequest>(payload)
        val command = MoveCommand(gameService, gameId, row, col)
        logger.info("Move made: gameId=$gameId, session=${session.id}, side=$side, row=$row, col=$col")
        getUndoManager(gameId).doStep(command)
    }

    private fun undoMove(gameId: String?) {
        logger.info("Undo requested: gameId=$gameId")
        getUndoManager(gameId).undoStep()
    }

    private fun redoMove(gameId: String?) {
        logger.info("Redo requested: gameId=$gameId")
        getUndoManager(gameId).redoStep()
    }

    private fun broadcastGameUpdate(game: Game, messageType: MessageType) {
        sessions.sessionsForGame(game.id).filter { it.isOpen }.forEach { session ->
            try {
                sendServerMessage(session, messageType, game.id, game)
            } catch (e: Exception) {
                logger.warn("Failed to send message to session: ${e.message}")
            }
        }
    }

    private fun sendServerMessage(session: WebSocketSession, type: MessageType, gameId: String, payload: Game) {
        val message = ServerMessage(type, gameId, json.encodeToJsonElement(payload))
        session.sendMessage(TextMessage(json.encodeToString(message)))
    }

    private fun sendError(session: WebSocketSession, errorMsg: String) {
        val errorPayload = json.encodeToJsonElement(mapOf("error" to errorMsg))
        val errorMessage = ServerMessage(type = MessageType.ERROR, payload = errorPayload)
        try {
            session.sendMessage(TextMessage(json.encodeToString(errorMessage)))
        } catch (e: Exception) {
            logger.error("Failed to send error message: ${e.message}")
        }
    }

    private fun getUndoManager(gameId: String?): UndoManager {
        requireNotNull(gameId)
        return undoManagers.getOrPut(gameId) { UndoManager() }
    }
}
