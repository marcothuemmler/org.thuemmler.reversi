package reversi.websocket

import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession
import reversi.model.CellState
import java.util.concurrent.ConcurrentHashMap

@Component
class SessionRegistry {
    private data class SessionInfo(val gameId: String, val side: CellState)

    private val sessions = ConcurrentHashMap<WebSocketSession, SessionInfo>()

    fun register(session: WebSocketSession, gameId: String, side: CellState) {
        sessions[session] = SessionInfo(gameId, side)
    }

    fun remove(session: WebSocketSession) = sessions.remove(session)?.gameId


    fun assignedSide(session: WebSocketSession) = sessions[session]?.side

    fun openSessionsForGame(gameId: String) =
        sessions.filter { (session, info) -> info.gameId == gameId && session.isOpen }.keys

    fun forEachOpenSession(gameId: String, block: (WebSocketSession) -> Unit) {
        sessions.forEach { (session, info) ->
            if (info.gameId == gameId && session.isOpen) block(session)
        }
    }

    fun hasOpenSessions(gameId: String) = sessions.any { (session, info) -> info.gameId == gameId && session.isOpen }
}
