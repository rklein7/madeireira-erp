package com.madeireira.erp.modules.vendas.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CondicaoPagamento {

    A_VISTA("À vista"),
    A_PRAZO("A prazo"),
    PARCELADO("Parcelado"),
    CHEQUE("Cheque"),
    CARTAO("Cartão");

    private final String descricao;
}
