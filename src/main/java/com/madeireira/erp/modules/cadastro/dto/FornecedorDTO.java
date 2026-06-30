package com.madeireira.erp.modules.cadastro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class FornecedorDTO {

    @Schema(description = "Dados para cadastro ou atualização de fornecedor")
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {

        @Schema(description = "Tipo de pessoa: PF (Física) ou PJ (Jurídica)", example = "PJ")
        @NotBlank
        private String tipoPessoa = "PJ";

        @Schema(description = "Razão social (PJ) ou nome completo (PF)",
                example = "Madeiras do Sul Indústria LTDA")
        @NotBlank(message = "Razão social é obrigatória")
        private String razaoSocial;

        @Schema(description = "Nome fantasia", example = "Madeiras do Sul")
        private String nomeFantasia;

        @Schema(description = "CPF (11 dígitos) ou CNPJ (18 caracteres com máscara)",
                example = "98.765.432/0001-10")
        @NotBlank(message = "CPF/CNPJ é obrigatório")
        private String cpfCnpj;

        @Schema(description = "Inscrição Estadual do fornecedor", example = "987.654.321-00")
        private String ie;

        @Schema(description = "E-mail comercial para pedidos e notas",
                example = "vendas@madeirassul.com.br")
        @Email
        private String email;

        @Schema(example = "(54) 3333-5555")
        private String telefone;

        @Schema(example = "(54) 99988-7766")
        private String celular;

        @Schema(description = "Nome do contato comercial no fornecedor",
                example = "Carlos Eduardo")
        private String contato;

        @Schema(example = "95010-020")
        private String cep;

        @Schema(example = "Av. das Indústrias")
        private String logradouro;

        @Schema(example = "800")
        private String numero;

        @Schema(example = "Galpão B")
        private String complemento;

        @Schema(example = "Distrito Industrial")
        private String bairro;

        @Schema(example = "Caxias do Sul")
        private String cidade;

        @Schema(description = "UF com 2 letras", example = "RS")
        private String uf;

        @Schema(description = "Prazo de entrega padrão em dias corridos", example = "7")
        @Min(0)
        private Integer prazoEntrega;

        @Schema(description = "Observações internas sobre o fornecedor",
                example = "Frete grátis acima de R$ 5.000. Pedido mínimo: 10 chapas.")
        private String observacoes;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private UUID id;
        private String tipoPessoa;
        private String razaoSocial;
        private String nomeFantasia;
        private String cpfCnpj;
        private String ie;
        private String email;
        private String telefone;
        private String celular;
        private String contato;
        private String cep;
        private String logradouro;
        private String numero;
        private String complemento;
        private String bairro;
        private String cidade;
        private String uf;
        private Integer prazoEntrega;
        private Boolean ativo;
        private String observacoes;
        private LocalDateTime criadoEm;
        private LocalDateTime atualizadoEm;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Resumo {
        private UUID id;
        private String razaoSocial;
        private String nomeFantasia;
        private String cpfCnpj;
        private String telefone;
        private String cidade;
        private String uf;
        private Boolean ativo;
    }
}
