package reversi.model

import kotlinx.serialization.Serializable

@Serializable
data class Board<T>(
    val size: Int = 8,
    val grid: List<List<T>>
) {
    companion object {
        inline fun <reified T> new(size: Int = 8, defaultValue: T) =
            Board(size, grid = List(size) { List(size) { defaultValue } })

        inline fun <reified T> new(defaultValue: T) = new(8, defaultValue)
    }

    fun getCell(row: Int, col: Int) = grid[row][col]

    fun setCell(row: Int, col: Int, value: T): Board<T> {
        val newGrid = grid.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, cell -> if (r == row && c == col) value else cell }
        }
        return copy(grid = newGrid)
    }
}
