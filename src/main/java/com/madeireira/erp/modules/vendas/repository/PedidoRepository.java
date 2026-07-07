package com.madeireira.erp.modules.vendas.repository;

import com.madeireira.erp.modules.vendas.entity.Pedido;
import com.madeireira.erp.modules.vendas.entity.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface PedidoRepository extends JpaRepository<Pedido, UUID> {

    @Query(nativeQuery = true, value = "SELECT nextval('seq_pedido_numero')")
    Long nextNumeroPedido();

    Page<Pedido> findByClienteId(UUID clienteId, Pageable pageable);

    Page<Pedido> findByStatus(StatusPedido status, Pageable pageable);

    Page<Pedido> findByClienteIdAndStatus(UUID clienteId, StatusPedido status, Pageable pageable);

    @Query("SELECT p FROM Pedido p WHERE p.status = com.madeireira.erp.modules.vendas.entity.StatusPedido.FATURADO " +
           "AND NOT EXISTS (" +
           "  SELECT nf FROM com.madeireira.erp.modules.fiscal.entity.NotaFiscal nf " +
           "  WHERE nf.pedido.id = p.id " +
           "  AND nf.status <> com.madeireira.erp.modules.fiscal.entity.StatusNF.CANCELADA" +
           ")")
    Page<Pedido> findFaturadosSemNfVinculada(Pageable pageable);
}
