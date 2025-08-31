package reversi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
open class AppConfig(
    @param:Value($$"${app.allowed-origins}") private val allowedOrigins: String
) {
    val allowedOriginsList: Array<String> = allowedOrigins.split(",").map { it.trim() }.toTypedArray()
}
