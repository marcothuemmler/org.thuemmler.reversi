package reversi.util

interface Command {
    fun doStep()
    fun undoStep()
    fun redoStep()
}
