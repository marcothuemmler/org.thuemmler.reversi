package reversi.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun `handleNotFound returns 404`() {
        val ex = NoSuchElementException("Test not found")
        val response = handler.handleNotFound(ex)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("Test not found", response.body?.message)
    }

    @Test
    fun `handleNotFound returns 404 with default message`() {
        val ex = NoSuchElementException()
        val response = handler.handleNotFound(ex)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("Resource not found", response.body?.message)
    }

    @Test
    fun `handleGeneralError returns 500`() {
        val ex = Exception("Some error")
        val response = handler.handleGeneralError(ex)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("An unexpected error occurred", response.body?.message)
    }
}
