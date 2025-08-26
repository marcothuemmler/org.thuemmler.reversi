package reversi.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import reversi.controller.GameWebSocketHandler

@Configuration
@EnableWebSocket
@Suppress("unused")
open class WebSocketConfig(private val gameWebSocketHandler: GameWebSocketHandler) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(gameWebSocketHandler, "/ws/games")
            .setAllowedOrigins("*")
    }
}
