package reversi.ai

import reversi.model.Game

interface MoveSelectorStrategy {
    fun selectMove(game: Game): Pair<Int, Int>?
}