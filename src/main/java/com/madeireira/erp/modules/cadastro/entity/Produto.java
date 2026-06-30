package com.madeireira.erp.modules.cadastro.entity;

import com.madeireira.erp.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "produtos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Produto extends BaseEntity {

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(nullable = false)
    private String descricao;

    @Column(name = "descricao_curta", length = 100)
    private String descricaoCurta;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidade_medida", nullable = false, length = 10)
    private UnidadeMedida unidadeMedida;

    @Column(name = "preco_custo", precision = 12, scale = 4)
    private BigDecimal precoCusto;

    @Column(name = "preco_venda", nullable = false, precision = 12, scale = 4)
    private BigDecimal precoVenda;

    @Column(name = "estoque_atual", nullable = false, precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal estoqueAtual = BigDecimal.ZERO;

    @Column(name = "estoque_minimo", nullable = false, precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal estoqueMinimo = BigDecimal.ZERO;

    @Column(name = "estoque_maximo", precision = 12, scale = 4)
    private BigDecimal estoqueMaximo;

    @Column(length = 10)
    private String ncm;

    @Column(name = "peso_unitario", precision = 10, scale = 4)
    private BigDecimal pesoUnitario;

    @Column(name = "largura_cm", precision = 8, scale = 2)
    private BigDecimal larguraCm;

    @Column(name = "comprimento_cm", precision = 8, scale = 2)
    private BigDecimal comprimentoCm;

    @Column(name = "espessura_mm", precision = 6, scale = 2)
    private BigDecimal espessuraMm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    public boolean isAbaixoDoMinimo() {
        return estoqueAtual.compareTo(estoqueMinimo) < 0;
    }
}
