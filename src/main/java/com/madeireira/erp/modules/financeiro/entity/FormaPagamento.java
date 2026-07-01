package com.madeireira.erp.modules.financeiro.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FormaPagamento {

    DINHEIRO("Dinheiro"),
    PIX("Pix"),
    BOLETO("Boleto"),
    CARTAO_CREDITO("Cartão de crédito"),
    CARTAO_DEBITO("Cartão de débito"),
    CHEQUE("Cheque"),
    TRANSFERENCIA("Transferência bancária");

    private final String descricao;
}
