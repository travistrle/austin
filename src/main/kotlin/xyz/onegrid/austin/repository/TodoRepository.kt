package xyz.onegrid.austin.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import xyz.onegrid.austin.domain.Todo
import java.util.Optional

/**
 * Spring Data SQL repository for the [Todo] entity.
 */
@Repository
interface TodoRepository : TodoRepositoryWithBagRelationships, JpaRepository<Todo, Long> {

    @JvmDefault fun findOneWithEagerRelationships(id: Long): Optional<Todo> {
        return this.fetchBagRelationships(this.findById(id))
    }

    @JvmDefault fun findAllWithEagerRelationships(): MutableList<Todo> {
        return this.fetchBagRelationships(this.findAll())
    }

    @JvmDefault fun findAllWithEagerRelationships(pageable: Pageable): Page<Todo> {
        return this.fetchBagRelationships(this.findAll(pageable))
    }
}
