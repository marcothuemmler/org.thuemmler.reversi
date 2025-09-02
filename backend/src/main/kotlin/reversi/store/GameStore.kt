package reversi.store

import org.springframework.stereotype.Component
import reversi.model.Game
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Component
class GameStore {
    private val games: ConcurrentMap<String, Game> = ConcurrentHashMap()

    fun getGame(id: String): Game? = games[id]

    fun listGames(): List<Game> = games.values.toList()

    fun save(game: Game): Game {
        games[game.id] = game
        return game
    }

    fun removeGame(id: String) = games.remove(id)

}
