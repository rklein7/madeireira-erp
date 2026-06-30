package com.madeireira.erp.modules.cadastro.repository;

import com.madeireira.erp.modules.cadastro.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    Optional<Cliente> findByCpfCnpj(String cpfCnpj);
    boolean existsByCpfCnpj(String cpfCnpj);
    Page<Cliente> findByAtivoTrue(Pageable pageable);

    @Query("SELECT c FROM Cliente c WHERE c.ativo = true AND " +
           "(LOWER(c.razaoSocial) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "LOWER(c.nomeFantasia) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "c.cpfCnpj LIKE CONCAT('%', :termo, '%'))")
    Page<Cliente> buscar(@Param("termo") String termo, Pageable pageable);
}
