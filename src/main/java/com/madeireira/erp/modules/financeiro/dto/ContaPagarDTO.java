package com.madeireira.erp.modules.financeiro.dto;

import com.madeireira.erp.modules.financeiro.entity.FormaPagamento;
import com.madeireira.erp.modules.financeiro.entity.StatusConta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ContaPagarDTO {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {

        private UUID fornecedorId;

        @NotBlank(message = "Descrição é obrigatória")
        private String descricao;

        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
        private BigDecimal valor;

        @NotNull(message = "Data de vencimento é obrigatória")
        private LocalDate dataVencimento;

        private String documento;

        private String observacoes;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {

        private UUID id;

        private UUID fornecedorId;
        private String fornecedorNome;

        private String descricao;
        private BigDecimal valor;
        private LocalDate dataVencimento;
        private LocalDate dataPagamento;
        private BigDecimal valorPago;

        private StatusConta status;
        private FormaPagamento formaPagamento;

        private String documento;
        private String observacoes;
        private boolean vencida;

        private LocalDateTime criadoEm;
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
