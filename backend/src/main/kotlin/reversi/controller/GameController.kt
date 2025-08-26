package reversi.controller

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reversi.controller.dto.MoveRequest
import reversi.model.Game
import reversi.service.GameService

@RestController
@RequestMapping("/games")
@Suppress("unused")
class GameController(private val service: GameService) {

    @PostMapping
    fun createGame(): Game = service.createGame()

    @GetMapping
    fun listGames(): List<Game> = service.listGames()

    @GetMapping("/{id}")
    fun getGame(@PathVariable id: String): Game {
        return service.getGame(id) ?: throw NoSuchElementException("Game not found: $id")
    }

    @DeleteMapping("/{id}")
    fun removeGame(@PathVariable id: String) {
        service.removeGame(id)
    }

    @PostMapping("/{id}/moves")
    fun makeMove(@PathVariable id: String, @RequestBody move: MoveRequest): Game {
        return service.makeMove(id, move.row, move.col)
    }

    @GetMapping("/{id}/moves")
    fun getValidMoves(@PathVariable id: String): List<MoveRequest> {
        return service.getValidMoves(id).map { MoveRequest(it.first, it.second) }
    }
}
