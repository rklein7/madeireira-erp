package com.madeireira.erp.modules.compras.dto;

import com.madeireira.erp.modules.compras.entity.StatusPedidoCompra;
import com.madeireira.erp.modules.vendas.entity.CondicaoPagamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PedidoCompraDTO {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ItemRequest {

        @NotNull(message = "Produto é obrigatório")
        private UUID produtoId;

        @NotNull(message = "Número do item é obrigatório")
        private Integer numeroItem;

        @NotNull(message = "Quantidade é obrigatória")
        @DecimalMin(value = "0.0001", message = "Quantidade deve ser maior que zero")
        private BigDecimal quantidade;

        @NotNull(message = "Preço unitário é obrigatório")
        @DecimalMin(value = "0.01", message = "Preço unitário deve ser maior que zero")
        private BigDecimal precoUnitario;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {

        @NotNull(message = "Fornecedor é obrigatório")
        private UUID fornecedorId;

        @NotNull(message = "Condição de pagamento é obrigatória")
        private CondicaoPagamento condicaoPagamento;

        @Builder.Default
        private Integer parcelas = 1;

        @Builder.Default
        private BigDecimal valorFrete = BigDecimal.ZERO;

        private String observacoes;

        @NotNull(message = "Lista de itens é obrigatória")
        @NotEmpty(message = "O pedido de compra deve ter ao menos um item")
        @Valid
        private List<ItemRequest> itens;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ItemResponse {

        private UUID id;
        private UUID produtoId;
        private String produtoCodigo;
        private String produtoDescricao;
        private String unidadeSimbolo;
        private Integer numeroItem;
        private BigDecimal quantidade;
        private BigDecimal precoUnitario;
        private BigDecimal valorTotal;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {

        private UUID id;
        private String numero;
        private StatusPedidoCompra status;
        private String statusDescricao;

        private UUID fornecedorId;
        private String fornecedorNome;

        private CondicaoPagamento condicaoPagamento;
        private Integer parcelas;

        private BigDecimal valorFrete;
        private BigDecimal valorTotal;

        private String observacoes;
        private String usuarioNome;

        private List<ItemResponse> itens;

        private LocalDateTime criadoEm;
        private LocalDateTime atualizadoEm;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Resumo {

        private UUID id;
        private String numero;
        private StatusPedidoCompra status;
        private String statusDescricao;
        private String fornecedorNome;
        private CondicaoPagamento condicaoPagamento;
        private BigDecimal valorTotal;
        private int totalItens;
        private LocalDateTime criadoEm;
    }
}
