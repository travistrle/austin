package xyz.onegrid.austin.security

import org.springframework.data.domain.AuditorAware
import org.springframework.stereotype.Component
import xyz.onegrid.austin.config.SYSTEM_ACCOUNT
import java.util.Optional

/**
 * Implementation of [AuditorAware] based on Spring Security.
 */
@Component
class SpringSecurityAuditorAware : AuditorAware<String> {
    override fun getCurrentAuditor(): Optional<String> = Optional.of(getCurrentUserLogin().orElse(SYSTEM_ACCOUNT))
}
