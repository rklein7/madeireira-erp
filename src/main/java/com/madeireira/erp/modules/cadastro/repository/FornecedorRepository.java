package com.madeireira.erp.modules.cadastro.repository;

import com.madeireira.erp.modules.cadastro.entity.Fornecedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FornecedorRepository extends JpaRepository<Fornecedor, UUID> {
    Optional<Fornecedor> findByCpfCnpj(String cpfCnpj);
    boolean existsByCpfCnpj(String cpfCnpj);
    Page<Fornecedor> findByAtivoTrue(Pageable pageable);

    @Query("SELECT f FROM Fornecedor f WHERE f.ativo = true AND " +
           "(LOWER(f.razaoSocial) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "f.cpfCnpj LIKE CONCAT('%', :termo, '%'))")
    Page<Fornecedor> buscar(@Param("termo") String termo, Pageable pageable);
}
