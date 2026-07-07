package com.madeireira.erp.modules.vendas.service;

import com.madeireira.erp.modules.auth.repository.UsuarioRepository;
import com.madeireira.erp.modules.cadastro.entity.Produto;
import com.madeireira.erp.modules.cadastro.repository.ClienteRepository;
import com.madeireira.erp.modules.cadastro.repository.ProdutoRepository;
import com.madeireira.erp.modules.estoque.dto.MovimentoEstoqueDTO;
import com.madeireira.erp.modules.estoque.entity.TipoMovimento;
import com.madeireira.erp.modules.estoque.service.EstoqueService;
import com.madeireira.erp.modules.financeiro.service.FinanceiroService;
import com.madeireira.erp.modules.vendas.dto.PedidoDTO;
import com.madeireira.erp.modules.vendas.entity.ItemPedido;
import com.madeireira.erp.modules.vendas.entity.Pedido;
import com.madeireira.erp.modules.vendas.entity.StatusPedido;
import com.madeireira.erp.modules.vendas.repository.ItemPedidoRepository;
import com.madeireira.erp.modules.vendas.repository.PedidoRepository;
import com.madeireira.erp.shared.exception.BusinessException;
import com.madeireira.erp.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VendasService {

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstoqueService estoqueService;
    private final FinanceiroService financeiroService;

    @Transactional
    public PedidoDTO.Response criar(PedidoDTO.Request request) {
        var cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado: " + request.getClienteId()));

        String numero = String.format("PED-%d-%05d",
                LocalDate.now().getYear(), pedidoRepository.nextNumeroPedido());

        Pedido pedido = Pedido.builder()
                .numero(numero)
                .cliente(cliente)
                .condicaoPagamento(request.getCondicaoPagamento())
                .parcelas(request.getParcelas() != null ? request.getParcelas() : 1)
                .valorFrete(request.getValorFrete() != null ? request.getValorFrete() : java.math.BigDecimal.ZERO)
                .observacoes(request.getObservacoes())
                .status(StatusPedido.RASCUNHO)
                .build();

        if (request.getVendedorId() != null) {
            var vendedor = usuarioRepository.findById(request.getVendedorId())
                    .orElseThrow(() -> new NotFoundException(
                            "Vendedor não encontrado: " + request.getVendedorId()));
            pedido.setVendedor(vendedor);
        }

        List<ItemPedido> itens = request.getItens().stream().map(itemReq -> {
            Produto produto = produtoRepository.findById(itemReq.getProdutoId())
                    .orElseThrow(() -> new NotFoundException("Produto não encontrado: " + itemReq.getProdutoId()));

            ItemPedido item = ItemPedido.builder()
                    .pedido(pedido)
                    .produto(produto)
                    .quantidade(itemReq.getQuantidade())
                    .precoUnitario(produto.getPrecoVenda())
                    .descontoPerc(itemReq.getDescontoPerc() != null
                            ? itemReq.getDescontoPerc()
                            : java.math.BigDecimal.ZERO)
                    .build();
            item.calcularTotal();
            return item;
        }).toList();

        pedido.getItens().addAll(itens);
        pedido.calcularTotais();

        return toResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoDTO.Response confirmarPedido(UUID id) {
        Pedido pedido = buscarEntidade(id);

        if (pedido.getStatus() != StatusPedido.RASCUNHO) {
            throw new BusinessException(
                    "Pedido não pode ser confirmado. Status atual: " + pedido.getStatus().getDescricao());
        }

        // Valida estoque de TODOS os itens antes de processar qualquer movimento
        for (ItemPedido item : pedido.getItens()) {
            Produto produto = item.getProduto();
            if (item.getQuantidade().compareTo(produto.getEstoqueAtual()) > 0) {
                throw new BusinessException(String.format(
                        "Estoque insuficiente para o produto '%s'. Saldo atual: %s, solicitado: %s",
                        produto.getDescricao(),
                        produto.getEstoqueAtual().toPlainString(),
                        item.getQuantidade().toPlainString()));
            }
        }

        // Após validar todos, registra as saídas
        for (ItemPedido item : pedido.getItens()) {
            estoqueService.registrarMovimento(MovimentoEstoqueDTO.Request.builder()
                    .produtoId(item.getProduto().getId())
                    .tipo(TipoMovimento.SAIDA_PEDIDO)
                    .quantidade(item.getQuantidade())
                    .documento(pedido.getNumero())
                    .build());
        }

        pedido.setStatus(StatusPedido.CONFIRMADO);
        return toResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoDTO.Response faturarPedido(UUID id) {
        Pedido pedido = buscarEntidade(id);

        if (pedido.getStatus() != StatusPedido.CONFIRMADO) {
            throw new BusinessException(
                    "Pedido não pode ser faturado. Status atual: " + pedido.getStatus().getDescricao());
        }

        pedido.setStatus(StatusPedido.FATURADO);
        Pedido salvo = pedidoRepository.save(pedido);
        financeiroService.gerarContasReceberDoPedido(salvo.getId());
        return toResponse(salvo);
    }

    @Transactional
    public PedidoDTO.Response entregarPedido(UUID id) {
        Pedido pedido = buscarEntidade(id);

        if (pedido.getStatus() != StatusPedido.FATURADO) {
            throw new BusinessException(
                    "Pedido não pode ser marcado como entregue. Status atual: " + pedido.getStatus().getDescricao());
        }

        pedido.setStatus(StatusPedido.ENTREGUE);
        return toResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoDTO.Response cancelarPedido(UUID id) {
        Pedido pedido = buscarEntidade(id);

        if (!pedido.getStatus().podeCancelar()) {
            throw new BusinessException(
                    "Pedido não pode ser cancelado. Status atual: " + pedido.getStatus().getDescricao());
        }

        // Estorna estoque apenas se já tinha baixado (status CONFIRMADO)
        if (pedido.getStatus() == StatusPedido.CONFIRMADO) {
            for (ItemPedido item : pedido.getItens()) {
                estoqueService.registrarMovimento(MovimentoEstoqueDTO.Request.builder()
                        .produtoId(item.getProduto().getId())
                        .tipo(TipoMovimento.ENTRADA_MANUAL)
                        .quantidade(item.getQuantidade())
                        .documento("ESTORNO-" + pedido.getNumero())
                        .observacoes("Estorno por cancelamento")
                        .build());
            }
        }

        pedido.setStatus(StatusPedido.CANCELADO);
        return toResponse(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public Page<PedidoDTO.Resumo> listar(
            UUID clienteId, StatusPedido status, Boolean semNfVinculada, Pageable pageable) {

        if (Boolean.TRUE.equals(semNfVinculada)) {
            return pedidoRepository.findFaturadosSemNfVinculada(pageable).map(this::toResumo);
        }

        Page<Pedido> page;
        if (clienteId != null && status != null) {
            page = pedidoRepository.findByClienteIdAndStatus(clienteId, status, pageable);
        } else if (clienteId != null) {
            page = pedidoRepository.findByClienteId(clienteId, pageable);
        } else if (status != null) {
            page = pedidoRepository.findByStatus(status, pageable);
        } else {
            page = pedidoRepository.findAll(pageable);
        }

        return page.map(this::toResumo);
    }

    @Transactional(readOnly = true)
    public PedidoDTO.Response buscarPorId(UUID id) {
        return toResponse(buscarEntidade(id));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Pedido buscarEntidade(UUID id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado: " + id));
    }

    private PedidoDTO.Response toResponse(Pedido p) {
        return PedidoDTO.Response.builder()
                .id(p.getId())
                .numero(p.getNumero())
                .status(p.getStatus())
                .condicaoPagamento(p.getCondicaoPagamento())
                .parcelas(p.getParcelas())
                .clienteId(p.getCliente().getId())
                .clienteNome(p.getCliente().getRazaoSocial())
                .clienteCpfCnpj(p.getCliente().getCpfCnpj())
                .clienteEndereco(p.getCliente().getLogradouro() != null
                        ? p.getCliente().getLogradouro() + ", " + p.getCliente().getNumero()
                        : null)
                .clienteBairro(p.getCliente().getBairro())
                .clienteCidade(p.getCliente().getCidade())
                .clienteUf(p.getCliente().getUf())
                .clienteCep(p.getCliente().getCep())
                .clienteTelefone(p.getCliente().getTelefone() != null
                        ? p.getCliente().getTelefone()
                        : p.getCliente().getCelular())
                .clienteEmail(p.getCliente().getEmail())
                .clienteIe(p.getCliente().getIe())
                .vendedorId(p.getVendedor() != null ? p.getVendedor().getId() : null)
                .vendedorNome(p.getVendedor() != null ? p.getVendedor().getNome() : null)
                .valorSubtotal(p.getValorSubtotal())
                .valorDesconto(p.getValorDesconto())
                .valorFrete(p.getValorFrete())
                .valorTotal(p.getValorTotal())
                .observacoes(p.getObservacoes())
                .usuarioNome(p.getUsuario() != null ? p.getUsuario().getNome() : null)
                .itens(p.getItens().stream().map(this::toItemResponse).toList())
                .criadoEm(p.getCriadoEm())
                .atualizadoEm(p.getAtualizadoEm())
                .build();
    }

    private PedidoDTO.ItemResponse toItemResponse(ItemPedido item) {
        return PedidoDTO.ItemResponse.builder()
                .id(item.getId())
                .produtoId(item.getProduto().getId())
                .produtoCodigo(item.getProduto().getCodigo())
                .produtoDescricao(item.getProduto().getDescricao())
                .unidadeMedida(item.getProduto().getUnidadeMedida())
                .unidadeSimbolo(item.getProduto().getUnidadeMedida().getSimbolo())
                .quantidade(item.getQuantidade())
                .precoUnitario(item.getPrecoUnitario())
                .descontoPerc(item.getDescontoPerc())
                .valorTotal(item.getValorTotal())
                .build();
    }

    private PedidoDTO.Resumo toResumo(Pedido p) {
        return PedidoDTO.Resumo.builder()
                .id(p.getId())
                .numero(p.getNumero())
                .status(p.getStatus())
                .clienteNome(p.getCliente().getRazaoSocial())
                .vendedorNome(p.getVendedor() != null ? p.getVendedor().getNome() : null)
                .condicaoPagamento(p.getCondicaoPagamento())
                .valorTotal(p.getValorTotal())
                .totalItens(p.getItens().size())
                .criadoEm(p.getCriadoEm())
                .build();
    }
}
