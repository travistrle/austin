package xyz.onegrid.austin.repository

import org.hibernate.annotations.QueryHints
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import xyz.onegrid.austin.domain.Todo
import java.util.Collections
import java.util.Optional
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * Utility repository to load bag relationships based on https://vladmihalcea.com/hibernate-multiplebagfetchexception/
 */
class TodoRepositoryWithBagRelationshipsImpl(
    @PersistenceContext
    private val entityManager: EntityManager
) : TodoRepositoryWithBagRelationships {

    override fun fetchBagRelationships(todo: Optional<Todo>): Optional<Todo> {
        return todo
            .map(this::fetchUsers)
    }

    override fun fetchBagRelationships(todos: Page<Todo>): Page<Todo> {
        return PageImpl<Todo>(fetchBagRelationships(todos.content), todos.pageable, todos.totalElements)
    }

    override fun fetchBagRelationships(todos: List<Todo>): MutableList<Todo> {
        return Optional
            .of(todos)
            .map(this::fetchUsers)
            .orElse(Collections.emptyList())
            .toMutableList()
    }

    fun fetchUsers(result: Todo): Todo {
        return entityManager
            .createQuery(
                "select todo from Todo todo left join fetch todo.users where todo is :todo",
                Todo::class.java
            )
            .setParameter("todo", result)
            .setHint(QueryHints.PASS_DISTINCT_THROUGH, false)
            .getSingleResult()
    }

    fun fetchUsers(todos: List<Todo>): List<Todo> {
        return entityManager
            .createQuery(
                "select distinct todo from Todo todo left join fetch todo.users where todo in :todos",
                Todo::class.java
            )
            .setParameter("todos", todos)
            .setHint(QueryHints.PASS_DISTINCT_THROUGH, false)
            .getResultList()
    }
}
