package com.madeireira.erp.modules.compras.repository;

import com.madeireira.erp.modules.compras.entity.ItemPedidoCompra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ItemPedidoCompraRepository extends JpaRepository<ItemPedidoCompra, UUID> {
}
