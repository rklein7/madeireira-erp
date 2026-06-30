package com.madeireira.erp.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

public class AuthDTO {

    @Schema(description = "Credenciais para autenticação")
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LoginRequest {

        @Schema(description = "E-mail cadastrado no sistema", example = "admin@madeireira.com")
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        private String email;

        @Schema(description = "Senha do usuário", example = "senha123")
        @NotBlank(message = "Senha é obrigatória")
        private String senha;
    }

    @Schema(description = "Token JWT e dados do usuário autenticado")
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LoginResponse {
        @Schema(description = "Token JWT para uso no header Authorization: Bearer <token>")
        private String token;
        private String nome;
        private String email;
        @Schema(description = "Perfil do usuário: ADMIN, GERENTE, OPERADOR ou FINANCEIRO")
        private String perfil;
    }

    @Schema(description = "Dados para registro de novo usuário")
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RegisterRequest {

        @Schema(description = "Nome completo do usuário", example = "João da Silva")
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150)
        private String nome;

        @Schema(description = "E-mail único usado como login", example = "joao@madeireira.com")
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 150)
        private String email;

        @Schema(description = "Senha com no mínimo 6 caracteres", example = "senha123")
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        private String senha;

        @Schema(description = "Perfil de acesso: ADMIN, GERENTE, OPERADOR ou FINANCEIRO. " +
                "Se não informado, assume OPERADOR.", example = "OPERADOR")
        @Pattern(regexp = "ADMIN|GERENTE|OPERADOR|FINANCEIRO",
                 message = "Perfil deve ser ADMIN, GERENTE, OPERADOR ou FINANCEIRO")
        private String perfil;
    }
}
