package reversi.service

import reversi.model.CellState
import reversi.model.Game
import reversi.model.GameBoard
import reversi.model.MoveList

interface MoveEngine {
    fun calculateValidMoves(board: GameBoard, player: CellState): MoveList
    fun isGameFinished(board: GameBoard): Boolean
    fun applyPlayerMove(game: Game, row: Int, col: Int): Game
    fun getFlippableCells(board: GameBoard, row: Int, col: Int, player: CellState, dx: Int, dy: Int): MoveList
    fun applyMove(board: GameBoard, row: Int, col: Int, player: CellState, flippable: MoveList): GameBoard
}
