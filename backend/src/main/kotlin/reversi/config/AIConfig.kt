package reversi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reversi.ai.AlphaBetaSelector
import reversi.service.GameService
import reversi.ai.MoveSelectorStrategy

@Configuration
@Suppress("unused")
open class AIConfig {
    @Bean
    open fun alphaBetaSelector(gameProvider: GameService): MoveSelectorStrategy {
        return AlphaBetaSelector(gameProvider)
    }
}