package reversi.service

import reversi.model.Game

interface MoveSelectorStrategy {
    suspend fun selectMove(game: Game): Pair<Int, Int>?
}
