package xyz.onegrid.austin.web.rest

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import tech.jhipster.web.util.HeaderUtil
import tech.jhipster.web.util.ResponseUtil
import xyz.onegrid.austin.domain.Todo
import xyz.onegrid.austin.repository.TodoRepository
import xyz.onegrid.austin.web.rest.errors.BadRequestAlertException
import java.net.URI
import java.net.URISyntaxException
import java.util.Objects
import javax.validation.Valid
import javax.validation.constraints.NotNull

private const val ENTITY_NAME = "austinTodo"
/**
 * REST controller for managing [xyz.onegrid.austin.domain.Todo].
 */
@RestController
@RequestMapping("/api")
@Transactional
class TodoResource(
    private val todoRepository: TodoRepository,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "austinTodo"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /todos` : Create a new todo.
     *
     * @param todo the todo to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new todo, or with status `400 (Bad Request)` if the todo has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/todos")
    fun createTodo(@Valid @RequestBody todo: Todo): ResponseEntity<Todo> {
        log.debug("REST request to save Todo : $todo")
        if (todo.id != null) {
            throw BadRequestAlertException(
                "A new todo cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = todoRepository.save(todo)
        return ResponseEntity.created(URI("/api/todos/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /todos/:id} : Updates an existing todo.
     *
     * @param id the id of the todo to save.
     * @param todo the todo to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated todo,
     * or with status `400 (Bad Request)` if the todo is not valid,
     * or with status `500 (Internal Server Error)` if the todo couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/todos/{id}")
    fun updateTodo(
        @PathVariable(value = "id", required = false) id: Long,
        @Valid @RequestBody todo: Todo
    ): ResponseEntity<Todo> {
        log.debug("REST request to update Todo : {}, {}", id, todo)
        if (todo.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, todo.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!todoRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = todoRepository.save(todo)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME,
                    todo.id.toString()
                )
            )
            .body(result)
    }

    /**
     * {@code PATCH  /todos/:id} : Partial updates given fields of an existing todo, field will ignore if it is null
     *
     * @param id the id of the todo to save.
     * @param todo the todo to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated todo,
     * or with status {@code 400 (Bad Request)} if the todo is not valid,
     * or with status {@code 404 (Not Found)} if the todo is not found,
     * or with status {@code 500 (Internal Server Error)} if the todo couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = ["/todos/{id}"], consumes = ["application/json", "application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateTodo(
        @PathVariable(value = "id", required = false) id: Long,
        @NotNull @RequestBody todo: Todo
    ): ResponseEntity<Todo> {
        log.debug("REST request to partial update Todo partially : {}, {}", id, todo)
        if (todo.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, todo.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!todoRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = todoRepository.findById(todo.id)
            .map {

                if (todo.task != null) {
                    it.task = todo.task
                }
                if (todo.scheduledTime != null) {
                    it.scheduledTime = todo.scheduledTime
                }
                if (todo.validUntil != null) {
                    it.validUntil = todo.validUntil
                }
                if (todo.createdDate != null) {
                    it.createdDate = todo.createdDate
                }
                if (todo.lastModifiedDate != null) {
                    it.lastModifiedDate = todo.lastModifiedDate
                }
                if (todo.createdBy != null) {
                    it.createdBy = todo.createdBy
                }
                if (todo.lastModifiedBy != null) {
                    it.lastModifiedBy = todo.lastModifiedBy
                }

                it
            }
            .map { todoRepository.save(it) }

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, todo.id.toString())
        )
    }

    /**
     * `GET  /todos` : get all the todos.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the [ResponseEntity] with status `200 (OK)` and the list of todos in body.
     */
    @GetMapping("/todos")
    fun getAllTodos(@RequestParam(required = false, defaultValue = "false") eagerload: Boolean): MutableList<Todo> {

        log.debug("REST request to get all Todos")
        return todoRepository.findAllWithEagerRelationships()
    }

    /**
     * `GET  /todos/:id` : get the "id" todo.
     *
     * @param id the id of the todo to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the todo, or with status `404 (Not Found)`.
     */
    @GetMapping("/todos/{id}")
    fun getTodo(@PathVariable id: Long): ResponseEntity<Todo> {
        log.debug("REST request to get Todo : $id")
        val todo = todoRepository.findOneWithEagerRelationships(id)
        return ResponseUtil.wrapOrNotFound(todo)
    }
    /**
     *  `DELETE  /todos/:id` : delete the "id" todo.
     *
     * @param id the id of the todo to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/todos/{id}")
    fun deleteTodo(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Todo : $id")

        todoRepository.deleteById(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }
}
