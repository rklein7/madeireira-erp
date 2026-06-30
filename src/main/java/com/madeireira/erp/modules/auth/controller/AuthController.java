package com.madeireira.erp.modules.auth.controller;

import com.madeireira.erp.modules.auth.dto.AuthDTO;
import com.madeireira.erp.modules.auth.service.AuthService;
import com.madeireira.erp.shared.exception.GlobalExceptionHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Login e registro de usuários. Endpoints públicos — não requerem token JWT.")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
        summary = "Autenticar usuário",
        description = "Valida e-mail e senha e retorna um token JWT com validade de 24 horas. " +
                      "Use o token retornado no header `Authorization: Bearer <token>` para acessar os demais endpoints. " +
                      "Clique em **Authorize** no topo da página para inserir o token no Swagger UI."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Autenticação bem-sucedida — token JWT retornado"),
        @ApiResponse(responseCode = "400", description = "Credenciais inválidas ou campos obrigatórios ausentes",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "E-mail ou senha incorretos",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<AuthDTO.LoginResponse> login(@Valid @RequestBody AuthDTO.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/registrar")
    @Operation(
        summary = "Registrar novo usuário",
        description = "Cria um novo usuário no sistema e retorna o token JWT imediatamente. " +
                      "O e-mail deve ser único. Se `perfil` não for informado, assume `OPERADOR`. " +
                      "Perfis disponíveis: `ADMIN`, `GERENTE`, `OPERADOR`, `FINANCEIRO`."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuário criado e token JWT retornado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou e-mail já cadastrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<AuthDTO.LoginResponse> registrar(@Valid @RequestBody AuthDTO.RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }
}
