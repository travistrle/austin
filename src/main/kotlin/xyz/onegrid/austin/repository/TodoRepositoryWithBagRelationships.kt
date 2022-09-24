package xyz.onegrid.austin.repository

import org.springframework.data.domain.Page
import xyz.onegrid.austin.domain.Todo
import java.util.Optional

interface TodoRepositoryWithBagRelationships {
    fun fetchBagRelationships(todo: Optional<Todo>): Optional<Todo>

    fun fetchBagRelationships(todos: List<Todo>): MutableList<Todo>

    fun fetchBagRelationships(todos: Page<Todo>): Page<Todo>
}
