package reversi.websocket

import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession
import reversi.model.CellState
import java.util.concurrent.ConcurrentHashMap

@Component
class SessionRegistry {
    private val sessions = ConcurrentHashMap<String, MutableSet<WebSocketSession>>()
    private val sessionToGameId = ConcurrentHashMap<WebSocketSession, String>()
    private val sessionSides = ConcurrentHashMap<WebSocketSession, CellState>()

    fun register(session: WebSocketSession, gameId: String, side: CellState) {
        sessionToGameId[session] = gameId
        sessionSides[session] = side
        sessions.computeIfAbsent(gameId) { ConcurrentHashMap.newKeySet() }.add(session)
    }

    fun remove(session: WebSocketSession): String? {
        val gameId = sessionToGameId.remove(session)
        sessionSides.remove(session)
        gameId?.let {
            sessions[it]?.remove(session)
            if (sessions[it]?.isEmpty() == true) sessions.remove(it)
        }
        return gameId
    }

    fun sessionsForGame(gameId: String): Set<WebSocketSession> =
        sessions[gameId]?.filter { it.isOpen }?.toSet() ?: emptySet()

    fun assignedSide(session: WebSocketSession): CellState? {
        return sessionSides[session]
    }
}
