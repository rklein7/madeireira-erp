package com.madeireira.erp.modules.fiscal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatusNF {

    ESCRITURADA_MANUAL("Escriturada manualmente"),
    EMITIDA_MANUALMENTE("Emitida manualmente"),
    CANCELADA("Cancelada");

    private final String descricao;

    public boolean podeCancelar() {
        return this == ESCRITURADA_MANUAL || this == EMITIDA_MANUALMENTE;
    }
}
