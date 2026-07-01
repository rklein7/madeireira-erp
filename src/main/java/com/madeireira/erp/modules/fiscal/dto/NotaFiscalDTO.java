package com.madeireira.erp.modules.fiscal.dto;

import com.madeireira.erp.modules.cadastro.entity.UnidadeMedida;
import com.madeireira.erp.modules.fiscal.entity.StatusNF;
import com.madeireira.erp.modules.fiscal.entity.TipoNF;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class NotaFiscalDTO {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ItemRequest {

        @NotNull(message = "Produto é obrigatório")
        private UUID produtoId;

        @NotNull(message = "Número do item é obrigatório")
        private Integer numeroItem;

        @NotNull(message = "Quantidade é obrigatória")
        @DecimalMin(value = "0.0001", message = "Quantidade deve ser maior que zero")
        private BigDecimal quantidade;

        @NotNull(message = "Valor unitário é obrigatório")
        private BigDecimal valorUnitario;

        // ICMS
        private String cstIcms;
        private BigDecimal aliqIcms;

        // IPI
        private String cstIpi;
        private BigDecimal aliqIpi;

        // PIS
        private String cstPis;
        private BigDecimal aliqPis;

        // COFINS
        private String cstCofins;
        private BigDecimal aliqCofins;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class EntradaRequest {

        @NotNull(message = "Fornecedor é obrigatório")
        private UUID fornecedorId;

        @NotBlank(message = "Número da nota é obrigatório")
        @Size(max = 20)
        private String numero;

        @Builder.Default
        private String serie = "1";

        @NotBlank(message = "CFOP é obrigatório")
        private String cfop;

        @NotNull(message = "Data de emissão é obrigatória")
        private LocalDate dataEmissao;

        @NotNull(message = "Data de entrada é obrigatória")
        private LocalDate dataEntradaSaida;

        private String naturezaOperacao;

        @Size(max = 44)
        private String chaveAcesso;

        private BigDecimal valorFrete;
        private BigDecimal valorSeguro;
        private BigDecimal valorDesconto;

        private String observacoes;

        @NotNull(message = "Lista de itens é obrigatória")
        @NotEmpty(message = "A nota deve ter ao menos um item")
        @Valid
        private List<ItemRequest> itens;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SaidaRequest {

        @NotNull(message = "Pedido é obrigatório")
        private UUID pedidoId;

        @NotBlank(message = "Número da nota é obrigatório")
        private String numero;

        @Builder.Default
        private String serie = "1";

        @NotBlank(message = "CFOP é obrigatório")
        private String cfop;

        @NotNull(message = "Data de emissão é obrigatória")
        private LocalDate dataEmissao;

        private String naturezaOperacao;
        private String observacoes;

        @NotNull(message = "Lista de itens é obrigatória")
        @NotEmpty(message = "A nota deve ter ao menos um item")
        @Valid
        private List<ItemRequest> itens;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ItemResponse {

        private UUID id;

        private UUID produtoId;
        private String produtoCodigo;
        private String produtoDescricao;
        private UnidadeMedida unidadeMedida;

        private Integer numeroItem;
        private BigDecimal quantidade;
        private BigDecimal valorUnitario;
        private BigDecimal valorTotal;

        // ICMS
        private String cstIcms;
        private BigDecimal aliqIcms;
        private BigDecimal valorIcms;

        // IPI
        private String cstIpi;
        private BigDecimal aliqIpi;
        private BigDecimal valorIpi;

        // PIS
        private String cstPis;
        private BigDecimal aliqPis;
        private BigDecimal valorPis;

        // COFINS
        private String cstCofins;
        private BigDecimal aliqCofins;
        private BigDecimal valorCofins;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {

        private UUID id;
        private TipoNF tipo;
        private StatusNF status;
        private String numero;
        private String serie;
        private String cfop;
        private String chaveAcesso;

        private LocalDate dataEmissao;
        private LocalDate dataEntradaSaida;
        private String naturezaOperacao;

        private UUID fornecedorId;
        private String fornecedorNome;

        private UUID clienteId;
        private String clienteNome;

        private UUID pedidoId;
        private String pedidoNumero;

        private BigDecimal valorProdutos;
        private BigDecimal valorFrete;
        private BigDecimal valorSeguro;
        private BigDecimal valorDesconto;
        private BigDecimal valorIcms;
        private BigDecimal valorIpi;
        private BigDecimal valorPis;
        private BigDecimal valorCofins;
        private BigDecimal valorTotal;

        private String observacoes;

        private List<ItemResponse> itens;

        private LocalDateTime criadoEm;
        private LocalDateTime atualizadoEm;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Resumo {

        private UUID id;
        private TipoNF tipo;
        private StatusNF status;
        private String numero;
        private String serie;
        private String cfop;

        private LocalDate dataEmissao;
        private String fornecedorNome;
        private String clienteNome;

        private BigDecimal valorTotal;
        private int totalItens;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ResumoTributos {

        private String periodo;

        private BigDecimal totalProdutos;
        private BigDecimal totalIcms;
        private BigDecimal totalIpi;
        private BigDecimal totalPis;
        private BigDecimal totalCofins;
        private BigDecimal totalNF;
    }
}
