package com.madeireira.erp.modules.auth.dto;

import lombok.*;

import java.util.UUID;

public class UsuarioDTO {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Resumo {

        private UUID id;
        private String nome;
        private String email;
        private String perfil;
        private Boolean ativo;
    }
}
