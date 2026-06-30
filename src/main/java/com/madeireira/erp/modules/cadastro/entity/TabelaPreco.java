package com.madeireira.erp.modules.cadastro.entity;

import com.madeireira.erp.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tabelas_preco")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TabelaPreco extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @OneToMany(mappedBy = "tabela", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TabelaPrecoItem> itens = new ArrayList<>();
}
