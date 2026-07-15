package com.madeireira.erp.modules.fiscal.entity;

import com.madeireira.erp.modules.auth.entity.Usuario;
import com.madeireira.erp.modules.cadastro.entity.Cliente;
import com.madeireira.erp.modules.cadastro.entity.Fornecedor;
import com.madeireira.erp.modules.compras.entity.PedidoCompra;
import com.madeireira.erp.modules.vendas.entity.Pedido;
import com.madeireira.erp.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notas_fiscais")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotaFiscal extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoNF tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private StatusNF status = StatusNF.ESCRITURADA_MANUAL;

    @Column(nullable = false, length = 20)
    private String numero;

    @Column(nullable = false, length = 5)
    @Builder.Default
    private String serie = "1";

    @Column(nullable = false, length = 10)
    private String cfop;

    @Column(name = "chave_acesso", length = 44)
    private String chaveAcesso;

    @Column(name = "data_emissao", nullable = false)
    private LocalDate dataEmissao;

    @Column(name = "data_entrada_saida", nullable = false)
    private LocalDate dataEntradaSaida;

    @Column(name = "natureza_operacao", length = 100)
    private String naturezaOperacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_compra_id")
    private PedidoCompra pedidoCompra;

    @Column(name = "valor_produtos", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorProdutos = BigDecimal.ZERO;

    @Column(name = "valor_frete", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorFrete = BigDecimal.ZERO;

    @Column(name = "valor_seguro", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorSeguro = BigDecimal.ZERO;

    @Column(name = "valor_desconto", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorDesconto = BigDecimal.ZERO;

    @Column(name = "valor_ipi", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorIpi = BigDecimal.ZERO;

    @Column(name = "valor_icms", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorIcms = BigDecimal.ZERO;

    @Column(name = "valor_pis", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorPis = BigDecimal.ZERO;

    @Column(name = "valor_cofins", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorCofins = BigDecimal.ZERO;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @OneToMany(mappedBy = "notaFiscal", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemNotaFiscal> itens = new ArrayList<>();

    /**
     * Soma os valores dos itens e atualiza os totais da nota.
     * valorFrete, valorSeguro e valorDesconto são mantidos conforme informados.
     * valorTotal = valorProdutos + valorFrete + valorSeguro + valorIpi - valorDesconto
     */
    public void calcularTotais() {
        valorProdutos = itens.stream()
                .map(ItemNotaFiscal::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        valorIpi = itens.stream()
                .map(ItemNotaFiscal::getValorIpi)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        valorIcms = itens.stream()
                .map(ItemNotaFiscal::getValorIcms)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        valorPis = itens.stream()
                .map(ItemNotaFiscal::getValorPis)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        valorCofins = itens.stream()
                .map(ItemNotaFiscal::getValorCofins)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal frete  = valorFrete   != null ? valorFrete   : BigDecimal.ZERO;
        BigDecimal seguro = valorSeguro  != null ? valorSeguro  : BigDecimal.ZERO;
        BigDecimal desc   = valorDesconto != null ? valorDesconto : BigDecimal.ZERO;

        valorTotal = valorProdutos
                .add(frete)
                .add(seguro)
                .add(valorIpi)
                .subtract(desc)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
