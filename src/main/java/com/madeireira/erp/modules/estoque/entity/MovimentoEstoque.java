package com.madeireira.erp.modules.estoque.entity;

import com.madeireira.erp.modules.auth.entity.Usuario;
import com.madeireira.erp.modules.cadastro.entity.Fornecedor;
import com.madeireira.erp.modules.cadastro.entity.Produto;
import com.madeireira.erp.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "movimentos_estoque")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MovimentoEstoque extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimento tipo;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal quantidade;

    @Column(name = "custo_unitario", precision = 12, scale = 4)
    private BigDecimal custoUnitario;

    /** Saldo do produto imediatamente após este movimento */
    @Column(name = "saldo_apos", nullable = false, precision = 12, scale = 4)
    private BigDecimal saldoApos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @Column(length = 60)
    private String documento;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
