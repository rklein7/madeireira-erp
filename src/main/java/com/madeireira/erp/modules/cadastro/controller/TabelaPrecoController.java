package com.madeireira.erp.modules.cadastro.controller;

import com.madeireira.erp.modules.cadastro.dto.TabelaPrecoDTO;
import com.madeireira.erp.modules.cadastro.service.TabelaPrecoService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tabelas-preco")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tabelas de Preço", description = "Gestão de tabelas de preço e seus itens por produto")
public class TabelaPrecoController {

    private final TabelaPrecoService tabelaPrecoService;

    @GetMapping
    @Operation(
        summary = "Listar tabelas de preço ativas",
        description = "Retorna todas as tabelas de preço com `ativo = true`, ordenadas por nome. " +
                      "O campo `quantidadeItens` indica quantos produtos estão vinculados à tabela."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<List<TabelaPrecoDTO.Resumo>> listar() {
        return ResponseEntity.ok(tabelaPrecoService.listar());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar tabela de preço por ID",
        description = "Retorna a tabela com a lista completa de itens (produtos, preços, descontos e vigências). " +
                      "Use este endpoint para exibir a tabela de preços completa ao vendedor."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tabela encontrada com seus itens"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Tabela de preço não encontrada",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<TabelaPrecoDTO.Response> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(tabelaPrecoService.buscarPorId(id));
    }

    @PostMapping
    @Operation(
        summary = "Criar tabela de preço",
        description = "Cria uma nova tabela de preço vazia. Após criar, adicione produtos via `POST /{id}/itens`."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tabela criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<TabelaPrecoDTO.Response> criar(@Valid @RequestBody TabelaPrecoDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tabelaPrecoService.criar(request));
    }

    @PostMapping("/{id}/itens")
    @Operation(
        summary = "Adicionar produto à tabela",
        description = "Vincula um produto com preço específico à tabela. " +
                      "Valida que o produto existe e que não há duplicidade (mesmo produto só pode aparecer uma vez por tabela). " +
                      "As datas de vigência são opcionais e servem para planejar reajustes futuros."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Item adicionado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Produto não encontrado, produto duplicado na tabela, ou dados inválidos",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Tabela de preço não encontrada",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<TabelaPrecoDTO.ItemResponse> adicionarItem(
            @PathVariable UUID id,
            @Valid @RequestBody TabelaPrecoDTO.ItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tabelaPrecoService.adicionarItem(id, request));
    }

    @DeleteMapping("/{id}/itens/{itemId}")
    @Operation(
        summary = "Remover produto da tabela",
        description = "Remove um item da tabela de preço. O produto continua cadastrado no sistema, " +
                      "apenas sua entrada nesta tabela específica é excluída."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Item removido com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Tabela ou item não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<Void> removerItem(@PathVariable UUID id, @PathVariable UUID itemId) {
        tabelaPrecoService.removerItem(id, itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Inativar tabela de preço",
        description = "Inativação lógica da tabela: marcada como `ativo = false` e removida das listagens. " +
                      "Clientes vinculados a esta tabela precisam ser migrados para outra tabela manualmente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Tabela inativada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Tabela de preço não encontrada",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<Void> inativar(@PathVariable UUID id) {
        tabelaPrecoService.inativar(id);
        return ResponseEntity.noContent().build();
    }
}
