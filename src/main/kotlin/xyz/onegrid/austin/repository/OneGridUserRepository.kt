package xyz.onegrid.austin.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import xyz.onegrid.austin.domain.OneGridUser

/**
 * Spring Data SQL repository for the [OneGridUser] entity.
 */
@Suppress("unused")
@Repository
interface OneGridUserRepository : JpaRepository<OneGridUser, Long>
