package xyz.onegrid.austin.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.io.Serializable
import java.time.Instant
import javax.persistence.*
import javax.validation.constraints.*

/**
 * A Todo.
 */

@Entity
@Table(name = "todo")

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
data class Todo(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    var id: Long? = null,

    @get: NotNull

    @Column(name = "task", nullable = false)
    var task: String? = null,

    @get: NotNull

    @Column(name = "scheduled_time", nullable = false)
    var scheduledTime: String? = null,

    @Column(name = "valid_until")
    var validUntil: Instant? = null,

    @Column(name = "created_date")
    var createdDate: Instant? = null,

    @Column(name = "last_modified_date")
    var lastModifiedDate: Instant? = null,

    @Column(name = "created_by")
    var createdBy: Long? = null,

    @Column(name = "last_modified_by")
    var lastModifiedBy: Long? = null,

    @ManyToMany
    @NotNull
    @JoinTable(
        name = "rel_todo__users",
        joinColumns = [
            JoinColumn(name = "todo_id")
        ],
        inverseJoinColumns = [
            JoinColumn(name = "users_id")
        ]
    )

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(
        value = [
            "todos",
        ],
        allowSetters = true
    )
    var users: MutableSet<OneGridUser>? = mutableSetOf(),
    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {

    fun addUsers(oneGridUser: OneGridUser): Todo {
        this.users?.add(oneGridUser)
        oneGridUser.todos?.add(this)
        return this
    }
    fun removeUsers(oneGridUser: OneGridUser): Todo {
        this.users?.remove(oneGridUser)
        oneGridUser.todos?.remove(this)
        return this
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Todo) return false
        return id != null && other.id != null && id == other.id
    }

    @Override
    override fun toString(): String {
        return "Todo{" +
            "id=" + id +
            ", task='" + task + "'" +
            ", scheduledTime='" + scheduledTime + "'" +
            ", validUntil='" + validUntil + "'" +
            ", createdDate='" + createdDate + "'" +
            ", lastModifiedDate='" + lastModifiedDate + "'" +
            ", createdBy=" + createdBy +
            ", lastModifiedBy=" + lastModifiedBy +
            "}"
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
