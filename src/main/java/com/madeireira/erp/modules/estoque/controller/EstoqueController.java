package com.madeireira.erp.modules.estoque.controller;

import com.madeireira.erp.modules.estoque.dto.MovimentoEstoqueDTO;
import com.madeireira.erp.modules.estoque.entity.TipoMovimento;
import com.madeireira.erp.modules.estoque.service.EstoqueService;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/estoque")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Estoque", description = "Movimentações de estoque e consulta de saldos")
public class EstoqueController {

    private final EstoqueService estoqueService;

    @PostMapping("/movimentos")
    @Operation(
        summary = "Registrar movimento de estoque",
        description = "Registra uma entrada, saída ou ajuste de estoque de forma atômica: " +
                      "atualiza `estoqueAtual` do produto e grava o histórico em uma única transação. " +
                      "Para `AJUSTE`, a `quantidade` representa o **novo saldo absoluto** do produto " +
                      "(ex: quantidade=50 significa \"o saldo correto agora é 50\")."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Movimento registrado com sucesso"),
        @ApiResponse(responseCode = "400",
            description = "Dados inválidos, estoque insuficiente para saída, ou produto/fornecedor não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<MovimentoEstoqueDTO.Response> registrarMovimento(
            @Valid @RequestBody MovimentoEstoqueDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(estoqueService.registrarMovimento(request));
    }

    @GetMapping("/movimentos")
    @Operation(
        summary = "Histórico de movimentos por produto",
        description = "Retorna os movimentos de um produto ordenados por data decrescente. " +
                      "Filtros opcionais: `tipo` (ENTRADA_MANUAL, SAIDA_MANUAL, AJUSTE), " +
                      "`de` e `ate` (formato ISO: 2025-01-15). " +
                      "Quando `de` e `ate` são informados, o filtro de `tipo` é ignorado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<Page<MovimentoEstoqueDTO.Response>> listarMovimentos(
            @RequestParam UUID produtoId,
            @RequestParam(required = false) TipoMovimento tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @PageableDefault(size = 20, sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(estoqueService.listarMovimentos(produtoId, tipo, de, ate, pageable));
    }

    @GetMapping("/saldo/{produtoId}")
    @Operation(
        summary = "Consultar saldo atual de um produto",
        description = "Retorna o estoque atual, mínimo, indicador de alerta e dados do último movimento registrado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Saldo retornado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<MovimentoEstoqueDTO.SaldoProduto> buscarSaldo(@PathVariable UUID produtoId) {
        return ResponseEntity.ok(estoqueService.buscarSaldo(produtoId));
    }

    @GetMapping("/posicao")
    @Operation(
        summary = "Posição de estoque de todos os produtos",
        description = "Lista todos os produtos ativos com seus saldos atuais, mínimos, máximos e indicador " +
                      "`abaixoDoMinimo`. Use para relatórios de inventário e planejamento de compras."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Posição de estoque retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<Page<MovimentoEstoqueDTO.PosicaoEstoque>> posicaoEstoque(
            @PageableDefault(size = 50, sort = "descricao") Pageable pageable) {
        return ResponseEntity.ok(estoqueService.posicaoEstoque(pageable));
    }
}
