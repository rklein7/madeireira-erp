package com.madeireira.erp.modules.estoque.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoMovimento {

    ENTRADA_MANUAL("Entrada manual", true),
    SAIDA_MANUAL("Saída manual", false),
    SAIDA_PEDIDO("Saída por pedido de venda", false),
    AJUSTE("Ajuste de inventário", null);

    private final String descricao;

    /** true = entrada, false = saída, null = ajuste (pode ir nos dois sentidos) */
    private final Boolean entrada;

    public boolean isEntrada() {
        return Boolean.TRUE.equals(entrada);
    }

    public boolean isSaida() {
        return Boolean.FALSE.equals(entrada);
    }
}
