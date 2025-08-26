package reversi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReversiApplication

fun main(args: Array<String>) {
    runApplication<ReversiApplication>(*args)
}
