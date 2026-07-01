package com.madeireira.erp.modules.estoque.repository;

import com.madeireira.erp.modules.estoque.entity.MovimentoEstoque;
import com.madeireira.erp.modules.estoque.entity.TipoMovimento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface MovimentoEstoqueRepository extends JpaRepository<MovimentoEstoque, UUID> {

    Page<MovimentoEstoque> findByProdutoId(UUID produtoId, Pageable pageable);

    Page<MovimentoEstoque> findByProdutoIdAndTipo(UUID produtoId, TipoMovimento tipo, Pageable pageable);

    @Query("SELECT m FROM MovimentoEstoque m WHERE m.produto.id = :produtoId " +
           "AND m.criadoEm BETWEEN :de AND :ate")
    Page<MovimentoEstoque> findByProdutoIdAndPeriodo(
            @Param("produtoId") UUID produtoId,
            @Param("de") LocalDateTime de,
            @Param("ate") LocalDateTime ate,
            Pageable pageable);

    Optional<MovimentoEstoque> findFirstByProdutoIdOrderByCriadoEmDesc(UUID produtoId);
}
