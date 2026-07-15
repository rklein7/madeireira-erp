package com.madeireira.erp.modules.compras.entity;

import com.madeireira.erp.modules.auth.entity.Usuario;
import com.madeireira.erp.modules.cadastro.entity.Fornecedor;
import com.madeireira.erp.modules.vendas.entity.CondicaoPagamento;
import com.madeireira.erp.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos_compra")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PedidoCompra extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private StatusPedidoCompra status = StatusPedidoCompra.RASCUNHO;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicao_pagamento", nullable = false, length = 30)
    private CondicaoPagamento condicaoPagamento;

    @Column
    @Builder.Default
    private Integer parcelas = 1;

    @Column(name = "valor_frete", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorFrete = BigDecimal.ZERO;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @OneToMany(mappedBy = "pedidoCompra", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemPedidoCompra> itens = new ArrayList<>();

    public void calcularTotal() {
        BigDecimal subtotal = itens.stream()
                .map(ItemPedidoCompra::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal frete = valorFrete != null ? valorFrete : BigDecimal.ZERO;
        valorTotal = subtotal.add(frete).setScale(2, RoundingMode.HALF_UP);
    }
}
