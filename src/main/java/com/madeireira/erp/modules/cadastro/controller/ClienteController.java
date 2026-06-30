package com.madeireira.erp.modules.cadastro.controller;

import com.madeireira.erp.modules.cadastro.dto.ClienteDTO;
import com.madeireira.erp.modules.cadastro.service.ClienteService;
import com.madeireira.erp.shared.exception.GlobalExceptionHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Clientes", description = "Gestão de clientes da madeireira")
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    @Operation(
        summary = "Listar clientes",
        description = "Retorna lista paginada de clientes ativos. " +
                      "Use `?busca=termo` para filtrar por razão social, nome fantasia ou CPF/CNPJ. " +
                      "Ordenação padrão por razão social."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<Page<ClienteDTO.Resumo>> listar(
            @RequestParam(required = false) String busca,
            @PageableDefault(size = 20, sort = "razaoSocial") Pageable pageable) {
        return ResponseEntity.ok(clienteService.listar(busca, pageable));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar cliente por ID",
        description = "Retorna todos os dados do cliente, incluindo endereço, limite de crédito e tabela de preço vinculada."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<ClienteDTO.Response> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    @PostMapping
    @Operation(
        summary = "Cadastrar cliente",
        description = "Cadastra um novo cliente. O CPF/CNPJ deve ser único no sistema. " +
                      "Se `tabelaPrecoId` não for informado, o cliente utilizará a tabela padrão no momento do pedido."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Cliente cadastrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou CPF/CNPJ duplicado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<ClienteDTO.Response> criar(@Valid @RequestBody ClienteDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.criar(request));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar cliente",
        description = "Atualiza os dados cadastrais do cliente. " +
                      "O CPF/CNPJ pode ser alterado desde que não pertença a outro cliente ativo."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou CPF/CNPJ já cadastrado em outro cliente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<ClienteDTO.Response> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ClienteDTO.Request request) {
        return ResponseEntity.ok(clienteService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Inativar cliente",
        description = "Inativação lógica: o cliente é marcado como `ativo = false` e não aparece " +
                      "nas listagens, mas permanece no banco preservando o histórico de pedidos e financeiro."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Cliente inativado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<Void> inativar(@PathVariable UUID id) {
        clienteService.inativar(id);
        return ResponseEntity.noContent().build();
    }
}
