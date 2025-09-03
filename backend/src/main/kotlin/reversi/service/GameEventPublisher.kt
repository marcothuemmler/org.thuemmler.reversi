package reversi.service

import org.springframework.stereotype.Component
import reversi.model.Game
import reversi.websocket.dto.MessageType
import java.util.concurrent.CopyOnWriteArrayList

@Component
class GameEventPublisher {

    private val listeners = CopyOnWriteArrayList<(Game, MessageType) -> Unit>()

    fun subscribe(listener: (Game, MessageType) -> Unit) {
        listeners.add(listener)
    }

    fun unsubscribe(listener: (Game, MessageType) -> Unit) {
        listeners.remove(listener)
    }

    fun notify(game: Game, messageType: MessageType) {
        listeners.forEach { it(game, messageType) }
    }
}