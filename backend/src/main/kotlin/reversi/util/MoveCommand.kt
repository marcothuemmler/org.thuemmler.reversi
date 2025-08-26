package reversi.util

import reversi.model.Game
import reversi.service.GameService

class MoveCommand(
    private val gameService: GameService,
    private val gameId: String,
    private val row: Int,
    private val col: Int
) : Command {

    private var previousGame: Game? = null
    private var newGame: Game? = null

    override fun doStep() {
        previousGame = gameService.getGame(gameId)
        newGame = gameService.makeMove(gameId, row, col)
    }

    override fun undoStep() {
        previousGame?.let {
            gameService.saveState(it)
        }
    }

    override fun redoStep() {
        newGame?.let {
            gameService.saveState(it)
        }
    }
}