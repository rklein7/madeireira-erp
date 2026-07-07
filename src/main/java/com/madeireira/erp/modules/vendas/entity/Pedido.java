package com.madeireira.erp.modules.vendas.entity;

import com.madeireira.erp.modules.auth.entity.Usuario;
import com.madeireira.erp.modules.cadastro.entity.Cliente;
import com.madeireira.erp.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Pedido extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusPedido status = StatusPedido.RASCUNHO;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicao_pagamento", nullable = false, length = 20)
    private CondicaoPagamento condicaoPagamento;

    @Column
    @Builder.Default
    private Integer parcelas = 1;

    @Column(name = "valor_subtotal", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorSubtotal = BigDecimal.ZERO;

    @Column(name = "valor_desconto", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorDesconto = BigDecimal.ZERO;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id")
    private Usuario vendedor;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemPedido> itens = new ArrayList<>();

    /**
     * Recalcula valorSubtotal somando os itens, aplica desconto e frete,
     * e atualiza valorTotal. Deve ser chamado sempre que itens forem alterados.
     */
    public void calcularTotais() {
        valorSubtotal = itens.stream()
                .map(ItemPedido::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal desconto = valorDesconto != null ? valorDesconto : BigDecimal.ZERO;
        BigDecimal frete = valorFrete != null ? valorFrete : BigDecimal.ZERO;

        valorTotal = valorSubtotal.subtract(desconto).add(frete)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
