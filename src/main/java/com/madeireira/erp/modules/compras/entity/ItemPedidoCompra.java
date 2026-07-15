package com.madeireira.erp.modules.compras.entity;

import com.madeireira.erp.modules.cadastro.entity.Produto;
import com.madeireira.erp.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "itens_pedido_compra")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemPedidoCompra extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_compra_id")
    private PedidoCompra pedidoCompra;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @Column(name = "numero_item", nullable = false)
    private Integer numeroItem;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 12, scale = 4)
    private BigDecimal precoUnitario;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorTotal = BigDecimal.ZERO;

    public void calcularTotal() {
        valorTotal = quantidade.multiply(precoUnitario).setScale(2, RoundingMode.HALF_UP);
    }
}
