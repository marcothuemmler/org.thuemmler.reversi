package reversi.websocket

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.web.socket.WebSocketSession
import reversi.model.CellState
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionRegistryTest {

    private val registry = SessionRegistry()

    private fun mockSession(isOpen: Boolean = true): WebSocketSession {
        val session = mockk<WebSocketSession>(relaxed = true)
        every { session.isOpen } returns isOpen
        return session
    }

    @Test
    fun `register stores session and assignedSide retrieves it`() {
        val session = mockSession()
        registry.register(session, "game1", CellState.BLACK)

        assertEquals(CellState.BLACK, registry.assignedSide(session))
    }

    @Test
    fun `remove deletes session and returns gameId`() {
        val session = mockSession()
        registry.register(session, "game1", CellState.WHITE)

        val removedGameId = registry.remove(session)

        assertEquals("game1", removedGameId)
        assertNull(registry.assignedSide(session))
    }

    @Test
    fun `openSessionsForGame returns only open sessions for game`() {
        val s1 = mockSession(true)
        val s2 = mockSession(false)
        val s3 = mockSession(true)

        registry.register(s1, "game1", CellState.BLACK)
        registry.register(s2, "game1", CellState.WHITE)
        registry.register(s3, "game2", CellState.BLACK)

        val sessions = registry.openSessionsForGame("game1")

        assertTrue(s1 in sessions)
        assertFalse(s2 in sessions)
        assertFalse(s3 in sessions)
    }

    @Test
    fun `forEachOpenSession executes block only for open sessions in game`() {
        val s1 = mockSession(true)
        val s2 = mockSession(false)
        registry.register(s1, "game1", CellState.BLACK)
        registry.register(s2, "game1", CellState.WHITE)

        val called = mutableListOf<WebSocketSession>()
        registry.forEachOpenSession("game1") { called.add(it) }

        assertEquals(listOf(s1), called)
    }

    @Test
    fun `hasOpenSessions returns true if open session exists`() {
        val s1 = mockSession(true)
        registry.register(s1, "game1", CellState.BLACK)

        assertTrue(registry.hasOpenSessions("game1"))
    }

    @Test
    fun `hasOpenSessions returns false if no open session exists`() {
        val s1 = mockSession(false)
        registry.register(s1, "game1", CellState.BLACK)

        assertFalse(registry.hasOpenSessions("game1"))
    }

    @Test
    fun `methods handle empty registry gracefully`() {
        val session = mockSession()
        assertNull(registry.remove(session))
        assertNull(registry.assignedSide(session))
        assertDoesNotThrow {
            registry.forEachOpenSession("unknown") { }
        }
    }
}
