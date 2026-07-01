package com.madeireira.erp.modules.vendas.entity;

import com.madeireira.erp.modules.cadastro.entity.Produto;
import com.madeireira.erp.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "itens_pedido")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemPedido extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 12, scale = 4)
    private BigDecimal precoUnitario;

    @Column(name = "desconto_perc", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal descontoPerc = BigDecimal.ZERO;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorTotal = BigDecimal.ZERO;

    /** Aplica desconto percentual sobre (quantidade * precoUnitario) e atualiza valorTotal. */
    public void calcularTotal() {
        BigDecimal bruto = quantidade.multiply(precoUnitario);
        if (descontoPerc != null && descontoPerc.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal fator = BigDecimal.ONE.subtract(
                    descontoPerc.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP));
            valorTotal = bruto.multiply(fator).setScale(2, RoundingMode.HALF_UP);
        } else {
            valorTotal = bruto.setScale(2, RoundingMode.HALF_UP);
        }
    }
}
