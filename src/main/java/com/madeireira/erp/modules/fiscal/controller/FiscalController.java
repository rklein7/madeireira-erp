package com.madeireira.erp.modules.fiscal.controller;

import com.madeireira.erp.modules.fiscal.dto.NotaFiscalDTO;
import com.madeireira.erp.modules.fiscal.entity.StatusNF;
import com.madeireira.erp.modules.fiscal.entity.TipoNF;
import com.madeireira.erp.modules.fiscal.service.FiscalService;
import io.swagger.v3.oas.annotations.Operation;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal")
@RequiredArgsConstructor
@Tag(name = "Fiscal", description = "Escrituração e emissão de notas fiscais")
public class FiscalController {

    private final FiscalService fiscalService;

    @PostMapping("/entrada")
    @Operation(summary = "Escriturar NF de entrada")
    public ResponseEntity<NotaFiscalDTO.Response> escriturarEntrada(
            @Valid @RequestBody NotaFiscalDTO.EntradaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fiscalService.escriturarEntrada(req));
    }

    @PostMapping("/saida")
    @Operation(summary = "Emitir NF de saída vinculada a pedido faturado")
    public ResponseEntity<NotaFiscalDTO.Response> emitirSaida(
            @Valid @RequestBody NotaFiscalDTO.SaidaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fiscalService.emitirSaida(req));
    }

    @GetMapping
    @Operation(summary = "Listar notas fiscais com filtros opcionais")
    public ResponseEntity<Page<NotaFiscalDTO.Resumo>> listar(
            @RequestParam(required = false) TipoNF tipo,
            @RequestParam(required = false) StatusNF status,
            @RequestParam(required = false) UUID fornecedorId,
            @RequestParam(required = false) UUID clienteId,
            @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @PageableDefault(size = 20, sort = "dataEmissao", direction = Sort.Direction.DESC)
                Pageable pageable) {
        return ResponseEntity.ok(
                fiscalService.listar(tipo, status, fornecedorId, clienteId, de, ate, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar nota fiscal por ID")
    public ResponseEntity<NotaFiscalDTO.Response> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(fiscalService.buscarPorId(id));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar nota fiscal")
    public ResponseEntity<NotaFiscalDTO.Response> cancelar(@PathVariable UUID id) {
        return ResponseEntity.ok(fiscalService.cancelarNF(id));
    }

    @GetMapping("/resumo-tributos")
    @Operation(summary = "Resumo de tributos agrupado por mês no período")
    public ResponseEntity<List<NotaFiscalDTO.ResumoTributos>> resumoTributos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return ResponseEntity.ok(fiscalService.resumoTributos(de, ate));
    }
}
