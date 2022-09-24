package xyz.onegrid.austin.web.rest

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.data.domain.PageImpl
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.Validator
import xyz.onegrid.austin.IntegrationTest
import xyz.onegrid.austin.domain.Todo
import xyz.onegrid.austin.repository.TodoRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Random
import java.util.concurrent.atomic.AtomicLong
import javax.persistence.EntityManager
import kotlin.test.assertNotNull

/**
 * Integration tests for the [TodoResource] REST controller.
 */
@IntegrationTest
@Extensions(
    ExtendWith(MockitoExtension::class)
)
@AutoConfigureMockMvc
@WithMockUser
class TodoResourceIT {
    @Autowired
    private lateinit var todoRepository: TodoRepository

    @Mock
    private lateinit var todoRepositoryMock: TodoRepository

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var validator: Validator

    @Autowired
    private lateinit var em: EntityManager

    @Autowired
    private lateinit var restTodoMockMvc: MockMvc

    private lateinit var todo: Todo

    @BeforeEach
    fun initTest() {
        todo = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createTodo() {
        val databaseSizeBeforeCreate = todoRepository.findAll().size
        // Create the Todo
        restTodoMockMvc.perform(
            post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(todo))
        ).andExpect(status().isCreated)

        // Validate the Todo in the database
        val todoList = todoRepository.findAll()
        assertThat(todoList).hasSize(databaseSizeBeforeCreate + 1)
        val testTodo = todoList[todoList.size - 1]

        assertThat(testTodo.task).isEqualTo(DEFAULT_TASK)
        assertThat(testTodo.scheduledTime).isEqualTo(DEFAULT_SCHEDULED_TIME)
        assertThat(testTodo.validUntil).isEqualTo(DEFAULT_VALID_UNTIL)
        assertThat(testTodo.createdDate).isEqualTo(DEFAULT_CREATED_DATE)
        assertThat(testTodo.lastModifiedDate).isEqualTo(DEFAULT_LAST_MODIFIED_DATE)
        assertThat(testTodo.createdBy).isEqualTo(DEFAULT_CREATED_BY)
        assertThat(testTodo.lastModifiedBy).isEqualTo(DEFAULT_LAST_MODIFIED_BY)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createTodoWithExistingId() {
        // Create the Todo with an existing ID
        todo.id = 1L

        val databaseSizeBeforeCreate = todoRepository.findAll().size

        // An entity with an existing ID cannot be created, so this API call must fail
        restTodoMockMvc.perform(
            post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(todo))
        ).andExpect(status().isBadRequest)

        // Validate the Todo in the database
        val todoList = todoRepository.findAll()
        assertThat(todoList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkTaskIsRequired() {
        val databaseSizeBeforeTest = todoRepository.findAll().size
        // set the field null
        todo.task = null

        // Create the Todo, which fails.

        restTodoMockMvc.perform(
            post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(todo))
        ).andExpect(status().isBadRequest)

        val todoList = todoRepository.findAll()
        assertThat(todoList).hasSize(databaseSizeBeforeTest)
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkScheduledTimeIsRequired() {
        val databaseSizeBeforeTest = todoRepository.findAll().size
        // set the field null
        todo.scheduledTime = null

        // Create the Todo, which fails.

        restTodoMockMvc.perform(
            post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(todo))
        ).andExpect(status().isBadRequest)

        val todoList = todoRepository.findAll()
        assertThat(todoList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllTodos() {
        // Initialize the database
        todoRepository.saveAndFlush(todo)

        // Get all the todoList
        restTodoMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(todo.id?.toInt())))
            .andExpect(jsonPath("$.[*].task").value(hasItem(DEFAULT_TASK)))
            .andExpect(jsonPath("$.[*].scheduledTime").value(hasItem(DEFAULT_SCHEDULED_TIME)))
            .andExpect(jsonPath("$.[*].validUntil").value(hasItem(DEFAULT_VALID_UNTIL.toString())))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())))
            .andExpect(jsonPath("$.[*].lastModifiedDate").value(hasItem(DEFAULT_LAST_MODIFIED_DATE.toString())))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY?.toInt())))
            .andExpect(jsonPath("$.[*].lastModifiedBy").value(hasItem(DEFAULT_LAST_MODIFIED_BY?.toInt())))
    }

    @Suppress("unchecked")
    @Throws(Exception::class)
    fun getAllTodosWithEagerRelationshipsIsEnabled() {
        val todoResource = TodoResource(todoRepositoryMock)
        `when`(todoRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(PageImpl(mutableListOf()))

        restTodoMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true"))
            .andExpect(status().isOk)

        verify(todoRepositoryMock, times(1)).findAllWithEagerRelationships(any())
    }

    @Suppress("unchecked")
    @Throws(Exception::class)
    fun getAllTodosWithEagerRelationshipsIsNotEnabled() {
        val todoResource = TodoResource(todoRepositoryMock)
        `when`(todoRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(PageImpl(mutableListOf()))

        restTodoMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true"))
            .andExpect(status().isOk)

        verify(todoRepositoryMock, times(1)).findAllWithEagerRelationships(any())
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getTodo() {
        // Initialize the database
        todoRepository.saveAndFlush(todo)

        val id = todo.id
        assertNotNull(id)

        // Get the todo
        restTodoMockMvc.perform(get(ENTITY_API_URL_ID, todo.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(todo.id?.toInt()))
            .andExpect(jsonPath("$.task").value(DEFAULT_TASK))
            .andExpect(jsonPath("$.scheduledTime").value(DEFAULT_SCHEDULED_TIME))
            .andExpect(jsonPath("$.validUntil").value(DEFAULT_VALID_UNTIL.toString()))
            .andExpect(jsonPath("$.createdDate").value(DEFAULT_CREATED_DATE.toString()))
            .andExpect(jsonPath("$.lastModifiedDate").value(DEFAULT_LAST_MODIFIED_DATE.toString()))
            .andExpect(jsonPath("$.createdBy").value(DEFAULT_CREATED_BY?.toInt()))
            .andExpect(jsonPath("$.lastModifiedBy").value(DEFAULT_LAST_MODIFIED_BY?.toInt()))
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingTodo() {
        // Get the todo
        restTodoMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putNewTodo() {
        // Initialize the database
        todoRepository.saveAndFlush(todo)

        val databaseSizeBeforeUpdate = todoRepository.findAll().size

        // Update the todo
        val updatedTodo = todoRepository.findById(todo.id).get()
        // Disconnect from session so that the updates on updatedTodo are not directly saved in db
        em.detach(updatedTodo)
        updatedTodo.task = UPDATED_TASK
        updatedTodo.scheduledTime = UPDATED_SCHEDULED_TIME
        updatedTodo.validUntil = UPDATED_VALID_UNTIL
        updatedTodo.createdDate = UPDATED_CREATED_DATE
        updatedTodo.lastModifiedDate = UPDATED_LAST_MODIFIED_DATE
        updatedTodo.createdBy = UPDATED_CREATED_BY
        updatedTodo.lastModifiedBy = UPDATED_LAST_MODIFIED_BY

        restTodoMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedTodo.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedTodo))
        ).andExpect(status().isOk)

        // Validate the Todo in the database
        val todoList = todoRepository.findAll()
        assertThat(todoList).hasSize(databaseSizeBeforeUpdate)
        val testTodo = todoList[todoList.size - 1]
        assertThat(testTodo.task).isEqualTo(UPDATED_TASK)
        assertThat(testTodo.scheduledTime).isEqualTo(UPDATED_SCHEDULED_TIME)
        assertThat(testTodo.validUntil).isEqualTo(UPDATED_VALID_UNTIL)
        assertThat(testTodo.createdDate).isEqualTo(UPDATED_CREATED_DATE)
        assertThat(testTodo.lastModifiedDate).isEqualTo(UPDATED_LAST_MODIFIED_DATE)
        assertThat(testTodo.createdBy).isEqualTo(UPDATED_CREATED_BY)
        assertThat(testTodo.lastModifiedBy).isEqualTo(UPDATED_LAST_MODIFIED_BY)
    }

    @Test
    @Transactional
    fun putNonExistingTodo() {
        val databaseSizeBeforeUpdate = todoRepository.findAll().size
        todo.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTodoMockMvc.perform(
            put(ENTITY_API_URL_ID, todo.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(todo))
        )
            .andExpect(status().isBadRequest)

        // Validate the Todo in the database
        val todoList = todoRepository.findAll()
        assertThat(todoList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchTodo() {
        val databaseSizeBeforeUpdate = todoRepository.findAll().size
        todo.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTodoMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(todo))
        ).andExpect(status().isBadRequest)

        // Validate the Todo in the database
        val todoList = todoRepository.findAll()
        assertThat(todoList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamTodo() {
        val databaseSizeBeforeUpdate = todoRepository.findAll().size
        todo.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTodoMockMvc.perform(
            put(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(todo))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the Todo in the database
        val todoList = todoRepository.findAll()
        assertThat(todoList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateTodoWithPatch() {
        todoRepository.saveAndFlush(todo)

        val databaseSizeBeforeUpdate = todoRepository.findAll().size

// Update the todo using partial update
        val partialUpdatedTodo = Todo().apply {
            id = todo.id

            task = UPDATED_TASK
            validUntil = UPDATED_VALID_UNTIL
            createdDate = UPDATED_CREATED_DATE
            lastModifiedBy = UPDATED_LAST_MODIFIED_BY
        }

        restTodoMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedTodo.id)
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedTodo))
        )
            .andExpect(status().isOk)

// Validate the Todo in the database
        val todoList = todoRepository.findAll()
        assertThat(todoList).hasSize(databaseSizeBeforeUpdate)
        val testTodo = todoList.last()
        assertThat(testTodo.task).isEqualTo(UPDATED_TASK)
        assertThat(testTodo.scheduledTime).isEqualTo(DEFAULT_SCHEDULED_TIME)
        assertThat(testTodo.validUntil).isEqualTo(UPDATED_VALID_UNTIL)
        assertThat(testTodo.createdDate).isEqualTo(UPDATED_CREATED_DATE)
        assertThat(testTodo.lastModifiedDate).isEqualTo(DEFAULT_LAST_MODIFIED_DATE)
        assertThat(testTodo.createdBy).isEqualTo(DEFAULT_CREATED_BY)
        assertThat(testTodo.lastModifiedBy).isEqualTo(UPDATED_LAST_MODIFIED_BY)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateTodoWithPatch() {
        todoRepository.saveAndFlush(todo)

        val databaseSizeBeforeUpdate = todoRepository.findAll().size

// Update the todo using partial update
        val partialUpdatedTodo = Todo().apply {
            id = todo.id

            task = UPDATED_TASK
            scheduledTime = UPDATED_SCHEDULED_TIME
            validUntil = UPDATED_VALID_UNTIL
            createdDate = UPDATED_CREATED_DATE
            lastModifiedDate = UPDATED_LAST_MODIFIED_DATE
            createdBy = UPDATED_CREATED_BY
            lastModifiedBy = UPDATED_LAST_MODIFIED_BY
        }

        restTodoMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedTodo.id)
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedTodo))
        )
            .andExpect(status().isOk)

// Validate the Todo in the database
        val todoList = todoRepository.findAll()
        assertThat(todoList).hasSize(databaseSizeBeforeUpdate)
        val testTodo = todoList.last()
        assertThat(testTodo.task).isEqualTo(UPDATED_TASK)
        assertThat(testTodo.scheduledTime).isEqualTo(UPDATED_SCHEDULED_TIME)
        assertThat(testTodo.validUntil).isEqualTo(UPDATED_VALID_UNTIL)
        assertThat(testTodo.createdDate).isEqualTo(UPDATED_CREATED_DATE)
        assertThat(testTodo.lastModifiedDate).isEqualTo(UPDATED_LAST_MODIFIED_DATE)
        assertThat(testTodo.createdBy).isEqualTo(UPDATED_CREATED_BY)
        assertThat(testTodo.lastModifiedBy).isEqualTo(UPDATED_LAST_MODIFIED_BY)
    }

    @Throws(Exception::class)
    fun patchNonExistingTodo() {
        val databaseSizeBeforeUpdate = todoRepository.findAll().size
        todo.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTodoMockMvc.perform(
            patch(ENTITY_API_URL_ID, todo.id)
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(todo))
        )
            .andExpect(status().isBadRequest)

        // Validate the Todo in the database
        val todoList = todoRepository.findAll()
        assertThat(todoList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchTodo() {
        val databaseSizeBeforeUpdate = todoRepository.findAll().size
        todo.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTodoMockMvc.perform(
            patch(ENTITY_API_URL_ID, count.incrementAndGet())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(todo))
        )
            .andExpect(status().isBadRequest)

        // Validate the Todo in the database
        val todoList = todoRepository.findAll()
        assertThat(todoList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamTodo() {
        val databaseSizeBeforeUpdate = todoRepository.findAll().size
        todo.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTodoMockMvc.perform(
            patch(ENTITY_API_URL)
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(todo))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the Todo in the database
        val todoList = todoRepository.findAll()
        assertThat(todoList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteTodo() {
        // Initialize the database
        todoRepository.saveAndFlush(todo)

        val databaseSizeBeforeDelete = todoRepository.findAll().size

        // Delete the todo
        restTodoMockMvc.perform(
            delete(ENTITY_API_URL_ID, todo.id)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val todoList = todoRepository.findAll()
        assertThat(todoList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_TASK = "AAAAAAAAAA"
        private const val UPDATED_TASK = "BBBBBBBBBB"

        private const val DEFAULT_SCHEDULED_TIME = "AAAAAAAAAA"
        private const val UPDATED_SCHEDULED_TIME = "BBBBBBBBBB"

        private val DEFAULT_VALID_UNTIL: Instant = Instant.ofEpochMilli(0L)
        private val UPDATED_VALID_UNTIL: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS)

        private val DEFAULT_CREATED_DATE: Instant = Instant.ofEpochMilli(0L)
        private val UPDATED_CREATED_DATE: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS)

        private val DEFAULT_LAST_MODIFIED_DATE: Instant = Instant.ofEpochMilli(0L)
        private val UPDATED_LAST_MODIFIED_DATE: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS)

        private const val DEFAULT_CREATED_BY: Long = 1L
        private const val UPDATED_CREATED_BY: Long = 2L

        private const val DEFAULT_LAST_MODIFIED_BY: Long = 1L
        private const val UPDATED_LAST_MODIFIED_BY: Long = 2L

        private val ENTITY_API_URL: String = "/api/todos"
        private val ENTITY_API_URL_ID: String = ENTITY_API_URL + "/{id}"

        private val random: Random = Random()
        private val count: AtomicLong = AtomicLong(random.nextInt().toLong() + (2 * Integer.MAX_VALUE))

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Todo {
            val todo = Todo(
                task = DEFAULT_TASK,

                scheduledTime = DEFAULT_SCHEDULED_TIME,

                validUntil = DEFAULT_VALID_UNTIL,

                createdDate = DEFAULT_CREATED_DATE,

                lastModifiedDate = DEFAULT_LAST_MODIFIED_DATE,

                createdBy = DEFAULT_CREATED_BY,

                lastModifiedBy = DEFAULT_LAST_MODIFIED_BY

            )

            // Add required entity
            val user = UserResourceIT.createEntity(em)
            em.persist(user)
            em.flush()
            todo.users?.add(user)
            return todo
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Todo {
            val todo = Todo(
                task = UPDATED_TASK,

                scheduledTime = UPDATED_SCHEDULED_TIME,

                validUntil = UPDATED_VALID_UNTIL,

                createdDate = UPDATED_CREATED_DATE,

                lastModifiedDate = UPDATED_LAST_MODIFIED_DATE,

                createdBy = UPDATED_CREATED_BY,

                lastModifiedBy = UPDATED_LAST_MODIFIED_BY

            )

            // Add required entity
            val user = UserResourceIT.createEntity(em)
            em.persist(user)
            em.flush()
            todo.users?.add(user)
            return todo
        }
    }
}
