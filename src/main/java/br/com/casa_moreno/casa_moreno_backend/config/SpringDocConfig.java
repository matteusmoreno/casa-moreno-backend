package br.com.casa_moreno.casa_moreno_backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "BearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)

@OpenAPIDefinition(
        info = @Info(
                title = "Casa Moreno API",
                version = "v1",
                description = "API para a plataforma de curadoria de produtos da Casa Moreno"
        ),

        security = @SecurityRequirement(name = "BearerAuth")
)
public class SpringDocConfig {
}