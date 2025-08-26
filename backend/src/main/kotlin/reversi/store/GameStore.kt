package reversi.store

import org.springframework.stereotype.Component
import reversi.model.Game

@Component
class GameStore {
    private val games: MutableMap<String, Game> = mutableMapOf()

    fun getGame(id: String): Game? = games[id]

    fun listGames(): List<Game> = games.values.toList()

    fun save(game: Game): Game {
        games[game.id] = game
        return game
    }

    fun removeGame(id: String) = games.remove(id)

}
