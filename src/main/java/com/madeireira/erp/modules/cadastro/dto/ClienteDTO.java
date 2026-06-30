package com.madeireira.erp.modules.cadastro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ClienteDTO {

    @Schema(description = "Dados para cadastro ou atualização de cliente")
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {

        @Schema(description = "Tipo de pessoa: PF (Física) ou PJ (Jurídica)", example = "PJ")
        @NotBlank
        @Pattern(regexp = "PF|PJ", message = "Tipo pessoa deve ser PF ou PJ")
        private String tipoPessoa = "PJ";

        @Schema(description = "Razão social (PJ) ou nome completo (PF)",
                example = "Construtora Horizonte LTDA")
        @NotBlank(message = "Razão social é obrigatória")
        @Size(max = 200)
        private String razaoSocial;

        @Schema(description = "Nome fantasia da empresa", example = "Construtora Horizonte")
        @Size(max = 200)
        private String nomeFantasia;

        @Schema(description = "CPF (11 dígitos) ou CNPJ (18 caracteres com máscara)",
                example = "12.345.678/0001-90")
        @NotBlank(message = "CPF/CNPJ é obrigatório")
        private String cpfCnpj;

        @Schema(description = "Inscrição Estadual", example = "123.456.789-10")
        private String ie;

        @Schema(description = "Inscrição Municipal", example = "12345")
        private String im;

        @Schema(description = "E-mail para envio de notas fiscais e contato",
                example = "compras@construtora.com.br")
        @Email(message = "E-mail inválido")
        private String email;

        @Schema(example = "(41) 3333-4444")
        private String telefone;

        @Schema(example = "(41) 99999-8888")
        private String celular;

        @Schema(example = "80230-010")
        private String cep;

        @Schema(example = "Rua das Araucárias")
        private String logradouro;

        @Schema(example = "1500")
        private String numero;

        @Schema(example = "Sala 3")
        private String complemento;

        @Schema(example = "Batel")
        private String bairro;

        @Schema(example = "Curitiba")
        private String cidade;

        @Schema(description = "UF com 2 letras", example = "PR")
        @Size(min = 2, max = 2)
        private String uf;

        @Schema(description = "Limite de crédito em reais para compras a prazo",
                example = "15000.00")
        @DecimalMin("0.0")
        private BigDecimal limiteCredito;

        @Schema(description = "Prazo padrão de pagamento em dias", example = "30")
        @Min(0)
        private Integer diasPrazo;

        @Schema(description = "UUID da tabela de preço associada ao cliente",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        private UUID tabelaPrecoId;

        @Schema(description = "Observações internas sobre o cliente",
                example = "Cliente preferencial. Confirmar pedidos via WhatsApp.")
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
        private String im;
        private String email;
        private String telefone;
        private String celular;
        private String cep;
        private String logradouro;
        private String numero;
        private String complemento;
        private String bairro;
        private String cidade;
        private String uf;
        private BigDecimal limiteCredito;
        private Integer diasPrazo;
        private UUID tabelaPrecoId;
        private String tabelaPrecoNome;
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
