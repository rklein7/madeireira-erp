package com.madeireira.erp.modules.cadastro.repository;

import com.madeireira.erp.modules.cadastro.entity.TabelaPrecoItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TabelaPrecoItemRepository extends JpaRepository<TabelaPrecoItem, UUID> {
    boolean existsByTabelaIdAndProdutoId(UUID tabelaId, UUID produtoId);
}
