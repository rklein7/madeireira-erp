package com.madeireira.erp.modules.financeiro.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class FluxoCaixaDTO {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ItemFluxo {

        private String periodo;
        private BigDecimal entradas;
        private BigDecimal saidas;
        private BigDecimal saldo;
        private BigDecimal saldoAcumulado;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {

        private LocalDate de;
        private LocalDate ate;
        private String agrupamento;

        private BigDecimal totalEntradas;
        private BigDecimal totalSaidas;
        private BigDecimal saldoPeriodo;

        private List<ItemFluxo> itens;
    }
}
