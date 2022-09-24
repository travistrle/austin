package xyz.onegrid.austin.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import xyz.onegrid.austin.web.rest.equalsVerifier

class OneGridUserTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(OneGridUser::class)
        val oneGridUser1 = OneGridUser()
        oneGridUser1.id = 1L
        val oneGridUser2 = OneGridUser()
        oneGridUser2.id = oneGridUser1.id
        assertThat(oneGridUser1).isEqualTo(oneGridUser2)
        oneGridUser2.id = 2L
        assertThat(oneGridUser1).isNotEqualTo(oneGridUser2)
        oneGridUser1.id = null
        assertThat(oneGridUser1).isNotEqualTo(oneGridUser2)
    }
}
