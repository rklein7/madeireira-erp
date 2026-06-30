package com.madeireira.erp.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ERP Madeireira API")
                        .version("1.0.0")
                        .description("""
                                API REST para gestão operacional de madeireira.

                                **Módulos disponíveis:** Produtos, Clientes, Fornecedores, Tabelas de Preço.

                                **Autenticação:** todos os endpoints (exceto `/api/v1/auth/**`) exigem \
                                token JWT no header `Authorization: Bearer <token>`. \
                                Obtenha o token via `POST /api/v1/auth/login` e clique em **Authorize** acima.
                                """)
                        .contact(new Contact()
                                .name("Equipe ERP Madeireira")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Cole aqui o token JWT obtido em POST /api/v1/auth/login")));
    }
}
