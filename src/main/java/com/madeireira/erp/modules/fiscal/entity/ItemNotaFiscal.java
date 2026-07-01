package com.madeireira.erp.modules.fiscal.entity;

import com.madeireira.erp.modules.cadastro.entity.Produto;
import com.madeireira.erp.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "itens_nota_fiscal")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemNotaFiscal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nota_fiscal_id")
    private NotaFiscal notaFiscal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @Column(name = "numero_item", nullable = false)
    private Integer numeroItem;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal quantidade;

    @Column(name = "valor_unitario", nullable = false, precision = 12, scale = 4)
    private BigDecimal valorUnitario;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorTotal = BigDecimal.ZERO;

    // ICMS
    @Column(name = "cst_icms", length = 5)
    private String cstIcms;

    @Column(name = "aliq_icms", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal aliqIcms = BigDecimal.ZERO;

    @Column(name = "valor_icms", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorIcms = BigDecimal.ZERO;

    // IPI
    @Column(name = "cst_ipi", length = 5)
    private String cstIpi;

    @Column(name = "aliq_ipi", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal aliqIpi = BigDecimal.ZERO;

    @Column(name = "valor_ipi", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorIpi = BigDecimal.ZERO;

    // PIS
    @Column(name = "cst_pis", length = 5)
    private String cstPis;

    @Column(name = "aliq_pis", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal aliqPis = BigDecimal.ZERO;

    @Column(name = "valor_pis", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorPis = BigDecimal.ZERO;

    // COFINS
    @Column(name = "cst_cofins", length = 5)
    private String cstCofins;

    @Column(name = "aliq_cofins", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal aliqCofins = BigDecimal.ZERO;

    @Column(name = "valor_cofins", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorCofins = BigDecimal.ZERO;

    public void calcularTotal() {
        valorTotal = quantidade.multiply(valorUnitario).setScale(2, RoundingMode.HALF_UP);
    }
}
