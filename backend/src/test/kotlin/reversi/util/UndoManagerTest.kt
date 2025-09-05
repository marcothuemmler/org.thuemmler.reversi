package reversi.util

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class UndoManagerTest {

    private lateinit var undoManager: UndoManager

    @BeforeEach
    fun setup() {
        undoManager = UndoManager()
    }

    @Test
    fun `multiple commands undo and redo in correct order`() = runTest {
        var state = 0

        val command1 = object : Command {
            override suspend fun doStep() { state += 1 }
            override fun undoStep() { state -= 1 }
            override fun redoStep() { state += 1 }
        }

        val command2 = object : Command {
            override suspend fun doStep() { state += 10 }
            override fun undoStep() { state -= 10 }
            override fun redoStep() { state += 10 }
        }

        undoManager.doStep(command1)
        undoManager.doStep(command2)
        assertEquals(11, state)

        undoManager.undoStep()
        assertEquals(1, state)

        undoManager.undoStep()
        assertEquals(0, state)

        undoManager.redoStep()
        assertEquals(1, state)

        undoManager.redoStep()
        assertEquals(11, state)
    }

    @Test
    fun `undo and redo do nothing when stacks are empty`() {
        val state = 0

        undoManager.undoStep()
        undoManager.redoStep()

        assertEquals(0, state, "State should remain unchanged when stacks are empty")
    }
}
