package reversi

import io.github.cdimascio.dotenv.dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.MapPropertySource

@SpringBootApplication
class ReversiApplication

fun main(args: Array<String>) {
    val env = if (java.io.File(".env").exists()) dotenv() else null

    runApplication<ReversiApplication>(*args) {
        env?.let { loadedEnv ->
            addInitializers( { ctx: ConfigurableApplicationContext ->
                val props = loadedEnv.entries().associate { it.key to it.value }
                ctx.environment.propertySources.addFirst(
                    MapPropertySource("dotenvProps", props)
                )
            })
        }
    }
}
