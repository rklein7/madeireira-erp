package com.madeireira.erp.modules.cadastro.repository;

import com.madeireira.erp.modules.cadastro.entity.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProdutoRepository extends JpaRepository<Produto, UUID> {
    Optional<Produto> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    Page<Produto> findByAtivoTrue(Pageable pageable);
    List<Produto> findByCategoriaIdAndAtivoTrue(UUID categoriaId);

    @Query("SELECT p FROM Produto p WHERE p.ativo = true AND " +
           "(LOWER(p.descricao) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "LOWER(p.codigo) LIKE LOWER(CONCAT('%', :termo, '%')))")
    Page<Produto> buscar(@Param("termo") String termo, Pageable pageable);

    @Query("SELECT p FROM Produto p WHERE p.ativo = true AND p.estoqueAtual < p.estoqueMinimo")
    List<Produto> findAbaixoDoEstoqueMinimo();
}
