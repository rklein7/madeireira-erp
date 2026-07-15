package com.madeireira.erp.modules.compras.controller;

import com.madeireira.erp.modules.compras.dto.PedidoCompraDTO;
import com.madeireira.erp.modules.compras.entity.StatusPedidoCompra;
import com.madeireira.erp.modules.compras.service.ComprasService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pedidos-compra")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Compras", description = "Gestão de pedidos de compra — ciclo: RASCUNHO → CONFIRMADO → RECEBIDO")
public class ComprasController {

    private final ComprasService comprasService;

    @GetMapping
    @Operation(
        summary = "Listar pedidos de compra",
        description = "Lista com filtros opcionais por fornecedor, status e semNfVinculada " +
                      "(pedidos CONFIRMADOS sem NF de entrada ativa vinculada)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<Page<PedidoCompraDTO.Resumo>> listar(
            @RequestParam(required = false) UUID fornecedorId,
            @RequestParam(required = false) StatusPedidoCompra status,
            @RequestParam(required = false) Boolean semNfVinculada,
            @PageableDefault(size = 20, sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(comprasService.listar(fornecedorId, status, semNfVinculada, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido de compra por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<PedidoCompraDTO.Response> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(comprasService.buscarPorId(id));
    }

    @PostMapping
    @Operation(
        summary = "Criar pedido de compra",
        description = "Cria um pedido de compra em RASCUNHO. O número é gerado automaticamente no formato " +
                      "CMP-AAAA-NNNNN. O preço unitário deve ser informado pelo usuário (preço negociado com fornecedor)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pedido de compra criado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou fornecedor/produto não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<PedidoCompraDTO.Response> criar(@Valid @RequestBody PedidoCompraDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(comprasService.criar(request));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar pedido de compra",
        description = "Substitui todos os dados do pedido. Somente permitido em RASCUNHO."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido atualizado"),
        @ApiResponse(responseCode = "400", description = "Pedido não está em RASCUNHO ou dados inválidos",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<PedidoCompraDTO.Response> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody PedidoCompraDTO.Request request) {
        return ResponseEntity.ok(comprasService.atualizar(id, request));
    }

    @PatchMapping("/{id}/confirmar")
    @Operation(
        summary = "Confirmar pedido de compra",
        description = "Avança o pedido de RASCUNHO para CONFIRMADO. Nenhum estoque ou conta é movimentado neste momento."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido confirmado"),
        @ApiResponse(responseCode = "400", description = "Pedido não está em RASCUNHO",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<PedidoCompraDTO.Response> confirmar(@PathVariable UUID id) {
        return ResponseEntity.ok(comprasService.confirmarPedido(id));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(
        summary = "Cancelar pedido de compra",
        description = "Cancela um pedido em RASCUNHO ou CONFIRMADO."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido cancelado"),
        @ApiResponse(responseCode = "400", description = "Pedido em status que não permite cancelamento",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<PedidoCompraDTO.Response> cancelar(@PathVariable UUID id) {
        return ResponseEntity.ok(comprasService.cancelarPedido(id));
    }
}
