package reversi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reversi.util.UndoManager
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Configuration
@Suppress("unused")
open class UndoManagerConfig {
    @Bean
    open fun undoManagers(): ConcurrentMap<String, UndoManager> = ConcurrentHashMap()
}
