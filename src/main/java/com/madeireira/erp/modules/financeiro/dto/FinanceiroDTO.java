package com.madeireira.erp.modules.financeiro.dto;

import com.madeireira.erp.modules.financeiro.entity.FormaPagamento;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class FinanceiroDTO {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LancamentoResponse {

        private UUID id;

        /** "RECEBIMENTO" ou "PAGAMENTO" */
        private String tipo;

        private LocalDate data;

        private String descricao;

        private BigDecimal valor;
        private BigDecimal valorPago;

        private String codigoBanco;
        private String nomeBanco;

        private FormaPagamento formaPagamento;

        /** Preenchido apenas para RECEBIMENTO */
        private String clienteNome;

        /** Preenchido apenas para PAGAMENTO */
        private String fornecedorNome;

        /** "CONTA_RECEBER" ou "CONTA_PAGAR" */
        private String origem;
    }
}
