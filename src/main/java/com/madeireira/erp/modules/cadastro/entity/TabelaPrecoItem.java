package com.madeireira.erp.modules.cadastro.entity;

import com.madeireira.erp.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tabela_preco_itens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TabelaPrecoItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tabela_id")
    private TabelaPreco tabela;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal preco;

    @Column(name = "desconto_max", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal descontoMax = BigDecimal.ZERO;

    @Column(name = "vigencia_inicio")
    private LocalDate vigenciaInicio;

    @Column(name = "vigencia_fim")
    private LocalDate vigenciaFim;
}
