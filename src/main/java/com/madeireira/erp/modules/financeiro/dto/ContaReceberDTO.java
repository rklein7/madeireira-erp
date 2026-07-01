package com.madeireira.erp.modules.financeiro.dto;

import com.madeireira.erp.modules.financeiro.entity.FormaPagamento;
import com.madeireira.erp.modules.financeiro.entity.StatusConta;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ContaReceberDTO {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {

        private UUID id;

        private UUID pedidoId;
        private String pedidoNumero;

        private UUID clienteId;
        private String clienteNome;

        private String descricao;
        private BigDecimal valor;
        private LocalDate dataVencimento;
        private LocalDate dataPagamento;
        private BigDecimal valorPago;

        private StatusConta status;
        private FormaPagamento formaPagamento;

        private Integer parcela;
        private Integer totalParcelas;

        private String observacoes;
        private boolean vencida;

        private LocalDateTime criadoEm;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Resumo {

        private UUID id;
        private String clienteNome;
        private String descricao;
        private BigDecimal valor;
        private LocalDate dataVencimento;
        private StatusConta status;
        private Integer parcela;
        private Integer totalParcelas;
        private boolean vencida;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PagarRequest {

        @NotNull(message = "Data de pagamento é obrigatória")
        private LocalDate dataPagamento;

        @NotNull(message = "Valor pago é obrigatório")
        private BigDecimal valorPago;

        @NotNull(message = "Forma de pagamento é obrigatória")
        private FormaPagamento formaPagamento;

        private String observacoes;
    }
}
