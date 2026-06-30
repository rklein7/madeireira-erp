package com.madeireira.erp.modules.cadastro.service;

import com.madeireira.erp.modules.cadastro.dto.ProdutoDTO;
import com.madeireira.erp.modules.cadastro.entity.UnidadeMedida;
import com.madeireira.erp.modules.cadastro.repository.CategoriaRepository;
import com.madeireira.erp.modules.cadastro.repository.ProdutoRepository;
import com.madeireira.erp.modules.cadastro.repository.TabelaPrecoItemRepository;
import com.madeireira.erp.shared.exception.BusinessException;
import com.madeireira.erp.shared.exception.NotFoundException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ProdutoServiceTest {

    @Autowired private ProdutoService produtoService;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private TabelaPrecoItemRepository tabelaPrecoItemRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private Validator validator;

    @BeforeEach
    void limpar() {
        tabelaPrecoItemRepository.deleteAll();
        produtoRepository.deleteAll();
        categoriaRepository.deleteAll();
    }

    private ProdutoDTO.Request produtoValido() {
        return ProdutoDTO.Request.builder()
                .codigo("PROD-001")
                .descricao("Tábua de Pinus 15cm")
                .unidadeMedida(UnidadeMedida.M2)
                .precoVenda(new BigDecimal("45.90"))
                .estoqueMinimo(new BigDecimal("10.00"))
                .build();
    }

    @Test
    void criar_comDadosValidos_retornaIdGerado() {
        var response = produtoService.criar(produtoValido());

        assertNotNull(response.getId());
        assertEquals("PROD-001", response.getCodigo());
        assertEquals("Tábua de Pinus 15cm", response.getDescricao());
        assertTrue(response.getAtivo());
        assertEquals(0, response.getPrecoVenda().compareTo(new BigDecimal("45.90")));
    }

    @Test
    void criar_comCodigoDuplicado_lancaBusinessException() {
        produtoService.criar(produtoValido());

        assertThrows(BusinessException.class, () -> produtoService.criar(produtoValido()));
    }

    @Test
    void criar_semPrecoDeVenda_reprovaNaValidacaoDoDTO() {
        var request = ProdutoDTO.Request.builder()
                .codigo("PROD-SEM-PRECO")
                .descricao("Produto sem preço")
                .unidadeMedida(UnidadeMedida.M2)
                .build();

        var violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("precoVenda")));
    }

    @Test
    void buscarPorId_idInexistente_lancaNotFoundException() {
        assertThrows(NotFoundException.class, () -> produtoService.buscarPorId(UUID.randomUUID()));
    }

    @Test
    void atualizar_refleteMudancas() {
        var criado = produtoService.criar(produtoValido());

        var novoRequest = ProdutoDTO.Request.builder()
                .codigo("PROD-001")
                .descricao("Tábua de Pinus 20cm ATUALIZADO")
                .unidadeMedida(UnidadeMedida.PECA)
                .precoVenda(new BigDecimal("55.00"))
                .build();

        var atualizado = produtoService.atualizar(criado.getId(), novoRequest);

        assertEquals(criado.getId(), atualizado.getId());
        assertEquals("Tábua de Pinus 20cm ATUALIZADO", atualizado.getDescricao());
        assertEquals(UnidadeMedida.PECA, atualizado.getUnidadeMedida());
        assertEquals(0, atualizado.getPrecoVenda().compareTo(new BigDecimal("55.00")));
    }

    @Test
    void inativar_marcaAtivoFalso() {
        var criado = produtoService.criar(produtoValido());

        produtoService.inativar(criado.getId());

        var produto = produtoService.buscarPorId(criado.getId());
        assertFalse(produto.getAtivo());
    }

    @Test
    void alertasEstoqueMinimo_retornaProdutosAbaixoDoMinimo() {
        // estoqueMinimo = 10, estoqueAtual = 0 (padrão) → abaixo do mínimo
        produtoService.criar(produtoValido());

        // estoqueMinimo = 0, estoqueAtual = 0 → NÃO abaixo do mínimo
        produtoService.criar(ProdutoDTO.Request.builder()
                .codigo("PROD-002")
                .descricao("Produto com estoque OK")
                .unidadeMedida(UnidadeMedida.PECA)
                .precoVenda(new BigDecimal("10.00"))
                .estoqueMinimo(BigDecimal.ZERO)
                .build());

        var alertas = produtoService.alertasEstoqueMinimo();

        assertEquals(1, alertas.size());
        assertEquals("PROD-001", alertas.get(0).getCodigo());
        assertTrue(alertas.get(0).isAbaixoDoMinimo());
    }
}
