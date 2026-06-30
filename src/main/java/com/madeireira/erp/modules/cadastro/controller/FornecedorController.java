package com.madeireira.erp.modules.cadastro.controller;

import com.madeireira.erp.modules.cadastro.dto.FornecedorDTO;
import com.madeireira.erp.modules.cadastro.service.FornecedorService;
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
@RequestMapping("/api/v1/fornecedores")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Fornecedores", description = "Gestão de fornecedores de madeira e insumos")
public class FornecedorController {

    private final FornecedorService fornecedorService;

    @GetMapping
    @Operation(
        summary = "Listar fornecedores",
        description = "Retorna lista paginada de fornecedores ativos. " +
                      "Use `?busca=termo` para filtrar por razão social ou CPF/CNPJ. " +
                      "Ordenação padrão por razão social."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<Page<FornecedorDTO.Resumo>> listar(
            @RequestParam(required = false) String busca,
            @PageableDefault(size = 20, sort = "razaoSocial") Pageable pageable) {
        return ResponseEntity.ok(fornecedorService.listar(busca, pageable));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar fornecedor por ID",
        description = "Retorna todos os dados do fornecedor, incluindo contato comercial e prazo de entrega padrão."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Fornecedor encontrado"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<FornecedorDTO.Response> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(fornecedorService.buscarPorId(id));
    }

    @PostMapping
    @Operation(
        summary = "Cadastrar fornecedor",
        description = "Cadastra um novo fornecedor. O CPF/CNPJ deve ser único. " +
                      "O campo `contato` deve conter o nome do representante comercial para agilizar pedidos."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Fornecedor cadastrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou CPF/CNPJ duplicado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<FornecedorDTO.Response> criar(@Valid @RequestBody FornecedorDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fornecedorService.criar(request));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar fornecedor",
        description = "Atualiza os dados do fornecedor. " +
                      "O CPF/CNPJ pode ser alterado desde que não pertença a outro fornecedor ativo."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Fornecedor atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou CPF/CNPJ já cadastrado em outro fornecedor",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<FornecedorDTO.Response> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody FornecedorDTO.Request request) {
        return ResponseEntity.ok(fornecedorService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Inativar fornecedor",
        description = "Inativação lógica: o fornecedor é marcado como `ativo = false`. " +
                      "O registro permanece no banco para preservar o histórico de compras."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Fornecedor inativado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<Void> inativar(@PathVariable UUID id) {
        fornecedorService.inativar(id);
        return ResponseEntity.noContent().build();
    }
}
