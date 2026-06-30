package com.madeireira.erp.modules.cadastro.dto;

import com.madeireira.erp.modules.cadastro.entity.UnidadeMedida;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProdutoDTO {

    @Schema(description = "Dados para cadastro ou atualização de produto")
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {

        @Schema(description = "Código único do produto para identificação rápida no estoque",
                example = "PIH-1220-15")
        @NotBlank(message = "Código é obrigatório")
        @Size(max = 30)
        private String codigo;

        @Schema(description = "Descrição completa do produto",
                example = "Pinus Homogeneizado 1220x2440x15mm")
        @NotBlank(message = "Descrição é obrigatória")
        private String descricao;

        @Schema(description = "Descrição resumida para exibição em listas e etiquetas",
                example = "Pinus 15mm")
        @Size(max = 100)
        private String descricaoCurta;

        @Schema(description = "Unidade de medida: M2 (metro quadrado), M3 (metro cúbico), " +
                "KG (quilograma), PECA (peça), ML (metro linear), ROLO, DUZIA, CENTO",
                example = "M2")
        @NotNull(message = "Unidade de medida é obrigatória")
        private UnidadeMedida unidadeMedida;

        @Schema(description = "Preço de venda unitário na unidade de medida informada",
                example = "89.90")
        @DecimalMin(value = "0.0", inclusive = false, message = "Preço de venda deve ser maior que zero")
        @NotNull(message = "Preço de venda é obrigatório")
        private BigDecimal precoVenda;

        @Schema(description = "Preço de custo unitário (opcional, usado para cálculo de margem)",
                example = "62.50")
        private BigDecimal precoCusto;

        @Schema(description = "Estoque mínimo que dispara alerta de reposição",
                example = "50.0000")
        @DecimalMin("0.0")
        private BigDecimal estoqueMinimo;

        @Schema(description = "Estoque máximo para controle de compras",
                example = "500.0000")
        @DecimalMin("0.0")
        private BigDecimal estoqueMaximo;

        @Schema(description = "Código NCM (Nomenclatura Comum do Mercosul) para emissão fiscal",
                example = "44071000")
        @Size(max = 10)
        private String ncm;

        @Schema(description = "Peso unitário em quilogramas",
                example = "18.5")
        private BigDecimal pesoUnitario;

        @Schema(description = "Largura em centímetros",
                example = "122.0")
        private BigDecimal larguraCm;

        @Schema(description = "Comprimento em centímetros",
                example = "244.0")
        private BigDecimal comprimentoCm;

        @Schema(description = "Espessura em milímetros",
                example = "15.0")
        private BigDecimal espessuraMm;

        @Schema(description = "UUID da categoria do produto",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        private UUID categoriaId;

        @Schema(description = "Observações internas sobre o produto",
                example = "Lote importado. Verificar umidade antes do uso estrutural.")
        private String observacoes;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private UUID id;
        private String codigo;
        private String descricao;
        private String descricaoCurta;
        private UnidadeMedida unidadeMedida;
        private String unidadeSimbolo;
        private BigDecimal precoVenda;
        private BigDecimal precoCusto;
        private BigDecimal estoqueAtual;
        private BigDecimal estoqueMinimo;
        private BigDecimal estoqueMaximo;
        private boolean abaixoDoMinimo;
        private String ncm;
        private BigDecimal pesoUnitario;
        private BigDecimal larguraCm;
        private BigDecimal comprimentoCm;
        private BigDecimal espessuraMm;
        private UUID categoriaId;
        private String categoriaNome;
        private Boolean ativo;
        private String observacoes;
        private LocalDateTime criadoEm;
        private LocalDateTime atualizadoEm;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Resumo {
        private UUID id;
        private String codigo;
        private String descricao;
        private UnidadeMedida unidadeMedida;
        private String unidadeSimbolo;
        private BigDecimal precoVenda;
        private BigDecimal estoqueAtual;
        private boolean abaixoDoMinimo;
        private Boolean ativo;
    }
}
