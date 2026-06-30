package com.madeireira.erp.modules.cadastro.service;

import com.madeireira.erp.modules.cadastro.dto.TabelaPrecoDTO;
import com.madeireira.erp.modules.cadastro.entity.Produto;
import com.madeireira.erp.modules.cadastro.entity.TabelaPreco;
import com.madeireira.erp.modules.cadastro.entity.TabelaPrecoItem;
import com.madeireira.erp.modules.cadastro.repository.ProdutoRepository;
import com.madeireira.erp.modules.cadastro.repository.TabelaPrecoItemRepository;
import com.madeireira.erp.modules.cadastro.repository.TabelaPrecoRepository;
import com.madeireira.erp.shared.exception.BusinessException;
import com.madeireira.erp.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TabelaPrecoService {

    private final TabelaPrecoRepository tabelaPrecoRepository;
    private final TabelaPrecoItemRepository tabelaPrecoItemRepository;
    private final ProdutoRepository produtoRepository;

    @Transactional(readOnly = true)
    public List<TabelaPrecoDTO.Resumo> listar() {
        return tabelaPrecoRepository.findByAtivoTrueOrderByNome()
                .stream().map(this::toResumo).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TabelaPrecoDTO.Response buscarPorId(UUID id) {
        return toResponse(findById(id));
    }

    @Transactional
    public TabelaPrecoDTO.Response criar(TabelaPrecoDTO.Request request) {
        TabelaPreco tabela = TabelaPreco.builder()
                .nome(request.getNome())
                .descricao(request.getDescricao())
                .build();
        return toResponse(tabelaPrecoRepository.save(tabela));
    }

    @Transactional
    public TabelaPrecoDTO.ItemResponse adicionarItem(UUID tabelaId, TabelaPrecoDTO.ItemRequest request) {
        TabelaPreco tabela = findById(tabelaId);

        Produto produto = produtoRepository.findById(request.getProdutoId())
                .orElseThrow(() -> new NotFoundException("Produto não encontrado: " + request.getProdutoId()));

        if (tabelaPrecoItemRepository.existsByTabelaIdAndProdutoId(tabelaId, request.getProdutoId())) {
            throw new BusinessException("Produto já cadastrado nesta tabela de preços: " + produto.getCodigo());
        }

        TabelaPrecoItem item = TabelaPrecoItem.builder()
                .tabela(tabela)
                .produto(produto)
                .preco(request.getPreco())
                .descontoMax(request.getDescontoMax() != null ? request.getDescontoMax() : BigDecimal.ZERO)
                .vigenciaInicio(request.getVigenciaInicio())
                .vigenciaFim(request.getVigenciaFim())
                .build();

        tabela.getItens().add(item);
        tabelaPrecoRepository.save(tabela);

        return toItemResponse(item);
    }

    @Transactional
    public void removerItem(UUID tabelaId, UUID itemId) {
        TabelaPreco tabela = findById(tabelaId);

        TabelaPrecoItem item = tabela.getItens().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Item não encontrado na tabela: " + itemId));

        tabela.getItens().remove(item);
        tabelaPrecoRepository.save(tabela);
    }

    @Transactional
    public void inativar(UUID id) {
        TabelaPreco tabela = findById(id);
        tabela.setAtivo(false);
        tabelaPrecoRepository.save(tabela);
    }

    private TabelaPreco findById(UUID id) {
        return tabelaPrecoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tabela de preços não encontrada: " + id));
    }

    private TabelaPrecoDTO.Response toResponse(TabelaPreco t) {
        List<TabelaPrecoDTO.ItemResponse> itens = t.getItens().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        return TabelaPrecoDTO.Response.builder()
                .id(t.getId())
                .nome(t.getNome())
                .descricao(t.getDescricao())
                .ativo(t.getAtivo())
                .itens(itens)
                .criadoEm(t.getCriadoEm())
                .atualizadoEm(t.getAtualizadoEm())
                .build();
    }

    private TabelaPrecoDTO.Resumo toResumo(TabelaPreco t) {
        return TabelaPrecoDTO.Resumo.builder()
                .id(t.getId())
                .nome(t.getNome())
                .descricao(t.getDescricao())
                .ativo(t.getAtivo())
                .quantidadeItens(t.getItens().size())
                .build();
    }

    private TabelaPrecoDTO.ItemResponse toItemResponse(TabelaPrecoItem i) {
        return TabelaPrecoDTO.ItemResponse.builder()
                .id(i.getId())
                .produtoId(i.getProduto().getId())
                .produtoCodigo(i.getProduto().getCodigo())
                .produtoDescricao(i.getProduto().getDescricao())
                .preco(i.getPreco())
                .descontoMax(i.getDescontoMax())
                .vigenciaInicio(i.getVigenciaInicio())
                .vigenciaFim(i.getVigenciaFim())
                .criadoEm(i.getCriadoEm())
                .atualizadoEm(i.getAtualizadoEm())
                .build();
    }
}
