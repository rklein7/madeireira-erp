package com.madeireira.erp.modules.cadastro.entity;

import com.madeireira.erp.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "clientes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cliente extends BaseEntity {

    @Column(name = "tipo_pessoa", nullable = false, length = 2)
    @Builder.Default
    private String tipoPessoa = "PJ";

    @Column(name = "razao_social", nullable = false)
    private String razaoSocial;

    @Column(name = "nome_fantasia")
    private String nomeFantasia;

    @Column(name = "cpf_cnpj", nullable = false, unique = true, length = 18)
    private String cpfCnpj;

    @Column(length = 20)
    private String ie;

    @Column(length = 20)
    private String im;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(length = 20)
    private String celular;

    @Column(length = 9)
    private String cep;

    @Column(length = 200)
    private String logradouro;

    @Column(length = 10)
    private String numero;

    @Column(length = 100)
    private String complemento;

    @Column(length = 100)
    private String bairro;

    @Column(length = 100)
    private String cidade;

    @Column(length = 2)
    private String uf;

    @Column(name = "limite_credito", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal limiteCredito = BigDecimal.ZERO;

    @Column(name = "dias_prazo")
    @Builder.Default
    private Integer diasPrazo = 30;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tabela_preco_id")
    private TabelaPreco tabelaPreco;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;
}
