package com.madeireira.erp.modules.financeiro.repository;

import com.madeireira.erp.modules.financeiro.entity.ContaPagar;
import com.madeireira.erp.modules.financeiro.entity.StatusConta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ContaPagarRepository extends JpaRepository<ContaPagar, UUID> {

    Page<ContaPagar> findByStatus(StatusConta status, Pageable pageable);

    List<ContaPagar> findByFornecedorId(UUID fornecedorId);

    List<ContaPagar> findByDataVencimentoBetween(LocalDate de, LocalDate ate);

    @Query("SELECT c FROM ContaPagar c " +
           "WHERE c.dataVencimento BETWEEN :de AND :ate " +
           "AND c.status = com.madeireira.erp.modules.financeiro.entity.StatusConta.ABERTO")
    List<ContaPagar> findAbertasVencendoNoPeriodo(
            @Param("de") LocalDate de,
            @Param("ate") LocalDate ate);
}
