package com.madeireira.erp.modules.compras.service;

import com.madeireira.erp.modules.cadastro.repository.FornecedorRepository;
import com.madeireira.erp.modules.cadastro.repository.ProdutoRepository;
import com.madeireira.erp.modules.compras.dto.PedidoCompraDTO;
import com.madeireira.erp.modules.compras.entity.ItemPedidoCompra;
import com.madeireira.erp.modules.compras.entity.PedidoCompra;
import com.madeireira.erp.modules.compras.entity.StatusPedidoCompra;
import com.madeireira.erp.modules.compras.repository.PedidoCompraRepository;
import com.madeireira.erp.shared.exception.BusinessException;
import com.madeireira.erp.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ComprasService {

    private final PedidoCompraRepository pedidoCompraRepository;
    private final FornecedorRepository fornecedorRepository;
    private final ProdutoRepository produtoRepository;

    // -------------------------------------------------------------------------
    // Criar
    // -------------------------------------------------------------------------

    @Transactional
    public PedidoCompraDTO.Response criar(PedidoCompraDTO.Request req) {
        var fornecedor = fornecedorRepository.findById(req.getFornecedorId())
                .orElseThrow(() -> new NotFoundException("Fornecedor não encontrado: " + req.getFornecedorId()));

        String numero = String.format("CMP-%d-%05d",
                LocalDate.now().getYear(), pedidoCompraRepository.nextNumero());

        PedidoCompra pedido = PedidoCompra.builder()
                .numero(numero)
                .fornecedor(fornecedor)
                .condicaoPagamento(req.getCondicaoPagamento())
                .parcelas(req.getParcelas() != null ? req.getParcelas() : 1)
                .valorFrete(req.getValorFrete() != null ? req.getValorFrete() : java.math.BigDecimal.ZERO)
                .observacoes(req.getObservacoes())
                .build();

        List<ItemPedidoCompra> itens = new ArrayList<>();
        for (PedidoCompraDTO.ItemRequest itemReq : req.getItens()) {
            var produto = produtoRepository.findById(itemReq.getProdutoId())
                    .orElseThrow(() -> new NotFoundException("Produto não encontrado: " + itemReq.getProdutoId()));

            ItemPedidoCompra item = ItemPedidoCompra.builder()
                    .pedidoCompra(pedido)
                    .produto(produto)
                    .numeroItem(itemReq.getNumeroItem())
                    .quantidade(itemReq.getQuantidade())
                    .precoUnitario(itemReq.getPrecoUnitario())
                    .build();
            item.calcularTotal();
            itens.add(item);
        }

        pedido.getItens().addAll(itens);
        pedido.calcularTotal();

        return toResponse(pedidoCompraRepository.save(pedido));
    }

    // -------------------------------------------------------------------------
    // Confirmar
    // -------------------------------------------------------------------------

    @Transactional
    public PedidoCompraDTO.Response confirmarPedido(UUID id) {
        PedidoCompra pedido = buscarEntidade(id);

        if (pedido.getStatus() != StatusPedidoCompra.RASCUNHO) {
            throw new BusinessException(
                    "Pedido de compra não pode ser confirmado. Status atual: "
                    + pedido.getStatus().getDescricao());
        }

        pedido.setStatus(StatusPedidoCompra.CONFIRMADO);
        return toResponse(pedidoCompraRepository.save(pedido));
    }

    // -------------------------------------------------------------------------
    // Cancelar
    // -------------------------------------------------------------------------

    @Transactional
    public PedidoCompraDTO.Response cancelarPedido(UUID id) {
        PedidoCompra pedido = buscarEntidade(id);

        if (!pedido.getStatus().podeCancelar()) {
            throw new BusinessException(
                    "Pedido de compra não pode ser cancelado. Status atual: "
                    + pedido.getStatus().getDescricao());
        }

        pedido.setStatus(StatusPedidoCompra.CANCELADO);
        return toResponse(pedidoCompraRepository.save(pedido));
    }

    // -------------------------------------------------------------------------
    // Atualizar (somente RASCUNHO)
    // -------------------------------------------------------------------------

    @Transactional
    public PedidoCompraDTO.Response atualizar(UUID id, PedidoCompraDTO.Request req) {
        PedidoCompra pedido = buscarEntidade(id);

        if (pedido.getStatus() != StatusPedidoCompra.RASCUNHO) {
            throw new BusinessException(
                    "Pedido de compra só pode ser editado em RASCUNHO. Status atual: "
                    + pedido.getStatus().getDescricao());
        }

        var fornecedor = fornecedorRepository.findById(req.getFornecedorId())
                .orElseThrow(() -> new NotFoundException("Fornecedor não encontrado: " + req.getFornecedorId()));

        pedido.setFornecedor(fornecedor);
        pedido.setCondicaoPagamento(req.getCondicaoPagamento());
        pedido.setParcelas(req.getParcelas() != null ? req.getParcelas() : 1);
        pedido.setValorFrete(req.getValorFrete() != null ? req.getValorFrete() : java.math.BigDecimal.ZERO);
        pedido.setObservacoes(req.getObservacoes());

        pedido.getItens().clear();

        for (PedidoCompraDTO.ItemRequest itemReq : req.getItens()) {
            var produto = produtoRepository.findById(itemReq.getProdutoId())
                    .orElseThrow(() -> new NotFoundException("Produto não encontrado: " + itemReq.getProdutoId()));

            ItemPedidoCompra item = ItemPedidoCompra.builder()
                    .pedidoCompra(pedido)
                    .produto(produto)
                    .numeroItem(itemReq.getNumeroItem())
                    .quantidade(itemReq.getQuantidade())
                    .precoUnitario(itemReq.getPrecoUnitario())
                    .build();
            item.calcularTotal();
            pedido.getItens().add(item);
        }

        pedido.calcularTotal();
        return toResponse(pedidoCompraRepository.save(pedido));
    }

    // -------------------------------------------------------------------------
    // Consultas
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<PedidoCompraDTO.Resumo> listar(
            UUID fornecedorId, StatusPedidoCompra status, Boolean semNfVinculada, Pageable pageable) {

        if (Boolean.TRUE.equals(semNfVinculada)) {
            return pedidoCompraRepository.findConfirmadosSemNfVinculada(pageable).map(this::toResumo);
        }

        Page<PedidoCompra> page;
        if (fornecedorId != null && status != null) {
            page = pedidoCompraRepository.findByFornecedorIdAndStatus(fornecedorId, status, pageable);
        } else if (fornecedorId != null) {
            page = pedidoCompraRepository.findByFornecedorId(fornecedorId, pageable);
        } else if (status != null) {
            page = pedidoCompraRepository.findByStatus(status, pageable);
        } else {
            page = pedidoCompraRepository.findAll(pageable);
        }

        return page.map(this::toResumo);
    }

    @Transactional(readOnly = true)
    public PedidoCompraDTO.Response buscarPorId(UUID id) {
        return toResponse(buscarEntidade(id));
    }

    // -------------------------------------------------------------------------
    // Integração com Fiscal (chamados pelo FiscalService)
    // -------------------------------------------------------------------------

    @Transactional
    public void marcarComoRecebido(UUID pedidoCompraId) {
        PedidoCompra pedido = buscarEntidade(pedidoCompraId);

        if (pedido.getStatus() != StatusPedidoCompra.CONFIRMADO) {
            throw new BusinessException(
                    "Pedido de compra deve estar CONFIRMADO para ser marcado como recebido. Status atual: "
                    + pedido.getStatus().getDescricao());
        }

        pedido.setStatus(StatusPedidoCompra.RECEBIDO);
        pedidoCompraRepository.save(pedido);
    }

    @Transactional
    public void reverterRecebimento(UUID pedidoCompraId) {
        PedidoCompra pedido = buscarEntidade(pedidoCompraId);

        if (pedido.getStatus() != StatusPedidoCompra.RECEBIDO) {
            return;
        }

        pedido.setStatus(StatusPedidoCompra.CONFIRMADO);
        pedidoCompraRepository.save(pedido);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private PedidoCompra buscarEntidade(UUID id) {
        return pedidoCompraRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido de compra não encontrado: " + id));
    }

    private PedidoCompraDTO.Response toResponse(PedidoCompra p) {
        return PedidoCompraDTO.Response.builder()
                .id(p.getId())
                .numero(p.getNumero())
                .status(p.getStatus())
                .statusDescricao(p.getStatus().getDescricao())
                .fornecedorId(p.getFornecedor().getId())
                .fornecedorNome(p.getFornecedor().getRazaoSocial())
                .condicaoPagamento(p.getCondicaoPagamento())
                .parcelas(p.getParcelas())
                .valorFrete(p.getValorFrete())
                .valorTotal(p.getValorTotal())
                .observacoes(p.getObservacoes())
                .usuarioNome(p.getUsuario() != null ? p.getUsuario().getNome() : null)
                .itens(p.getItens().stream().map(this::toItemResponse).toList())
                .criadoEm(p.getCriadoEm())
                .atualizadoEm(p.getAtualizadoEm())
                .build();
    }

    private PedidoCompraDTO.ItemResponse toItemResponse(ItemPedidoCompra i) {
        return PedidoCompraDTO.ItemResponse.builder()
                .id(i.getId())
                .produtoId(i.getProduto().getId())
                .produtoCodigo(i.getProduto().getCodigo())
                .produtoDescricao(i.getProduto().getDescricao())
                .unidadeSimbolo(i.getProduto().getUnidadeMedida().getSimbolo())
                .numeroItem(i.getNumeroItem())
                .quantidade(i.getQuantidade())
                .precoUnitario(i.getPrecoUnitario())
                .valorTotal(i.getValorTotal())
                .build();
    }

    private PedidoCompraDTO.Resumo toResumo(PedidoCompra p) {
        return PedidoCompraDTO.Resumo.builder()
                .id(p.getId())
                .numero(p.getNumero())
                .status(p.getStatus())
                .statusDescricao(p.getStatus().getDescricao())
                .fornecedorNome(p.getFornecedor().getRazaoSocial())
                .condicaoPagamento(p.getCondicaoPagamento())
                .valorTotal(p.getValorTotal())
                .totalItens(p.getItens().size())
                .criadoEm(p.getCriadoEm())
                .build();
    }
}
