package reversi.util

interface Command {
    suspend fun doStep()
    fun undoStep()
    fun redoStep()
}
