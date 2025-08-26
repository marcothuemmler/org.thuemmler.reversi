package reversi.util

class UndoManager {

    private val undoStack = mutableListOf<Command>()
    private val redoStack = mutableListOf<Command>()

    fun doStep(command: Command) {
        undoStack.add(0, command)
        command.doStep()
        redoStack.clear()
    }

    fun undoStep() {
        if (undoStack.isNotEmpty()) {
            val head = undoStack.removeAt(0)
            head.undoStep()
            redoStack.add(0, head)
        }
    }

    fun redoStep() {
        if (redoStack.isNotEmpty()) {
            val head = redoStack.removeAt(0)
            head.redoStep()
            undoStack.add(0, head)
        }
    }
}
