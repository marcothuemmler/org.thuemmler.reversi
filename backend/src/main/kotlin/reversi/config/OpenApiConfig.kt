package reversi.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@Suppress("unused")
open class OpenApiConfig(private val buildProperties: BuildProperties) {

    @Bean
    open fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Reversi API")
                    .description("REST API for Reversi game")
                    .version(buildProperties.version)
            )
    }
}
