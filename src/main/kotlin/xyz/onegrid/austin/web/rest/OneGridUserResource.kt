package xyz.onegrid.austin.web.rest

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import tech.jhipster.web.util.HeaderUtil
import tech.jhipster.web.util.ResponseUtil
import xyz.onegrid.austin.domain.OneGridUser
import xyz.onegrid.austin.repository.OneGridUserRepository
import xyz.onegrid.austin.web.rest.errors.BadRequestAlertException
import java.net.URI
import java.net.URISyntaxException
import java.util.Objects
import javax.validation.Valid
import javax.validation.constraints.NotNull

private const val ENTITY_NAME = "austinOneGridUser"
/**
 * REST controller for managing [xyz.onegrid.austin.domain.OneGridUser].
 */
@RestController
@RequestMapping("/api")
@Transactional
class OneGridUserResource(
    private val oneGridUserRepository: OneGridUserRepository,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "austinOneGridUser"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /one-grid-users` : Create a new oneGridUser.
     *
     * @param oneGridUser the oneGridUser to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new oneGridUser, or with status `400 (Bad Request)` if the oneGridUser has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/one-grid-users")
    fun createOneGridUser(@Valid @RequestBody oneGridUser: OneGridUser): ResponseEntity<OneGridUser> {
        log.debug("REST request to save OneGridUser : $oneGridUser")
        if (oneGridUser.id != null) {
            throw BadRequestAlertException(
                "A new oneGridUser cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = oneGridUserRepository.save(oneGridUser)
        return ResponseEntity.created(URI("/api/one-grid-users/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /one-grid-users/:id} : Updates an existing oneGridUser.
     *
     * @param id the id of the oneGridUser to save.
     * @param oneGridUser the oneGridUser to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated oneGridUser,
     * or with status `400 (Bad Request)` if the oneGridUser is not valid,
     * or with status `500 (Internal Server Error)` if the oneGridUser couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/one-grid-users/{id}")
    fun updateOneGridUser(
        @PathVariable(value = "id", required = false) id: Long,
        @Valid @RequestBody oneGridUser: OneGridUser
    ): ResponseEntity<OneGridUser> {
        log.debug("REST request to update OneGridUser : {}, {}", id, oneGridUser)
        if (oneGridUser.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, oneGridUser.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!oneGridUserRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = oneGridUserRepository.save(oneGridUser)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME,
                    oneGridUser.id.toString()
                )
            )
            .body(result)
    }

    /**
     * {@code PATCH  /one-grid-users/:id} : Partial updates given fields of an existing oneGridUser, field will ignore if it is null
     *
     * @param id the id of the oneGridUser to save.
     * @param oneGridUser the oneGridUser to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated oneGridUser,
     * or with status {@code 400 (Bad Request)} if the oneGridUser is not valid,
     * or with status {@code 404 (Not Found)} if the oneGridUser is not found,
     * or with status {@code 500 (Internal Server Error)} if the oneGridUser couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = ["/one-grid-users/{id}"], consumes = ["application/json", "application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateOneGridUser(
        @PathVariable(value = "id", required = false) id: Long,
        @NotNull @RequestBody oneGridUser: OneGridUser
    ): ResponseEntity<OneGridUser> {
        log.debug("REST request to partial update OneGridUser partially : {}, {}", id, oneGridUser)
        if (oneGridUser.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, oneGridUser.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!oneGridUserRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = oneGridUserRepository.findById(oneGridUser.id)
            .map {

                if (oneGridUser.email != null) {
                    it.email = oneGridUser.email
                }
                if (oneGridUser.dateOfBirth != null) {
                    it.dateOfBirth = oneGridUser.dateOfBirth
                }
                if (oneGridUser.group != null) {
                    it.group = oneGridUser.group
                }

                it
            }
            .map { oneGridUserRepository.save(it) }

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, oneGridUser.id.toString())
        )
    }

    /**
     * `GET  /one-grid-users` : get all the oneGridUsers.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of oneGridUsers in body.
     */
    @GetMapping("/one-grid-users")
    fun getAllOneGridUsers(): MutableList<OneGridUser> {

        log.debug("REST request to get all OneGridUsers")
        return oneGridUserRepository.findAll()
    }

    /**
     * `GET  /one-grid-users/:id` : get the "id" oneGridUser.
     *
     * @param id the id of the oneGridUser to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the oneGridUser, or with status `404 (Not Found)`.
     */
    @GetMapping("/one-grid-users/{id}")
    fun getOneGridUser(@PathVariable id: Long): ResponseEntity<OneGridUser> {
        log.debug("REST request to get OneGridUser : $id")
        val oneGridUser = oneGridUserRepository.findById(id)
        return ResponseUtil.wrapOrNotFound(oneGridUser)
    }
    /**
     *  `DELETE  /one-grid-users/:id` : delete the "id" oneGridUser.
     *
     * @param id the id of the oneGridUser to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/one-grid-users/{id}")
    fun deleteOneGridUser(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete OneGridUser : $id")

        oneGridUserRepository.deleteById(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }
}
