package com.madeireira.erp.modules.estoque.dto;

import com.madeireira.erp.modules.cadastro.entity.UnidadeMedida;
import com.madeireira.erp.modules.estoque.entity.TipoMovimento;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class MovimentoEstoqueDTO {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {

        @NotNull(message = "Produto é obrigatório")
        private UUID produtoId;

        @NotNull(message = "Tipo de movimento é obrigatório")
        private TipoMovimento tipo;

        @NotNull(message = "Quantidade é obrigatória")
        @DecimalMin(value = "0.0001", message = "Quantidade deve ser maior que zero")
        private BigDecimal quantidade;

        private BigDecimal custoUnitario;

        private UUID fornecedorId;

        @Size(max = 60)
        private String documento;

        private String observacoes;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {

        private UUID id;

        private UUID produtoId;
        private String produtoCodigo;
        private String produtoDescricao;
        private UnidadeMedida produtoUnidade;

        private TipoMovimento tipo;
        private String tipoDescricao;

        private BigDecimal quantidade;
        private BigDecimal custoUnitario;
        private BigDecimal custoTotal;

        private BigDecimal saldoApos;

        private UUID fornecedorId;
        private String fornecedorNome;

        private String documento;
        private String observacoes;

        private UUID usuarioId;
        private String usuarioNome;

        private LocalDateTime criadoEm;
        private LocalDateTime atualizadoEm;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SaldoProduto {

        private UUID produtoId;
        private String produtoCodigo;
        private String produtoDescricao;
        private UnidadeMedida unidadeMedida;
        private String unidadeSimbolo;

        private BigDecimal estoqueAtual;
        private BigDecimal estoqueMinimo;
        private boolean abaixoDoMinimo;

        private LocalDateTime ultimoMovimento;
        private TipoMovimento tipoUltimoMovimento;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PosicaoEstoque {

        private UUID produtoId;
        private String codigo;
        private String descricao;
        private UnidadeMedida unidadeMedida;
        private String unidadeSimbolo;

        private BigDecimal estoqueAtual;
        private BigDecimal estoqueMinimo;
        private BigDecimal estoqueMaximo;
        private boolean abaixoDoMinimo;

        private BigDecimal precoVenda;
    }
}
