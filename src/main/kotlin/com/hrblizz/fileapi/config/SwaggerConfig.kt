package com.hrblizz.fileapi.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun api(): OpenAPI {
        return OpenAPI()
            .info(
                io.swagger.v3.oas.models.info.Info()
                    .title("File API")
                    .description("API for file management")
                    .version("1.0")
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        "basicScheme",
                        SecurityScheme().type(SecurityScheme.Type.HTTP)
                            .scheme("basic")
                    )
            )
            .addSecurityItem(SecurityRequirement().addList("basicScheme"))
    }

}
