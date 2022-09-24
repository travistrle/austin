package xyz.onegrid.austin.config

import org.mockito.Mockito
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import xyz.onegrid.austin.domain.User
import xyz.onegrid.austin.service.MailService

@Configuration
class NoOpMailConfiguration {
    private val mockMailService = Mockito.mock(MailService::class.java)

    @Bean
    fun mailService(): MailService {
        return mockMailService
    }

    init {
        Mockito.doNothing().`when`(mockMailService).sendActivationEmail(User())
    }
}
