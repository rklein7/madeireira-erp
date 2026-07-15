package com.madeireira.erp.modules.compras.repository;

import com.madeireira.erp.modules.compras.entity.PedidoCompra;
import com.madeireira.erp.modules.compras.entity.StatusPedidoCompra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface PedidoCompraRepository extends JpaRepository<PedidoCompra, UUID> {

    @Query(nativeQuery = true, value = "SELECT nextval('seq_pedido_compra_numero')")
    Long nextNumero();

    Page<PedidoCompra> findByFornecedorId(UUID fornecedorId, Pageable pageable);

    Page<PedidoCompra> findByStatus(StatusPedidoCompra status, Pageable pageable);

    Page<PedidoCompra> findByFornecedorIdAndStatus(UUID fornecedorId, StatusPedidoCompra status, Pageable pageable);

    @Query("SELECT p FROM PedidoCompra p " +
           "WHERE p.status = com.madeireira.erp.modules.compras.entity.StatusPedidoCompra.CONFIRMADO " +
           "AND NOT EXISTS (" +
           "  SELECT nf FROM com.madeireira.erp.modules.fiscal.entity.NotaFiscal nf " +
           "  WHERE nf.pedidoCompra.id = p.id " +
           "  AND nf.status <> com.madeireira.erp.modules.fiscal.entity.StatusNF.CANCELADA" +
           ")")
    Page<PedidoCompra> findConfirmadosSemNfVinculada(Pageable pageable);
}
