package com.madeireira.erp.modules.cadastro.service;

import com.madeireira.erp.modules.cadastro.dto.ProdutoDTO;
import com.madeireira.erp.modules.cadastro.entity.Categoria;
import com.madeireira.erp.modules.cadastro.entity.Produto;
import com.madeireira.erp.modules.cadastro.repository.CategoriaRepository;
import com.madeireira.erp.modules.cadastro.repository.ProdutoRepository;
import com.madeireira.erp.shared.exception.BusinessException;
import com.madeireira.erp.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public Page<ProdutoDTO.Resumo> listar(String busca, Pageable pageable) {
        Page<Produto> page = (busca != null && !busca.isBlank())
                ? produtoRepository.buscar(busca, pageable)
                : produtoRepository.findByAtivoTrue(pageable);
        return page.map(this::toResumo);
    }

    @Transactional(readOnly = true)
    public ProdutoDTO.Response buscarPorId(UUID id) {
        return toResponse(findById(id));
    }

    @Transactional
    public ProdutoDTO.Response criar(ProdutoDTO.Request request) {
        if (produtoRepository.existsByCodigo(request.getCodigo())) {
            throw new BusinessException("Já existe um produto com o código: " + request.getCodigo());
        }
        Produto produto = fromRequest(request, new Produto());
        return toResponse(produtoRepository.save(produto));
    }

    @Transactional
    public ProdutoDTO.Response atualizar(UUID id, ProdutoDTO.Request request) {
        Produto produto = findById(id);
        if (!produto.getCodigo().equals(request.getCodigo()) &&
            produtoRepository.existsByCodigo(request.getCodigo())) {
            throw new BusinessException("Já existe um produto com o código: " + request.getCodigo());
        }
        fromRequest(request, produto);
        return toResponse(produtoRepository.save(produto));
    }

    @Transactional
    public void inativar(UUID id) {
        Produto produto = findById(id);
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }

    @Transactional(readOnly = true)
    public List<ProdutoDTO.Resumo> alertasEstoqueMinimo() {
        return produtoRepository.findAbaixoDoEstoqueMinimo()
                .stream().map(this::toResumo).collect(Collectors.toList());
    }

    private Produto findById(UUID id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado: " + id));
    }

    private Produto fromRequest(ProdutoDTO.Request req, Produto produto) {
        produto.setCodigo(req.getCodigo());
        produto.setDescricao(req.getDescricao());
        produto.setDescricaoCurta(req.getDescricaoCurta());
        produto.setUnidadeMedida(req.getUnidadeMedida());
        produto.setPrecoVenda(req.getPrecoVenda());
        produto.setPrecoCusto(req.getPrecoCusto());
        produto.setEstoqueMinimo(req.getEstoqueMinimo() != null ? req.getEstoqueMinimo() : java.math.BigDecimal.ZERO);
        produto.setEstoqueMaximo(req.getEstoqueMaximo());
        produto.setNcm(req.getNcm());
        produto.setPesoUnitario(req.getPesoUnitario());
        produto.setLarguraCm(req.getLarguraCm());
        produto.setComprimentoCm(req.getComprimentoCm());
        produto.setEspessuraMm(req.getEspessuraMm());
        produto.setObservacoes(req.getObservacoes());
        if (req.getCategoriaId() != null) {
            Categoria cat = categoriaRepository.findById(req.getCategoriaId())
                    .orElseThrow(() -> new NotFoundException("Categoria não encontrada"));
            produto.setCategoria(cat);
        }
        return produto;
    }

    private ProdutoDTO.Response toResponse(Produto p) {
        return ProdutoDTO.Response.builder()
                .id(p.getId())
                .codigo(p.getCodigo())
                .descricao(p.getDescricao())
                .descricaoCurta(p.getDescricaoCurta())
                .unidadeMedida(p.getUnidadeMedida())
                .unidadeSimbolo(p.getUnidadeMedida().getSimbolo())
                .precoVenda(p.getPrecoVenda())
                .precoCusto(p.getPrecoCusto())
                .estoqueAtual(p.getEstoqueAtual())
                .estoqueMinimo(p.getEstoqueMinimo())
                .estoqueMaximo(p.getEstoqueMaximo())
                .abaixoDoMinimo(p.isAbaixoDoMinimo())
                .ncm(p.getNcm())
                .pesoUnitario(p.getPesoUnitario())
                .larguraCm(p.getLarguraCm())
                .comprimentoCm(p.getComprimentoCm())
                .espessuraMm(p.getEspessuraMm())
                .categoriaId(p.getCategoria() != null ? p.getCategoria().getId() : null)
                .categoriaNome(p.getCategoria() != null ? p.getCategoria().getNome() : null)
                .ativo(p.getAtivo())
                .observacoes(p.getObservacoes())
                .criadoEm(p.getCriadoEm())
                .atualizadoEm(p.getAtualizadoEm())
                .build();
    }

    private ProdutoDTO.Resumo toResumo(Produto p) {
        return ProdutoDTO.Resumo.builder()
                .id(p.getId())
                .codigo(p.getCodigo())
                .descricao(p.getDescricao())
                .unidadeMedida(p.getUnidadeMedida())
                .unidadeSimbolo(p.getUnidadeMedida().getSimbolo())
                .precoVenda(p.getPrecoVenda())
                .estoqueAtual(p.getEstoqueAtual())
                .abaixoDoMinimo(p.isAbaixoDoMinimo())
                .ativo(p.getAtivo())
                .build();
    }
}
