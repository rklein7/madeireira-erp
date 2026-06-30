package com.madeireira.erp.modules.cadastro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TabelaPrecoDTO {

    @Schema(description = "Dados para criação de uma tabela de preço")
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {

        @Schema(description = "Nome da tabela de preço, único e descritivo",
                example = "Tabela Atacado 2025")
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100)
        private String nome;

        @Schema(description = "Descrição com regras ou público-alvo da tabela",
                example = "Preços para revendas com volume acima de 50 chapas/mês")
        @Size(max = 255)
        private String descricao;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private UUID id;
        private String nome;
        private String descricao;
        private Boolean ativo;
        private List<ItemResponse> itens;
        private LocalDateTime criadoEm;
        private LocalDateTime atualizadoEm;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Resumo {
        private UUID id;
        private String nome;
        private String descricao;
        private Boolean ativo;
        private int quantidadeItens;
    }

    @Schema(description = "Dados para vincular um produto e seu preço a uma tabela")
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ItemRequest {

        @Schema(description = "UUID do produto a ser adicionado à tabela",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull(message = "Produto é obrigatório")
        private UUID produtoId;

        @Schema(description = "Preço de venda nesta tabela (pode diferir do preço padrão do produto)",
                example = "75.90")
        @NotNull(message = "Preço é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "Preço deve ser maior que zero")
        private BigDecimal preco;

        @Schema(description = "Percentual máximo de desconto que o vendedor pode conceder (0 a 100)",
                example = "5.00")
        @DecimalMin(value = "0.0", message = "Desconto máximo não pode ser negativo")
        @DecimalMax(value = "100.0", message = "Desconto máximo não pode ultrapassar 100%")
        private BigDecimal descontoMax;

        @Schema(description = "Data de início de vigência do preço (opcional)",
                example = "2025-01-01")
        private LocalDate vigenciaInicio;

        @Schema(description = "Data de fim de vigência do preço (opcional). Após essa data o preço deve ser revisado.",
                example = "2025-12-31")
        private LocalDate vigenciaFim;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ItemResponse {
        private UUID id;
        private UUID produtoId;
        private String produtoCodigo;
        private String produtoDescricao;
        private BigDecimal preco;
        private BigDecimal descontoMax;
        private LocalDate vigenciaInicio;
        private LocalDate vigenciaFim;
        private LocalDateTime criadoEm;
        private LocalDateTime atualizadoEm;
    }
}
