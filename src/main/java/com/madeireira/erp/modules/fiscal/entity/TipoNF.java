package com.madeireira.erp.modules.fiscal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoNF {

    ENTRADA("Nota Fiscal de Entrada"),
    SAIDA("Nota Fiscal de Saída");

    private final String descricao;
}
