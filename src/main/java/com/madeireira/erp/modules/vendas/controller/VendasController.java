package com.madeireira.erp.modules.vendas.controller;

import com.madeireira.erp.modules.vendas.dto.PedidoDTO;
import com.madeireira.erp.modules.vendas.entity.StatusPedido;
import com.madeireira.erp.modules.vendas.service.VendasService;
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
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Vendas", description = "Gestão de pedidos de venda — ciclo: RASCUNHO → CONFIRMADO → FATURADO → ENTREGUE")
public class VendasController {

    private final VendasService vendasService;

    @GetMapping
    @Operation(
        summary = "Listar pedidos",
        description = "Lista pedidos com filtros opcionais por cliente e status. Ordenação padrão: mais recentes primeiro."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<Page<PedidoDTO.Resumo>> listar(
            @RequestParam(required = false) UUID clienteId,
            @RequestParam(required = false) StatusPedido status,
            @PageableDefault(size = 20, sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(vendasService.listar(clienteId, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<PedidoDTO.Response> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(vendasService.buscarPorId(id));
    }

    @PostMapping
    @Operation(
        summary = "Criar pedido",
        description = "Cria um pedido em status RASCUNHO. O número é gerado automaticamente no formato " +
                      "PED-AAAA-NNNNN. O preço unitário é um snapshot do `precoVenda` do produto no momento da criação."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou produto/cliente não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<PedidoDTO.Response> criar(@Valid @RequestBody PedidoDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vendasService.criar(request));
    }

    @PatchMapping("/{id}/confirmar")
    @Operation(
        summary = "Confirmar pedido",
        description = "Confirma um pedido em RASCUNHO: valida o estoque de todos os itens " +
                      "antes de baixar qualquer quantidade, garantindo atomicidade."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido confirmado — estoque baixado"),
        @ApiResponse(responseCode = "400",
            description = "Estoque insuficiente para um ou mais produtos, ou status inválido para confirmação",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<PedidoDTO.Response> confirmar(@PathVariable UUID id) {
        return ResponseEntity.ok(vendasService.confirmarPedido(id));
    }

    @PatchMapping("/{id}/faturar")
    @Operation(
        summary = "Faturar pedido",
        description = "Avança o pedido de CONFIRMADO para FATURADO. " +
                      "A integração com Contas a Receber será implementada na Sprint 5."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido faturado"),
        @ApiResponse(responseCode = "400", description = "Pedido não está em status CONFIRMADO",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<PedidoDTO.Response> faturar(@PathVariable UUID id) {
        return ResponseEntity.ok(vendasService.faturarPedido(id));
    }

    @PatchMapping("/{id}/entregar")
    @Operation(summary = "Marcar pedido como entregue", description = "Avança o pedido de FATURADO para ENTREGUE.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido marcado como entregue"),
        @ApiResponse(responseCode = "400", description = "Pedido não está em status FATURADO",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<PedidoDTO.Response> entregar(@PathVariable UUID id) {
        return ResponseEntity.ok(vendasService.entregarPedido(id));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(
        summary = "Cancelar pedido",
        description = "Cancela um pedido em RASCUNHO ou CONFIRMADO. " +
                      "Se estava CONFIRMADO, o estoque é estornado automaticamente para cada item."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido cancelado — estoque estornado se aplicável"),
        @ApiResponse(responseCode = "400", description = "Pedido em status que não permite cancelamento (FATURADO, ENTREGUE ou já CANCELADO)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<PedidoDTO.Response> cancelar(@PathVariable UUID id) {
        return ResponseEntity.ok(vendasService.cancelarPedido(id));
    }
}
