package xyz.onegrid.austin.config

import org.springdoc.core.GroupedOpenApi
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import tech.jhipster.config.JHipsterConstants
import tech.jhipster.config.JHipsterProperties
import tech.jhipster.config.apidoc.customizer.JHipsterOpenApiCustomizer

@Configuration
@Profile(JHipsterConstants.SPRING_PROFILE_API_DOCS)
class OpenApiConfiguration {

    val API_FIRST_PACKAGE: String = "xyz.onegrid.austin.web.api"

    @Bean
    @ConditionalOnMissingBean(name = ["apiFirstGroupedOpenAPI"])
    fun apiFirstGroupedOpenAPI(
        jhipsterOpenApiCustomizer: JHipsterOpenApiCustomizer,
        jHipsterProperties: JHipsterProperties
    ): GroupedOpenApi {
        val properties = jHipsterProperties.apiDocs
        return GroupedOpenApi.builder()
            .group("openapi")
            .addOpenApiCustomiser(jhipsterOpenApiCustomizer)
            .packagesToScan(API_FIRST_PACKAGE)
            .pathsToMatch(properties.getDefaultIncludePattern())
            .build()
    }
}
