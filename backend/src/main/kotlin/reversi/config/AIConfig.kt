package reversi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reversi.ai.AlphaBetaSelector
import reversi.ai.MoveSelectorStrategy
import reversi.service.MoveEngine

@Configuration
@Suppress("unused")
open class AIConfig {
    @Bean
    open fun alphaBetaSelector(moveEngine: MoveEngine): MoveSelectorStrategy {
        return AlphaBetaSelector(moveEngine)
    }
}
