package xyz.onegrid.austin.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import xyz.onegrid.austin.web.rest.equalsVerifier

class TodoTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Todo::class)
        val todo1 = Todo()
        todo1.id = 1L
        val todo2 = Todo()
        todo2.id = todo1.id
        assertThat(todo1).isEqualTo(todo2)
        todo2.id = 2L
        assertThat(todo1).isNotEqualTo(todo2)
        todo1.id = null
        assertThat(todo1).isNotEqualTo(todo2)
    }
}
