package com.madeireira.erp.modules.financeiro.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatusConta {

    ABERTO("Aberto"),
    PAGO("Pago"),
    VENCIDO("Vencido"),
    CANCELADO("Cancelado");

    private final String descricao;

    public boolean isQuitado() {
        return this == PAGO;
    }
}
