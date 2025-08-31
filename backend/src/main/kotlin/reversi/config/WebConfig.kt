package reversi.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@Suppress("unused")
open class WebConfig(private val appConfig: AppConfig) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins(*appConfig.allowedOriginsList)
            .allowedMethods("GET", "POST", "PUT", "DELETE")
    }
}
