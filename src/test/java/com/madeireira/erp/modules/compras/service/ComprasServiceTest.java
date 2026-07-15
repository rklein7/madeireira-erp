package com.madeireira.erp.modules.compras.service;

import com.madeireira.erp.modules.cadastro.dto.FornecedorDTO;
import com.madeireira.erp.modules.cadastro.dto.ProdutoDTO;
import com.madeireira.erp.modules.cadastro.entity.UnidadeMedida;
import com.madeireira.erp.modules.cadastro.repository.FornecedorRepository;
import com.madeireira.erp.modules.cadastro.repository.ProdutoRepository;
import com.madeireira.erp.modules.cadastro.repository.TabelaPrecoItemRepository;
import com.madeireira.erp.modules.cadastro.service.FornecedorService;
import com.madeireira.erp.modules.cadastro.service.ProdutoService;
import com.madeireira.erp.modules.compras.dto.PedidoCompraDTO;
import com.madeireira.erp.modules.compras.entity.StatusPedidoCompra;
import com.madeireira.erp.modules.compras.repository.ItemPedidoCompraRepository;
import com.madeireira.erp.modules.compras.repository.PedidoCompraRepository;
import com.madeireira.erp.modules.vendas.entity.CondicaoPagamento;
import com.madeireira.erp.shared.exception.BusinessException;
import com.madeireira.erp.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ComprasServiceTest {

    @Autowired private ComprasService comprasService;
    @Autowired private PedidoCompraRepository pedidoCompraRepository;
    @Autowired private ItemPedidoCompraRepository itemPedidoCompraRepository;
    @Autowired private FornecedorService fornecedorService;
    @Autowired private FornecedorRepository fornecedorRepository;
    @Autowired private ProdutoService produtoService;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private TabelaPrecoItemRepository tabelaPrecoItemRepository;

    @BeforeEach
    void limpar() {
        itemPedidoCompraRepository.deleteAll();
        pedidoCompraRepository.deleteAll();
        tabelaPrecoItemRepository.deleteAll();
        produtoRepository.deleteAll();
        fornecedorRepository.deleteAll();
    }

    private FornecedorDTO.Response criarFornecedor() {
        return fornecedorService.criar(FornecedorDTO.Request.builder()
                .tipoPessoa("PJ")
                .razaoSocial("Madeiras do Sul LTDA")
                .cpfCnpj("98.765.432/0001-10")
                .build());
    }

    private ProdutoDTO.Response criarProduto() {
        return produtoService.criar(ProdutoDTO.Request.builder()
                .codigo("PROD-CMP-001")
                .descricao("Tábua de Cedro 10cm")
                .unidadeMedida(UnidadeMedida.M2)
                .precoVenda(new BigDecimal("60.00"))
                .estoqueMinimo(new BigDecimal("5.00"))
                .build());
    }

    private PedidoCompraDTO.Request pedidoValido(UUID fornecedorId, UUID produtoId) {
        return PedidoCompraDTO.Request.builder()
                .fornecedorId(fornecedorId)
                .condicaoPagamento(CondicaoPagamento.A_VISTA)
                .parcelas(1)
                .valorFrete(BigDecimal.ZERO)
                .itens(List.of(PedidoCompraDTO.ItemRequest.builder()
                        .produtoId(produtoId)
                        .numeroItem(1)
                        .quantidade(new BigDecimal("10.00"))
                        .precoUnitario(new BigDecimal("50.00"))
                        .build()))
                .build();
    }

    // -------------------------------------------------------------------------
    // Teste 1: Criar com dados válidos gera número no formato CMP-AAAA-NNNNN
    // -------------------------------------------------------------------------

    @Test
    void criar_comDadosValidos_retornaNumeroNoFormatoCMP() {
        var fornecedor = criarFornecedor();
        var produto = criarProduto();

        var response = comprasService.criar(pedidoValido(fornecedor.getId(), produto.getId()));

        assertNotNull(response.getId());
        assertTrue(response.getNumero().startsWith("CMP-"),
                "Número deve iniciar com CMP-, mas foi: " + response.getNumero());
        assertEquals(StatusPedidoCompra.RASCUNHO, response.getStatus());
        assertEquals("Madeiras do Sul LTDA", response.getFornecedorNome());
        assertEquals(0, response.getValorTotal().compareTo(new BigDecimal("500.00")));
    }

    // -------------------------------------------------------------------------
    // Teste 2: Criar com fornecedor inexistente lança NotFoundException
    // -------------------------------------------------------------------------

    @Test
    void criar_comFornecedorInexistente_lancaNotFoundException() {
        var produto = criarProduto();
        var req = pedidoValido(UUID.randomUUID(), produto.getId());

        assertThrows(NotFoundException.class, () -> comprasService.criar(req));
    }

    // -------------------------------------------------------------------------
    // Teste 3: Criar com produto inexistente lança NotFoundException
    // -------------------------------------------------------------------------

    @Test
    void criar_comProdutoInexistente_lancaNotFoundException() {
        var fornecedor = criarFornecedor();
        var req = pedidoValido(fornecedor.getId(), UUID.randomUUID());

        assertThrows(NotFoundException.class, () -> comprasService.criar(req));
    }

    // -------------------------------------------------------------------------
    // Teste 4: Confirmar pedido em RASCUNHO muda status para CONFIRMADO
    // -------------------------------------------------------------------------

    @Test
    void confirmarPedido_deRascunho_mudaStatusParaConfirmado() {
        var fornecedor = criarFornecedor();
        var produto = criarProduto();
        var criado = comprasService.criar(pedidoValido(fornecedor.getId(), produto.getId()));

        var confirmado = comprasService.confirmarPedido(criado.getId());

        assertEquals(StatusPedidoCompra.CONFIRMADO, confirmado.getStatus());
        assertEquals("Confirmado", confirmado.getStatusDescricao());
    }

    // -------------------------------------------------------------------------
    // Teste 5: Confirmar pedido que não está em RASCUNHO lança BusinessException
    // -------------------------------------------------------------------------

    @Test
    void confirmarPedido_statusNaoRascunho_lancaBusinessException() {
        var fornecedor = criarFornecedor();
        var produto = criarProduto();
        var criado = comprasService.criar(pedidoValido(fornecedor.getId(), produto.getId()));
        comprasService.confirmarPedido(criado.getId()); // agora está CONFIRMADO

        assertThrows(BusinessException.class, () -> comprasService.confirmarPedido(criado.getId()));
    }

    // -------------------------------------------------------------------------
    // Teste 6: Cancelar pedido em RASCUNHO muda status para CANCELADO
    // -------------------------------------------------------------------------

    @Test
    void cancelarPedido_deRascunho_mudaStatusParaCancelado() {
        var fornecedor = criarFornecedor();
        var produto = criarProduto();
        var criado = comprasService.criar(pedidoValido(fornecedor.getId(), produto.getId()));

        var cancelado = comprasService.cancelarPedido(criado.getId());

        assertEquals(StatusPedidoCompra.CANCELADO, cancelado.getStatus());
    }

    // -------------------------------------------------------------------------
    // Teste 7: Cancelar pedido RECEBIDO lança BusinessException
    // -------------------------------------------------------------------------

    @Test
    void cancelarPedido_deRecebido_lancaBusinessException() {
        var fornecedor = criarFornecedor();
        var produto = criarProduto();
        var criado = comprasService.criar(pedidoValido(fornecedor.getId(), produto.getId()));
        comprasService.confirmarPedido(criado.getId());
        comprasService.marcarComoRecebido(criado.getId()); // CONFIRMADO → RECEBIDO

        assertThrows(BusinessException.class, () -> comprasService.cancelarPedido(criado.getId()));
    }

    // -------------------------------------------------------------------------
    // Teste 8: marcarComoRecebido e reverterRecebimento funcionam corretamente
    // -------------------------------------------------------------------------

    @Test
    void marcarComoRecebidoEReverter_cicloCOMPLETO() {
        var fornecedor = criarFornecedor();
        var produto = criarProduto();
        var criado = comprasService.criar(pedidoValido(fornecedor.getId(), produto.getId()));
        comprasService.confirmarPedido(criado.getId());

        comprasService.marcarComoRecebido(criado.getId());
        var aposRecebimento = comprasService.buscarPorId(criado.getId());
        assertEquals(StatusPedidoCompra.RECEBIDO, aposRecebimento.getStatus());

        comprasService.reverterRecebimento(criado.getId());
        var aposReversao = comprasService.buscarPorId(criado.getId());
        assertEquals(StatusPedidoCompra.CONFIRMADO, aposReversao.getStatus());
    }
}
