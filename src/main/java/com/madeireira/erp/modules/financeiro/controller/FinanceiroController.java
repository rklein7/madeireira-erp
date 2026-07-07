package com.madeireira.erp.modules.financeiro.controller;

import com.madeireira.erp.modules.financeiro.dto.ContaPagarDTO;
import com.madeireira.erp.modules.financeiro.dto.ContaReceberDTO;
import com.madeireira.erp.modules.financeiro.dto.FinanceiroDTO;
import com.madeireira.erp.modules.financeiro.dto.FluxoCaixaDTO;
import com.madeireira.erp.modules.financeiro.entity.StatusConta;
import com.madeireira.erp.modules.financeiro.service.FinanceiroService;
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
@RequestMapping("/api/v1/financeiro")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Financeiro", description = "Contas a receber, contas a pagar e fluxo de caixa")
public class FinanceiroController {

    private final FinanceiroService financeiroService;

    // -------------------------------------------------------------------------
    // Contas a Receber
    // -------------------------------------------------------------------------

    @GetMapping("/contas-receber")
    @Operation(
        summary = "Listar contas a receber",
        description = "Lista contas a receber com filtros opcionais por cliente e status. " +
                      "Ordenação padrão: vencimento ascendente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<Page<ContaReceberDTO.Resumo>> listarContasReceber(
            @RequestParam(required = false) UUID clienteId,
            @RequestParam(required = false) StatusConta status,
            @PageableDefault(size = 20, sort = "dataVencimento", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(financeiroService.listarContasReceber(clienteId, status, pageable));
    }

    @GetMapping("/contas-receber/{id}")
    @Operation(summary = "Buscar conta a receber por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Conta encontrada"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Conta não encontrada",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<ContaReceberDTO.Response> buscarContaReceber(@PathVariable UUID id) {
        return ResponseEntity.ok(financeiroService.buscarContaReceberPorId(id));
    }

    @PostMapping("/contas-receber/{id}/pagar")
    @Operation(
        summary = "Registrar pagamento de conta a receber",
        description = "Quita uma conta a receber em status ABERTO. " +
                      "Atualiza data de pagamento, valor pago e forma de pagamento."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pagamento registrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Conta já quitada ou cancelada",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Conta não encontrada",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<ContaReceberDTO.Response> pagarContaReceber(
            @PathVariable UUID id,
            @Valid @RequestBody ContaReceberDTO.PagarRequest request) {
        return ResponseEntity.ok(financeiroService.registrarPagamentoReceber(id, request));
    }

    // -------------------------------------------------------------------------
    // Contas a Pagar
    // -------------------------------------------------------------------------

    @GetMapping("/contas-pagar")
    @Operation(
        summary = "Listar contas a pagar",
        description = "Lista contas a pagar com filtro opcional por status. " +
                      "Ordenação padrão: vencimento ascendente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<Page<ContaPagarDTO.Response>> listarContasPagar(
            @RequestParam(required = false) StatusConta status,
            @PageableDefault(size = 20, sort = "dataVencimento", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(financeiroService.listarContasPagar(status, pageable));
    }

    @PostMapping("/contas-pagar")
    @Operation(
        summary = "Lançar conta a pagar",
        description = "Cria uma nova conta a pagar manualmente. " +
                      "Fornecedor é opcional — permite lançamentos avulsos (aluguel, contas de energia, etc.)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Conta a pagar criada"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou fornecedor não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<ContaPagarDTO.Response> lancarContaPagar(
            @Valid @RequestBody ContaPagarDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(financeiroService.lancarContaPagar(request));
    }

    @PostMapping("/contas-pagar/{id}/pagar")
    @Operation(
        summary = "Registrar pagamento de conta a pagar",
        description = "Quita uma conta a pagar em status ABERTO."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pagamento registrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Conta já quitada ou cancelada",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Conta não encontrada",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<ContaPagarDTO.Response> pagarContaPagar(
            @PathVariable UUID id,
            @Valid @RequestBody ContaPagarDTO.PagarRequest request) {
        return ResponseEntity.ok(financeiroService.registrarPagamentoPagar(id, request));
    }

    // -------------------------------------------------------------------------
    // Lançamentos consolidados
    // -------------------------------------------------------------------------

    @GetMapping("/lancamentos")
    @Operation(summary = "Histórico de lançamentos financeiros efetivados (pagamentos e recebimentos)")
    public ResponseEntity<Page<FinanceiroDTO.LancamentoResponse>> listarLancamentos(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String codigoBanco,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @PageableDefault(size = 20, sort = "data", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(
                financeiroService.listarLancamentos(tipo, codigoBanco, de, ate, pageable));
    }

    // -------------------------------------------------------------------------
    // Fluxo de Caixa
    // -------------------------------------------------------------------------

    @GetMapping("/fluxo-caixa")
    @Operation(
        summary = "Fluxo de caixa por período",
        description = "Agrupa entradas (contas a receber) e saídas (contas a pagar) por mês no período informado. " +
                      "Inclui contas de todos os status. Retorna saldo mensal e saldo acumulado " +
                      "para visualização de tendências. Formato de data: `yyyy-MM-dd`."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Fluxo de caixa calculado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<FluxoCaixaDTO.Response> fluxoCaixa(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return ResponseEntity.ok(financeiroService.fluxoCaixa(de, ate));
    }
}
