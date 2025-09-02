package reversi.service

import org.junit.jupiter.api.Test
import reversi.model.Board
import reversi.model.CellState
import reversi.model.Game
import reversi.websocket.dto.MessageType
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameEventPublisherTest {

    private val publisher = GameEventPublisher()

    private fun dummyGame() = Game(board = Board.new(2, CellState.EMPTY))

    @Test
    fun `subscribed listener is called on notify`() {
        var called = false
        val listener: (Game, MessageType) -> Unit = { _, _ -> called = true }

        publisher.subscribe(listener)
        publisher.notify(dummyGame(), MessageType.MAKE_MOVE)

        assertTrue(called)
    }

    @Test
    fun `unsubscribed listener is not called`() {
        var called = false
        val listener: (Game, MessageType) -> Unit = { _, _ -> called = true }

        publisher.subscribe(listener)
        publisher.unsubscribe(listener)
        publisher.notify(dummyGame(), MessageType.MAKE_MOVE)

        assertEquals(false, called)
    }

    @Test
    fun `multiple listeners are all called`() {
        val called = mutableListOf<String>()
        val l1: (Game, MessageType) -> Unit = { _, _ -> called.add("l1") }
        val l2: (Game, MessageType) -> Unit = { _, _ -> called.add("l2") }

        publisher.subscribe(l1)
        publisher.subscribe(l2)
        publisher.notify(dummyGame(), MessageType.MAKE_MOVE)

        assertEquals(listOf("l1", "l2"), called)
    }

    @Test
    fun `notify with no listeners does nothing`() {
        publisher.notify(dummyGame(), MessageType.MAKE_MOVE)
    }
}
