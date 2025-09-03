package reversi.controller

import org.springframework.web.bind.annotation.*
import reversi.controller.dto.MoveRequest
import reversi.controller.dto.NewGameRequest
import reversi.model.Game
import reversi.service.GameService
import reversi.util.MoveCommand
import reversi.util.UndoManager

@RestController
@RequestMapping("/games")
@Suppress("unused")
class GameController(
    private val service: GameService,
    private val undoManagers: MutableMap<String, UndoManager>
) {

    @PostMapping
    fun createGame(@RequestBody game: NewGameRequest): Game = service.createGame(game)

    @GetMapping
    fun listGames(): List<Game> = service.listGames()

    @GetMapping("/{id}")
    fun getGame(@PathVariable id: String): Game {
        return service.getGame(id) ?: throw NoSuchElementException("Game not found: $id")
    }

    @DeleteMapping("/{id}")
    fun removeGame(@PathVariable id: String) {
        undoManagers.remove(id)
        service.removeGame(id)
    }

    @PostMapping("/{id}/moves")
    fun makeMove(@PathVariable id: String, @RequestBody move: MoveRequest): Game {
        val undoManager = undoManagers.getOrPut(id) { UndoManager() }
        val command = MoveCommand(service, id, move.row, move.col)
        undoManager.doStep(command)
        return service.getGame(id) ?: throw NoSuchElementException("Game not found: $id")
    }

    @PostMapping("/{id}/undo")
    fun undoMove(@PathVariable id: String): Game {
        val undoManager = undoManagers[id] ?: throw IllegalStateException("No moves to undo")
        undoManager.undoStep()
        return service.getGame(id)  ?: throw NoSuchElementException("Game not found: $id")
    }

    @PostMapping("/{id}/redo")
    fun redoMove(@PathVariable id: String): Game {
        val undoManager = undoManagers[id] ?: throw IllegalStateException("No moves to redo")
        undoManager.redoStep()
        return service.getGame(id) ?: throw NoSuchElementException("Game not found: $id")
    }
}
