package com.madeireira.erp.modules.vendas.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatusPedido {

    RASCUNHO("Rascunho"),
    CONFIRMADO("Confirmado"),
    FATURADO("Faturado"),
    ENTREGUE("Entregue"),
    CANCELADO("Cancelado");

    private final String descricao;

    public boolean podeCancelar() {
        return this == RASCUNHO || this == CONFIRMADO;
    }
}
