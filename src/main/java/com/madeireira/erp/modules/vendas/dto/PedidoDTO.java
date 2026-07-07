package com.madeireira.erp.modules.vendas.dto;

import com.madeireira.erp.modules.cadastro.entity.UnidadeMedida;
import com.madeireira.erp.modules.vendas.entity.CondicaoPagamento;
import com.madeireira.erp.modules.vendas.entity.StatusPedido;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PedidoDTO {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ItemRequest {

        @NotNull(message = "Produto é obrigatório")
        private UUID produtoId;

        @NotNull(message = "Quantidade é obrigatória")
        @DecimalMin(value = "0.0001", message = "Quantidade deve ser maior que zero")
        private BigDecimal quantidade;

        @Builder.Default
        private BigDecimal descontoPerc = BigDecimal.ZERO;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {

        @NotNull(message = "Cliente é obrigatório")
        private UUID clienteId;

        /** Opcional — pedido pode ser criado sem vendedor vinculado */
        private UUID vendedorId;

        @NotNull(message = "Condição de pagamento é obrigatória")
        private CondicaoPagamento condicaoPagamento;

        @Builder.Default
        private Integer parcelas = 1;

        @Builder.Default
        private BigDecimal valorFrete = BigDecimal.ZERO;

        private String observacoes;

        @NotNull(message = "Lista de itens é obrigatória")
        @NotEmpty(message = "O pedido deve ter ao menos um item")
        @Valid
        private List<ItemRequest> itens;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ItemResponse {

        private UUID id;

        private UUID produtoId;
        private String produtoCodigo;
        private String produtoDescricao;
        private UnidadeMedida unidadeMedida;
        private String unidadeSimbolo;

        private BigDecimal quantidade;
        private BigDecimal precoUnitario;
        private BigDecimal descontoPerc;
        private BigDecimal valorTotal;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {

        private UUID id;
        private String numero;
        private StatusPedido status;
        private CondicaoPagamento condicaoPagamento;
        private Integer parcelas;

        private UUID clienteId;
        private String clienteNome;
        private String clienteCpfCnpj;
        private String clienteEndereco;
        private String clienteBairro;
        private String clienteCidade;
        private String clienteUf;
        private String clienteCep;
        private String clienteTelefone;
        private String clienteEmail;
        private String clienteIe;

        private UUID vendedorId;
        private String vendedorNome;

        private BigDecimal valorSubtotal;
        private BigDecimal valorDesconto;
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
        private StatusPedido status;
        private String clienteNome;
        private String vendedorNome;
        private CondicaoPagamento condicaoPagamento;

        private BigDecimal valorTotal;
        private int totalItens;

        private LocalDateTime criadoEm;
    }
}
