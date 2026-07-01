package com.madeireira.erp.modules.estoque.service;

import com.madeireira.erp.modules.auth.entity.Usuario;
import com.madeireira.erp.modules.auth.repository.UsuarioRepository;
import com.madeireira.erp.modules.cadastro.entity.Fornecedor;
import com.madeireira.erp.modules.cadastro.entity.Produto;
import com.madeireira.erp.modules.cadastro.repository.FornecedorRepository;
import com.madeireira.erp.modules.cadastro.repository.ProdutoRepository;
import com.madeireira.erp.modules.estoque.dto.MovimentoEstoqueDTO;
import com.madeireira.erp.modules.estoque.entity.MovimentoEstoque;
import com.madeireira.erp.modules.estoque.entity.TipoMovimento;
import com.madeireira.erp.modules.estoque.repository.MovimentoEstoqueRepository;
import com.madeireira.erp.shared.exception.BusinessException;
import com.madeireira.erp.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final ProdutoRepository produtoRepository;
    private final MovimentoEstoqueRepository movimentoEstoqueRepository;
    private final FornecedorRepository fornecedorRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public MovimentoEstoqueDTO.Response registrarMovimento(MovimentoEstoqueDTO.Request request) {
        Produto produto = produtoRepository.findById(request.getProdutoId())
                .orElseThrow(() -> new NotFoundException("Produto não encontrado: " + request.getProdutoId()));

        BigDecimal estoqueAtual = produto.getEstoqueAtual();
        BigDecimal quantidade = request.getQuantidade();

        if (request.getTipo() == TipoMovimento.SAIDA_MANUAL &&
                quantidade.compareTo(estoqueAtual) > 0) {
            throw new BusinessException(String.format(
                    "Estoque insuficiente. Saldo atual: %s, solicitado: %s",
                    estoqueAtual.toPlainString(), quantidade.toPlainString()));
        }

        BigDecimal novoSaldo = switch (request.getTipo()) {
            case ENTRADA_MANUAL, ENTRADA_NF -> estoqueAtual.add(quantidade);
            case SAIDA_MANUAL, SAIDA_PEDIDO -> estoqueAtual.subtract(quantidade);
            case AJUSTE -> quantidade; // quantidade = novo saldo absoluto
        };

        produto.setEstoqueAtual(novoSaldo);
        produtoRepository.save(produto);

        Fornecedor fornecedor = null;
        if (request.getFornecedorId() != null) {
            fornecedor = fornecedorRepository.findById(request.getFornecedorId())
                    .orElseThrow(() -> new NotFoundException("Fornecedor não encontrado: " + request.getFornecedorId()));
        }

        MovimentoEstoque movimento = MovimentoEstoque.builder()
                .produto(produto)
                .tipo(request.getTipo())
                .quantidade(quantidade)
                .custoUnitario(request.getCustoUnitario())
                .saldoApos(novoSaldo)
                .fornecedor(fornecedor)
                .documento(request.getDocumento())
                .observacoes(request.getObservacoes())
                .usuario(resolverUsuarioAtual())
                .build();

        return toResponse(movimentoEstoqueRepository.save(movimento));
    }

    @Transactional(readOnly = true)
    public Page<MovimentoEstoqueDTO.Response> listarMovimentos(
            UUID produtoId, TipoMovimento tipo,
            LocalDate de, LocalDate ate,
            Pageable pageable) {

        Pageable ordenado = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "criadoEm"));

        Page<MovimentoEstoque> page;

        if (de != null && ate != null) {
            page = movimentoEstoqueRepository.findByProdutoIdAndPeriodo(
                    produtoId,
                    de.atStartOfDay(),
                    ate.atTime(LocalTime.MAX),
                    ordenado);
        } else if (tipo != null) {
            page = movimentoEstoqueRepository.findByProdutoIdAndTipo(produtoId, tipo, ordenado);
        } else {
            page = movimentoEstoqueRepository.findByProdutoId(produtoId, ordenado);
        }

        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public MovimentoEstoqueDTO.SaldoProduto buscarSaldo(UUID produtoId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado: " + produtoId));

        var ultimo = movimentoEstoqueRepository.findFirstByProdutoIdOrderByCriadoEmDesc(produtoId);

        return MovimentoEstoqueDTO.SaldoProduto.builder()
                .produtoId(produto.getId())
                .produtoCodigo(produto.getCodigo())
                .produtoDescricao(produto.getDescricao())
                .unidadeMedida(produto.getUnidadeMedida())
                .unidadeSimbolo(produto.getUnidadeMedida().getSimbolo())
                .estoqueAtual(produto.getEstoqueAtual())
                .estoqueMinimo(produto.getEstoqueMinimo())
                .abaixoDoMinimo(produto.isAbaixoDoMinimo())
                .ultimoMovimento(ultimo.map(MovimentoEstoque::getCriadoEm).orElse(null))
                .tipoUltimoMovimento(ultimo.map(MovimentoEstoque::getTipo).orElse(null))
                .build();
    }

    @Transactional(readOnly = true)
    public Page<MovimentoEstoqueDTO.PosicaoEstoque> posicaoEstoque(Pageable pageable) {
        return produtoRepository.findByAtivoTrue(pageable).map(p ->
                MovimentoEstoqueDTO.PosicaoEstoque.builder()
                        .produtoId(p.getId())
                        .codigo(p.getCodigo())
                        .descricao(p.getDescricao())
                        .unidadeMedida(p.getUnidadeMedida())
                        .unidadeSimbolo(p.getUnidadeMedida().getSimbolo())
                        .estoqueAtual(p.getEstoqueAtual())
                        .estoqueMinimo(p.getEstoqueMinimo())
                        .estoqueMaximo(p.getEstoqueMaximo())
                        .abaixoDoMinimo(p.isAbaixoDoMinimo())
                        .precoVenda(p.getPrecoVenda())
                        .build());
    }

    private MovimentoEstoqueDTO.Response toResponse(MovimentoEstoque m) {
        BigDecimal custoTotal = null;
        if (m.getCustoUnitario() != null) {
            custoTotal = m.getCustoUnitario().multiply(m.getQuantidade());
        }

        return MovimentoEstoqueDTO.Response.builder()
                .id(m.getId())
                .produtoId(m.getProduto().getId())
                .produtoCodigo(m.getProduto().getCodigo())
                .produtoDescricao(m.getProduto().getDescricao())
                .produtoUnidade(m.getProduto().getUnidadeMedida())
                .tipo(m.getTipo())
                .tipoDescricao(m.getTipo().getDescricao())
                .quantidade(m.getQuantidade())
                .custoUnitario(m.getCustoUnitario())
                .custoTotal(custoTotal)
                .saldoApos(m.getSaldoApos())
                .fornecedorId(m.getFornecedor() != null ? m.getFornecedor().getId() : null)
                .fornecedorNome(m.getFornecedor() != null ? m.getFornecedor().getRazaoSocial() : null)
                .documento(m.getDocumento())
                .observacoes(m.getObservacoes())
                .usuarioId(m.getUsuario() != null ? m.getUsuario().getId() : null)
                .usuarioNome(m.getUsuario() != null ? m.getUsuario().getNome() : null)
                .criadoEm(m.getCriadoEm())
                .atualizadoEm(m.getAtualizadoEm())
                .build();
    }

    private Usuario resolverUsuarioAtual() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        return usuarioRepository.findByEmail(auth.getName()).orElse(null);
    }
}
