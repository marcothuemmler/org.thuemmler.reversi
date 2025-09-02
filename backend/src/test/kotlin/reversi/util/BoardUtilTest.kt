package reversi.util

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class BoardUtilTest {

    @Test
    fun `directions contains exactly all valid directions`() {
        val expected = listOf(
            -1 to -1, -1 to 0, -1 to 1, 0 to -1, 0 to 1, 1 to -1, 1 to 0, 1 to 1
        )

        assertEquals(expected.toSet(), BoardUtil.directions.toSet())
    }
}
