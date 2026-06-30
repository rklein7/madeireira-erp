package com.madeireira.erp.modules.cadastro.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UnidadeMedida {
    M2("m²", "Metro quadrado"),
    M3("m³", "Metro cúbico"),
    KG("kg", "Quilograma"),
    PECA("pç", "Peça"),
    ML("ml", "Metro linear"),
    ROLO("rl", "Rolo"),
    DUZIA("dz", "Dúzia"),
    CENTO("ct", "Cento");

    private final String simbolo;
    private final String descricao;
}
