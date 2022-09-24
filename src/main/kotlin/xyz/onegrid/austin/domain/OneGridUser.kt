package xyz.onegrid.austin.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import xyz.onegrid.austin.domain.enumeration.Group
import java.io.Serializable
import java.time.Instant
import javax.persistence.*
import javax.validation.constraints.*

/**
 * A OneGridUser.
 */

@Entity
@Table(name = "one_grid_user")

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
data class OneGridUser(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    var id: Long? = null,

    @get: NotNull

    @Column(name = "email", nullable = false)
    var email: String? = null,

    @Column(name = "date_of_birth")
    var dateOfBirth: Instant? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "jhi_group")
    var group: Group? = null,

    @ManyToMany(mappedBy = "users")

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(
        value = [
            "users",
        ],
        allowSetters = true
    )
    var todos: MutableSet<Todo>? = mutableSetOf(),
    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {

    fun addTodos(todo: Todo): OneGridUser {
        this.todos?.add(todo)
        todo.users?.add(this)
        return this
    }
    fun removeTodos(todo: Todo): OneGridUser {
        this.todos?.remove(todo)
        todo.users?.remove(this)
        return this
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OneGridUser) return false
        return id != null && other.id != null && id == other.id
    }

    @Override
    override fun toString(): String {
        return "OneGridUser{" +
            "id=" + id +
            ", email='" + email + "'" +
            ", dateOfBirth='" + dateOfBirth + "'" +
            ", group='" + group + "'" +
            "}"
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
