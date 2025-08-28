package reversi.service

import reversi.model.Game

class GameEventPublisher {

    private val listeners = mutableListOf<(Game) -> Unit>()

    fun subscribe(listener: (Game) -> Unit) {
        listeners.add(listener)
    }

    fun unsubscribe(listener: (Game) -> Unit) {
        listeners.remove(listener)
    }

    fun notify(game: Game) {
        listeners.forEach { it(game) }
    }
}