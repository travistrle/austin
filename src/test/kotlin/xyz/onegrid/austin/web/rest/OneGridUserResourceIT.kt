package xyz.onegrid.austin.web.rest

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
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
import xyz.onegrid.austin.domain.OneGridUser
import xyz.onegrid.austin.domain.Todo
import xyz.onegrid.austin.domain.enumeration.Group
import xyz.onegrid.austin.repository.OneGridUserRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Random
import java.util.concurrent.atomic.AtomicLong
import javax.persistence.EntityManager
import kotlin.test.assertNotNull

/**
 * Integration tests for the [OneGridUserResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class OneGridUserResourceIT {
    @Autowired
    private lateinit var oneGridUserRepository: OneGridUserRepository

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var validator: Validator

    @Autowired
    private lateinit var em: EntityManager

    @Autowired
    private lateinit var restOneGridUserMockMvc: MockMvc

    private lateinit var oneGridUser: OneGridUser

    @BeforeEach
    fun initTest() {
        oneGridUser = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createOneGridUser() {
        val databaseSizeBeforeCreate = oneGridUserRepository.findAll().size
        // Create the OneGridUser
        restOneGridUserMockMvc.perform(
            post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(oneGridUser))
        ).andExpect(status().isCreated)

        // Validate the OneGridUser in the database
        val oneGridUserList = oneGridUserRepository.findAll()
        assertThat(oneGridUserList).hasSize(databaseSizeBeforeCreate + 1)
        val testOneGridUser = oneGridUserList[oneGridUserList.size - 1]

        assertThat(testOneGridUser.email).isEqualTo(DEFAULT_EMAIL)
        assertThat(testOneGridUser.dateOfBirth).isEqualTo(DEFAULT_DATE_OF_BIRTH)
        assertThat(testOneGridUser.group).isEqualTo(DEFAULT_GROUP)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createOneGridUserWithExistingId() {
        // Create the OneGridUser with an existing ID
        oneGridUser.id = 1L

        val databaseSizeBeforeCreate = oneGridUserRepository.findAll().size

        // An entity with an existing ID cannot be created, so this API call must fail
        restOneGridUserMockMvc.perform(
            post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(oneGridUser))
        ).andExpect(status().isBadRequest)

        // Validate the OneGridUser in the database
        val oneGridUserList = oneGridUserRepository.findAll()
        assertThat(oneGridUserList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkEmailIsRequired() {
        val databaseSizeBeforeTest = oneGridUserRepository.findAll().size
        // set the field null
        oneGridUser.email = null

        // Create the OneGridUser, which fails.

        restOneGridUserMockMvc.perform(
            post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(oneGridUser))
        ).andExpect(status().isBadRequest)

        val oneGridUserList = oneGridUserRepository.findAll()
        assertThat(oneGridUserList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllOneGridUsers() {
        // Initialize the database
        oneGridUserRepository.saveAndFlush(oneGridUser)

        // Get all the oneGridUserList
        restOneGridUserMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(oneGridUser.id?.toInt())))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].dateOfBirth").value(hasItem(DEFAULT_DATE_OF_BIRTH.toString())))
            .andExpect(jsonPath("$.[*].group").value(hasItem(DEFAULT_GROUP.toString())))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getOneGridUser() {
        // Initialize the database
        oneGridUserRepository.saveAndFlush(oneGridUser)

        val id = oneGridUser.id
        assertNotNull(id)

        // Get the oneGridUser
        restOneGridUserMockMvc.perform(get(ENTITY_API_URL_ID, oneGridUser.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(oneGridUser.id?.toInt()))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.dateOfBirth").value(DEFAULT_DATE_OF_BIRTH.toString()))
            .andExpect(jsonPath("$.group").value(DEFAULT_GROUP.toString()))
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingOneGridUser() {
        // Get the oneGridUser
        restOneGridUserMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putNewOneGridUser() {
        // Initialize the database
        oneGridUserRepository.saveAndFlush(oneGridUser)

        val databaseSizeBeforeUpdate = oneGridUserRepository.findAll().size

        // Update the oneGridUser
        val updatedOneGridUser = oneGridUserRepository.findById(oneGridUser.id).get()
        // Disconnect from session so that the updates on updatedOneGridUser are not directly saved in db
        em.detach(updatedOneGridUser)
        updatedOneGridUser.email = UPDATED_EMAIL
        updatedOneGridUser.dateOfBirth = UPDATED_DATE_OF_BIRTH
        updatedOneGridUser.group = UPDATED_GROUP

        restOneGridUserMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedOneGridUser.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedOneGridUser))
        ).andExpect(status().isOk)

        // Validate the OneGridUser in the database
        val oneGridUserList = oneGridUserRepository.findAll()
        assertThat(oneGridUserList).hasSize(databaseSizeBeforeUpdate)
        val testOneGridUser = oneGridUserList[oneGridUserList.size - 1]
        assertThat(testOneGridUser.email).isEqualTo(UPDATED_EMAIL)
        assertThat(testOneGridUser.dateOfBirth).isEqualTo(UPDATED_DATE_OF_BIRTH)
        assertThat(testOneGridUser.group).isEqualTo(UPDATED_GROUP)
    }

    @Test
    @Transactional
    fun putNonExistingOneGridUser() {
        val databaseSizeBeforeUpdate = oneGridUserRepository.findAll().size
        oneGridUser.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restOneGridUserMockMvc.perform(
            put(ENTITY_API_URL_ID, oneGridUser.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(oneGridUser))
        )
            .andExpect(status().isBadRequest)

        // Validate the OneGridUser in the database
        val oneGridUserList = oneGridUserRepository.findAll()
        assertThat(oneGridUserList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchOneGridUser() {
        val databaseSizeBeforeUpdate = oneGridUserRepository.findAll().size
        oneGridUser.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOneGridUserMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(oneGridUser))
        ).andExpect(status().isBadRequest)

        // Validate the OneGridUser in the database
        val oneGridUserList = oneGridUserRepository.findAll()
        assertThat(oneGridUserList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamOneGridUser() {
        val databaseSizeBeforeUpdate = oneGridUserRepository.findAll().size
        oneGridUser.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOneGridUserMockMvc.perform(
            put(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(oneGridUser))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the OneGridUser in the database
        val oneGridUserList = oneGridUserRepository.findAll()
        assertThat(oneGridUserList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateOneGridUserWithPatch() {
        oneGridUserRepository.saveAndFlush(oneGridUser)

        val databaseSizeBeforeUpdate = oneGridUserRepository.findAll().size

// Update the oneGridUser using partial update
        val partialUpdatedOneGridUser = OneGridUser().apply {
            id = oneGridUser.id

            dateOfBirth = UPDATED_DATE_OF_BIRTH
        }

        restOneGridUserMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedOneGridUser.id)
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedOneGridUser))
        )
            .andExpect(status().isOk)

// Validate the OneGridUser in the database
        val oneGridUserList = oneGridUserRepository.findAll()
        assertThat(oneGridUserList).hasSize(databaseSizeBeforeUpdate)
        val testOneGridUser = oneGridUserList.last()
        assertThat(testOneGridUser.email).isEqualTo(DEFAULT_EMAIL)
        assertThat(testOneGridUser.dateOfBirth).isEqualTo(UPDATED_DATE_OF_BIRTH)
        assertThat(testOneGridUser.group).isEqualTo(DEFAULT_GROUP)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateOneGridUserWithPatch() {
        oneGridUserRepository.saveAndFlush(oneGridUser)

        val databaseSizeBeforeUpdate = oneGridUserRepository.findAll().size

// Update the oneGridUser using partial update
        val partialUpdatedOneGridUser = OneGridUser().apply {
            id = oneGridUser.id

            email = UPDATED_EMAIL
            dateOfBirth = UPDATED_DATE_OF_BIRTH
            group = UPDATED_GROUP
        }

        restOneGridUserMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedOneGridUser.id)
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedOneGridUser))
        )
            .andExpect(status().isOk)

// Validate the OneGridUser in the database
        val oneGridUserList = oneGridUserRepository.findAll()
        assertThat(oneGridUserList).hasSize(databaseSizeBeforeUpdate)
        val testOneGridUser = oneGridUserList.last()
        assertThat(testOneGridUser.email).isEqualTo(UPDATED_EMAIL)
        assertThat(testOneGridUser.dateOfBirth).isEqualTo(UPDATED_DATE_OF_BIRTH)
        assertThat(testOneGridUser.group).isEqualTo(UPDATED_GROUP)
    }

    @Throws(Exception::class)
    fun patchNonExistingOneGridUser() {
        val databaseSizeBeforeUpdate = oneGridUserRepository.findAll().size
        oneGridUser.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restOneGridUserMockMvc.perform(
            patch(ENTITY_API_URL_ID, oneGridUser.id)
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(oneGridUser))
        )
            .andExpect(status().isBadRequest)

        // Validate the OneGridUser in the database
        val oneGridUserList = oneGridUserRepository.findAll()
        assertThat(oneGridUserList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchOneGridUser() {
        val databaseSizeBeforeUpdate = oneGridUserRepository.findAll().size
        oneGridUser.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOneGridUserMockMvc.perform(
            patch(ENTITY_API_URL_ID, count.incrementAndGet())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(oneGridUser))
        )
            .andExpect(status().isBadRequest)

        // Validate the OneGridUser in the database
        val oneGridUserList = oneGridUserRepository.findAll()
        assertThat(oneGridUserList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamOneGridUser() {
        val databaseSizeBeforeUpdate = oneGridUserRepository.findAll().size
        oneGridUser.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOneGridUserMockMvc.perform(
            patch(ENTITY_API_URL)
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(oneGridUser))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the OneGridUser in the database
        val oneGridUserList = oneGridUserRepository.findAll()
        assertThat(oneGridUserList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteOneGridUser() {
        // Initialize the database
        oneGridUserRepository.saveAndFlush(oneGridUser)

        val databaseSizeBeforeDelete = oneGridUserRepository.findAll().size

        // Delete the oneGridUser
        restOneGridUserMockMvc.perform(
            delete(ENTITY_API_URL_ID, oneGridUser.id)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val oneGridUserList = oneGridUserRepository.findAll()
        assertThat(oneGridUserList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_EMAIL = "AAAAAAAAAA"
        private const val UPDATED_EMAIL = "BBBBBBBBBB"

        private val DEFAULT_DATE_OF_BIRTH: Instant = Instant.ofEpochMilli(0L)
        private val UPDATED_DATE_OF_BIRTH: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS)

        private val DEFAULT_GROUP: Group = Group.ADMIN
        private val UPDATED_GROUP: Group = Group.MODERATOR

        private val ENTITY_API_URL: String = "/api/one-grid-users"
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
        fun createEntity(em: EntityManager): OneGridUser {
            val oneGridUser = OneGridUser(
                email = DEFAULT_EMAIL,

                dateOfBirth = DEFAULT_DATE_OF_BIRTH,

                group = DEFAULT_GROUP

            )

            // Add required entity
            val todo: Todo
            if (findAll(em, Todo::class).isEmpty()) {
                todo = TodoResourceIT.createEntity(em)
                em.persist(todo)
                em.flush()
            } else {
                todo = findAll(em, Todo::class)[0]
            }
            oneGridUser.todos?.add(todo)
            return oneGridUser
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): OneGridUser {
            val oneGridUser = OneGridUser(
                email = UPDATED_EMAIL,

                dateOfBirth = UPDATED_DATE_OF_BIRTH,

                group = UPDATED_GROUP

            )

            // Add required entity
            val todo: Todo
            if (findAll(em, Todo::class).isEmpty()) {
                todo = TodoResourceIT.createUpdatedEntity(em)
                em.persist(todo)
                em.flush()
            } else {
                todo = findAll(em, Todo::class)[0]
            }
            oneGridUser.todos?.add(todo)
            return oneGridUser
        }
    }
}
