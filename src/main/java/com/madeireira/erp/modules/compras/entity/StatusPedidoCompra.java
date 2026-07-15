package com.madeireira.erp.modules.compras.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatusPedidoCompra {

    RASCUNHO("Rascunho"),
    CONFIRMADO("Confirmado"),
    RECEBIDO("Recebido"),
    CANCELADO("Cancelado");

    private final String descricao;

    public boolean podeCancelar() {
        return this == RASCUNHO || this == CONFIRMADO;
    }
}
