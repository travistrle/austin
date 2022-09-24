package xyz.onegrid.austin.repository

import org.springframework.data.jpa.repository.JpaRepository
import xyz.onegrid.austin.domain.Authority

/**
 * Spring Data JPA repository for the [Authority] entity.
 */

interface AuthorityRepository : JpaRepository<Authority, String>
