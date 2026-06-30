package com.madeireira.erp.modules.cadastro.entity;

import com.madeireira.erp.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorias")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Categoria extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String nome;

    private String descricao;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @OneToMany(mappedBy = "categoria")
    @Builder.Default
    private List<Produto> produtos = new ArrayList<>();
}
