package reversi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reversi.util.UndoManager

@Configuration
@Suppress("unused")
open class UndoManagerConfig {
    @Bean
    open fun undoManagers(): MutableMap<String, UndoManager> = mutableMapOf()
}
