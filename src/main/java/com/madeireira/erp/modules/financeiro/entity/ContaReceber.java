package com.madeireira.erp.modules.financeiro.entity;

import com.madeireira.erp.modules.auth.entity.Usuario;
import com.madeireira.erp.modules.cadastro.entity.Cliente;
import com.madeireira.erp.modules.vendas.entity.Pedido;
import com.madeireira.erp.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contas_receber")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContaReceber extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(nullable = false, length = 255)
    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Column(name = "valor_pago", precision = 12, scale = 2)
    private BigDecimal valorPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusConta status = StatusConta.ABERTO;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", length = 30)
    private FormaPagamento formaPagamento;

    @Column(nullable = false)
    @Builder.Default
    private Integer parcela = 1;

    @Column(name = "total_parcelas", nullable = false)
    @Builder.Default
    private Integer totalParcelas = 1;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public boolean isVencida() {
        return status == StatusConta.ABERTO && dataVencimento.isBefore(LocalDate.now());
    }
}
