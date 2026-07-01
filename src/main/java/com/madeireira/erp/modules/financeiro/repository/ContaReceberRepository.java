package com.madeireira.erp.modules.financeiro.repository;

import com.madeireira.erp.modules.financeiro.entity.ContaReceber;
import com.madeireira.erp.modules.financeiro.entity.StatusConta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ContaReceberRepository extends JpaRepository<ContaReceber, UUID> {

    Page<ContaReceber> findByClienteId(UUID clienteId, Pageable pageable);

    Page<ContaReceber> findByStatus(StatusConta status, Pageable pageable);

    Page<ContaReceber> findByClienteIdAndStatus(UUID clienteId, StatusConta status, Pageable pageable);

    List<ContaReceber> findByPedidoId(UUID pedidoId);

    List<ContaReceber> findByDataVencimentoBetween(LocalDate de, LocalDate ate);

    @Query("SELECT c FROM ContaReceber c " +
           "WHERE c.dataVencimento BETWEEN :de AND :ate " +
           "AND c.status = com.madeireira.erp.modules.financeiro.entity.StatusConta.ABERTO")
    List<ContaReceber> findAbertasVencendoNoPeriodo(
            @Param("de") LocalDate de,
            @Param("ate") LocalDate ate);

    @Query("SELECT COALESCE(SUM(c.valor), 0) FROM ContaReceber c " +
           "WHERE c.cliente.id = :clienteId " +
           "AND c.status = com.madeireira.erp.modules.financeiro.entity.StatusConta.ABERTO")
    BigDecimal sumValorAbertoByClienteId(@Param("clienteId") UUID clienteId);
}
