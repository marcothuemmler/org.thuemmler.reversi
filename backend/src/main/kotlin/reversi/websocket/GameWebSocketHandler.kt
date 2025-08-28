package reversi.websocket

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import reversi.controller.dto.MoveRequest
import reversi.controller.dto.NewGameRequest
import reversi.model.Game
import reversi.service.GameService
import reversi.util.MoveCommand
import reversi.util.UndoManager
import reversi.websocket.dto.ClientMessage
import reversi.websocket.dto.MessageType
import reversi.websocket.dto.ServerMessage
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class GameWebSocketHandler(
    private val gameService: GameService,
    private val undoManagers: MutableMap<String, UndoManager>
) : TextWebSocketHandler() {

    private val sessions = ConcurrentHashMap<String, MutableSet<WebSocketSession>>()
    private val sessionToGameId = ConcurrentHashMap<WebSocketSession, String>()
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }
    private val logger = LoggerFactory.getLogger(GameWebSocketHandler::class.java.name)

    init {
        gameService.eventPublisher.subscribe(::broadcastGameUpdate)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            val event = Json.decodeFromString<ClientMessage>(message.payload)
            val gameId = event.gameId
            val payload = event.payload

            when (event.type) {
                MessageType.CREATE -> createGame(session, Json.decodeFromJsonElement<NewGameRequest>(payload!!))
                MessageType.MAKE_MOVE -> makeMove(gameId, Json.decodeFromJsonElement<MoveRequest>(payload!!))
                MessageType.UNDO -> undoMove(gameId)
                MessageType.REDO -> redoMove(gameId)
                MessageType.JOIN -> joinGame(session, gameId)
                MessageType.ERROR -> sendError(session, "Client sent ERROR type")
            }
        } catch (e: Exception) {
            sendError(session, "Failed to handle message: ${e.message}")
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val gameId = sessionToGameId.remove(session) ?: return
        sessions[gameId]?.remove(session)
        if (sessions[gameId]?.isEmpty() == true) {
            sessions.remove(gameId)
            undoManagers.remove(gameId)
        }
    }

    private fun createGame(session: WebSocketSession, newGame: NewGameRequest) {
        val id = newGame.id ?: UUID.randomUUID().toString()
        undoManagers[id] = UndoManager()
        sessionToGameId[session] = id
        sessions.computeIfAbsent(id) { mutableSetOf() }.add(session)
        gameService.createGame(newGame.copy(id = id))
    }

    private fun makeMove(gameId: String?, moveRequest: MoveRequest) {
        requireNotNull(gameId)
        val undoManager = undoManagers.getOrPut(gameId) { UndoManager() }
        val command = MoveCommand(gameService, gameId, moveRequest.row, moveRequest.col)
        undoManager.doStep(command)
    }

    private fun undoMove(gameId: String?) {
        requireNotNull(gameId)
        undoManagers.getOrPut(gameId) { UndoManager() }.undoStep()
    }

    private fun redoMove(gameId: String?) {
        requireNotNull(gameId)
        undoManagers.getOrPut(gameId) { UndoManager() }.redoStep()
    }

    private fun broadcastGameUpdate(game: Game) {
        sessions[game.id]?.filter { it.isOpen }?.forEach { session ->
            try {
                sendServerMessage(session, MessageType.MAKE_MOVE, game.id, game)
            } catch (e: Exception) {
                logger.warn("Failed to send message to session: ${e.message}")
            }
        }
    }

    private fun sendServerMessage(session: WebSocketSession, type: MessageType, gameId: String, payload: Game) {
        val message = ServerMessage(type, gameId, json.encodeToJsonElement(payload).jsonObject)
        session.sendMessage(TextMessage(json.encodeToString(message)))
    }

    private fun joinGame(session: WebSocketSession, gameId: String?) {
        requireNotNull(gameId) { "Missing gameId" }
        val game = gameService.getGame(gameId)
        requireNotNull(game) { "Game not found" }
        sessionToGameId[session] = gameId
        sessions.computeIfAbsent(gameId) { mutableSetOf() }.add(session)
        sendServerMessage(session, MessageType.JOIN, gameId, game)
    }

    private fun sendError(session: WebSocketSession, errorMsg: String) {
        val errorPayload = Json.encodeToJsonElement(mapOf("error" to errorMsg)).jsonObject
        val errorMessage = ServerMessage(type = MessageType.ERROR, gameId = "", payload = errorPayload)
        try {
            session.sendMessage(TextMessage(json.encodeToString(errorMessage)))
        } catch (e: Exception) {
            logger.error("Failed to send error message: ${e.message}")
        }
    }
}
