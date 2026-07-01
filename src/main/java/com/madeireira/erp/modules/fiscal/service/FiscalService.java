package com.madeireira.erp.modules.fiscal.service;

import com.madeireira.erp.modules.cadastro.repository.ClienteRepository;
import com.madeireira.erp.modules.cadastro.repository.FornecedorRepository;
import com.madeireira.erp.modules.cadastro.repository.ProdutoRepository;
import com.madeireira.erp.modules.estoque.dto.MovimentoEstoqueDTO;
import com.madeireira.erp.modules.estoque.entity.TipoMovimento;
import com.madeireira.erp.modules.estoque.service.EstoqueService;
import com.madeireira.erp.modules.financeiro.dto.ContaPagarDTO;
import com.madeireira.erp.modules.financeiro.service.FinanceiroService;
import com.madeireira.erp.modules.fiscal.dto.NotaFiscalDTO;
import com.madeireira.erp.modules.fiscal.entity.ItemNotaFiscal;
import com.madeireira.erp.modules.fiscal.entity.NotaFiscal;
import com.madeireira.erp.modules.fiscal.entity.StatusNF;
import com.madeireira.erp.modules.fiscal.entity.TipoNF;
import com.madeireira.erp.modules.fiscal.FiscalSpecification;
import com.madeireira.erp.modules.fiscal.repository.NotaFiscalRepository;
import com.madeireira.erp.modules.vendas.entity.Pedido;
import com.madeireira.erp.modules.vendas.entity.StatusPedido;
import com.madeireira.erp.modules.vendas.repository.PedidoRepository;
import com.madeireira.erp.shared.exception.BusinessException;
import com.madeireira.erp.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FiscalService {

    private final NotaFiscalRepository notaFiscalRepository;
    private final FornecedorRepository fornecedorRepository;
    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final EstoqueService estoqueService;
    private final FinanceiroService financeiroService;

    // -------------------------------------------------------------------------
    // Escrituração de Entrada
    // -------------------------------------------------------------------------

    @Transactional
    public NotaFiscalDTO.Response escriturarEntrada(NotaFiscalDTO.EntradaRequest req) {
        var fornecedor = fornecedorRepository.findById(req.getFornecedorId())
                .orElseThrow(() -> new NotFoundException("Fornecedor não encontrado: " + req.getFornecedorId()));

        if (notaFiscalRepository.existsByNumeroAndSerieAndTipoAndFornecedorId(
                req.getNumero(), req.getSerie(), TipoNF.ENTRADA, req.getFornecedorId())) {
            throw new BusinessException(String.format(
                    "NF de entrada %s série %s já escriturada para este fornecedor",
                    req.getNumero(), req.getSerie()));
        }

        NotaFiscal nota = NotaFiscal.builder()
                .tipo(TipoNF.ENTRADA)
                .status(StatusNF.ESCRITURADA_MANUAL)
                .numero(req.getNumero())
                .serie(req.getSerie() != null ? req.getSerie() : "1")
                .cfop(req.getCfop())
                .chaveAcesso(req.getChaveAcesso())
                .dataEmissao(req.getDataEmissao())
                .dataEntradaSaida(req.getDataEntradaSaida())
                .naturezaOperacao(req.getNaturezaOperacao())
                .fornecedor(fornecedor)
                .valorFrete(orZero(req.getValorFrete()))
                .valorSeguro(orZero(req.getValorSeguro()))
                .valorDesconto(orZero(req.getValorDesconto()))
                .observacoes(req.getObservacoes())
                .build();

        List<ItemNotaFiscal> itens = new ArrayList<>();
        for (NotaFiscalDTO.ItemRequest itemReq : req.getItens()) {
            itens.add(buildItem(nota, itemReq));
        }
        nota.getItens().addAll(itens);
        nota.calcularTotais();

        NotaFiscal salva = notaFiscalRepository.save(nota);

        // Movimento de estoque por item
        String obs = "NF " + salva.getNumero() + " - " + fornecedor.getRazaoSocial();
        for (ItemNotaFiscal item : salva.getItens()) {
            estoqueService.registrarMovimento(MovimentoEstoqueDTO.Request.builder()
                    .produtoId(item.getProduto().getId())
                    .tipo(TipoMovimento.ENTRADA_NF)
                    .quantidade(item.getQuantidade())
                    .custoUnitario(item.getValorUnitario())
                    .documento(salva.getNumero())
                    .observacoes(obs)
                    .build());
        }

        // Conta a pagar para o fornecedor
        financeiroService.lancarContaPagar(ContaPagarDTO.Request.builder()
                .fornecedorId(fornecedor.getId())
                .descricao("NF " + salva.getNumero())
                .valor(salva.getValorTotal())
                .dataVencimento(salva.getDataEntradaSaida().plusDays(30))
                .documento(salva.getNumero())
                .build());

        return toResponse(salva);
    }

    // -------------------------------------------------------------------------
    // Emissão de Saída
    // -------------------------------------------------------------------------

    @Transactional
    public NotaFiscalDTO.Response emitirSaida(NotaFiscalDTO.SaidaRequest req) {
        Pedido pedido = pedidoRepository.findById(req.getPedidoId())
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado: " + req.getPedidoId()));

        if (pedido.getStatus() != StatusPedido.FATURADO) {
            throw new BusinessException(
                    "Pedido deve estar FATURADO para emitir NF de saída. Status atual: "
                    + pedido.getStatus().getDescricao());
        }

        if (notaFiscalRepository.findByPedidoId(req.getPedidoId()).isPresent()) {
            throw new BusinessException(
                    "Já existe NF de saída vinculada ao pedido: " + pedido.getNumero());
        }

        NotaFiscal nota = NotaFiscal.builder()
                .tipo(TipoNF.SAIDA)
                .status(StatusNF.EMITIDA_MANUALMENTE)
                .numero(req.getNumero())
                .serie(req.getSerie() != null ? req.getSerie() : "1")
                .cfop(req.getCfop())
                .dataEmissao(req.getDataEmissao())
                .dataEntradaSaida(req.getDataEmissao()) // saída: data igual à emissão
                .naturezaOperacao(req.getNaturezaOperacao())
                .cliente(pedido.getCliente())
                .pedido(pedido)
                .observacoes(req.getObservacoes())
                .build();

        List<ItemNotaFiscal> itens = new ArrayList<>();
        for (NotaFiscalDTO.ItemRequest itemReq : req.getItens()) {
            itens.add(buildItem(nota, itemReq));
        }
        nota.getItens().addAll(itens);
        nota.calcularTotais();

        return toResponse(notaFiscalRepository.save(nota));
    }

    // -------------------------------------------------------------------------
    // Cancelamento
    // -------------------------------------------------------------------------

    @Transactional
    public NotaFiscalDTO.Response cancelarNF(UUID id) {
        NotaFiscal nota = buscarEntidade(id);

        if (!nota.getStatus().podeCancelar()) {
            throw new BusinessException(
                    "Nota fiscal não pode ser cancelada. Status atual: "
                    + nota.getStatus().getDescricao());
        }

        // Estorno de estoque NÃO é feito automaticamente:
        // cancelamento fiscal é complexo e requer ajuste manual via
        // MovimentoEstoque para garantir rastreabilidade contábil.
        nota.setStatus(StatusNF.CANCELADA);
        return toResponse(notaFiscalRepository.save(nota));
    }

    // -------------------------------------------------------------------------
    // Consultas
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<NotaFiscalDTO.Resumo> listar(
            TipoNF tipo, StatusNF status, UUID fornecedorId, UUID clienteId,
            LocalDate de, LocalDate ate, Pageable pageable) {
        return notaFiscalRepository
                .findAll(FiscalSpecification.build(tipo, status, fornecedorId, clienteId, de, ate), pageable)
                .map(this::toResumo);
    }

    @Transactional(readOnly = true)
    public NotaFiscalDTO.Response buscarPorId(UUID id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<NotaFiscalDTO.ResumoTributos> resumoTributos(LocalDate de, LocalDate ate) {
        List<NotaFiscal> notas = notaFiscalRepository.findByPeriodo(de, ate).stream()
                .filter(n -> n.getStatus() != StatusNF.CANCELADA)
                .toList();

        // Agrupa por mês — TreeMap garante ordem cronológica
        Map<YearMonth, BigDecimal[]> meses = new TreeMap<>();
        // [0]=produtos [1]=icms [2]=ipi [3]=pis [4]=cofins [5]=total
        for (NotaFiscal n : notas) {
            YearMonth mes = YearMonth.from(n.getDataEmissao());
            BigDecimal[] acc = meses.computeIfAbsent(mes,
                    k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                                         BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
            acc[0] = acc[0].add(n.getValorProdutos());
            acc[1] = acc[1].add(n.getValorIcms());
            acc[2] = acc[2].add(n.getValorIpi());
            acc[3] = acc[3].add(n.getValorPis());
            acc[4] = acc[4].add(n.getValorCofins());
            acc[5] = acc[5].add(n.getValorTotal());
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        List<NotaFiscalDTO.ResumoTributos> resultado = new ArrayList<>();

        for (Map.Entry<YearMonth, BigDecimal[]> entry : meses.entrySet()) {
            BigDecimal[] v = entry.getValue();
            resultado.add(NotaFiscalDTO.ResumoTributos.builder()
                    .periodo(entry.getKey().format(fmt))
                    .totalProdutos(v[0].setScale(2, RoundingMode.HALF_UP))
                    .totalIcms(v[1].setScale(2, RoundingMode.HALF_UP))
                    .totalIpi(v[2].setScale(2, RoundingMode.HALF_UP))
                    .totalPis(v[3].setScale(2, RoundingMode.HALF_UP))
                    .totalCofins(v[4].setScale(2, RoundingMode.HALF_UP))
                    .totalNF(v[5].setScale(2, RoundingMode.HALF_UP))
                    .build());
        }

        return resultado;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private NotaFiscal buscarEntidade(UUID id) {
        return notaFiscalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nota fiscal não encontrada: " + id));
    }

    private ItemNotaFiscal buildItem(NotaFiscal nota, NotaFiscalDTO.ItemRequest req) {
        var produto = produtoRepository.findById(req.getProdutoId())
                .orElseThrow(() -> new NotFoundException("Produto não encontrado: " + req.getProdutoId()));

        BigDecimal qtd = req.getQuantidade();
        BigDecimal unit = req.getValorUnitario();
        BigDecimal valorTotal = qtd.multiply(unit).setScale(2, RoundingMode.HALF_UP);

        BigDecimal aliqIcms = orZero(req.getAliqIcms());
        BigDecimal aliqIpi  = orZero(req.getAliqIpi());
        BigDecimal aliqPis  = orZero(req.getAliqPis());
        BigDecimal aliqCofins = orZero(req.getAliqCofins());

        ItemNotaFiscal item = ItemNotaFiscal.builder()
                .notaFiscal(nota)
                .produto(produto)
                .numeroItem(req.getNumeroItem())
                .quantidade(qtd)
                .valorUnitario(unit)
                .valorTotal(valorTotal)
                // ICMS
                .cstIcms(req.getCstIcms())
                .aliqIcms(aliqIcms)
                .valorIcms(calcTributo(valorTotal, aliqIcms))
                // IPI
                .cstIpi(req.getCstIpi())
                .aliqIpi(aliqIpi)
                .valorIpi(calcTributo(valorTotal, aliqIpi))
                // PIS
                .cstPis(req.getCstPis())
                .aliqPis(aliqPis)
                .valorPis(calcTributo(valorTotal, aliqPis))
                // COFINS
                .cstCofins(req.getCstCofins())
                .aliqCofins(aliqCofins)
                .valorCofins(calcTributo(valorTotal, aliqCofins))
                .build();

        return item;
    }

    /** tributo = base * aliq / 100, arredondado para 2 casas */
    private BigDecimal calcTributo(BigDecimal base, BigDecimal aliq) {
        if (aliq.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return base.multiply(aliq)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal orZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private NotaFiscalDTO.Response toResponse(NotaFiscal n) {
        return NotaFiscalDTO.Response.builder()
                .id(n.getId())
                .tipo(n.getTipo())
                .status(n.getStatus())
                .numero(n.getNumero())
                .serie(n.getSerie())
                .cfop(n.getCfop())
                .chaveAcesso(n.getChaveAcesso())
                .dataEmissao(n.getDataEmissao())
                .dataEntradaSaida(n.getDataEntradaSaida())
                .naturezaOperacao(n.getNaturezaOperacao())
                .fornecedorId(n.getFornecedor() != null ? n.getFornecedor().getId() : null)
                .fornecedorNome(n.getFornecedor() != null ? n.getFornecedor().getRazaoSocial() : null)
                .clienteId(n.getCliente() != null ? n.getCliente().getId() : null)
                .clienteNome(n.getCliente() != null ? n.getCliente().getRazaoSocial() : null)
                .pedidoId(n.getPedido() != null ? n.getPedido().getId() : null)
                .pedidoNumero(n.getPedido() != null ? n.getPedido().getNumero() : null)
                .valorProdutos(n.getValorProdutos())
                .valorFrete(n.getValorFrete())
                .valorSeguro(n.getValorSeguro())
                .valorDesconto(n.getValorDesconto())
                .valorIcms(n.getValorIcms())
                .valorIpi(n.getValorIpi())
                .valorPis(n.getValorPis())
                .valorCofins(n.getValorCofins())
                .valorTotal(n.getValorTotal())
                .observacoes(n.getObservacoes())
                .itens(n.getItens().stream().map(this::toItemResponse).toList())
                .criadoEm(n.getCriadoEm())
                .atualizadoEm(n.getAtualizadoEm())
                .build();
    }

    private NotaFiscalDTO.ItemResponse toItemResponse(ItemNotaFiscal i) {
        return NotaFiscalDTO.ItemResponse.builder()
                .id(i.getId())
                .produtoId(i.getProduto().getId())
                .produtoCodigo(i.getProduto().getCodigo())
                .produtoDescricao(i.getProduto().getDescricao())
                .unidadeMedida(i.getProduto().getUnidadeMedida())
                .numeroItem(i.getNumeroItem())
                .quantidade(i.getQuantidade())
                .valorUnitario(i.getValorUnitario())
                .valorTotal(i.getValorTotal())
                .cstIcms(i.getCstIcms()).aliqIcms(i.getAliqIcms()).valorIcms(i.getValorIcms())
                .cstIpi(i.getCstIpi()).aliqIpi(i.getAliqIpi()).valorIpi(i.getValorIpi())
                .cstPis(i.getCstPis()).aliqPis(i.getAliqPis()).valorPis(i.getValorPis())
                .cstCofins(i.getCstCofins()).aliqCofins(i.getAliqCofins()).valorCofins(i.getValorCofins())
                .build();
    }

    private NotaFiscalDTO.Resumo toResumo(NotaFiscal n) {
        return NotaFiscalDTO.Resumo.builder()
                .id(n.getId())
                .tipo(n.getTipo())
                .status(n.getStatus())
                .numero(n.getNumero())
                .serie(n.getSerie())
                .cfop(n.getCfop())
                .dataEmissao(n.getDataEmissao())
                .fornecedorNome(n.getFornecedor() != null ? n.getFornecedor().getRazaoSocial() : null)
                .clienteNome(n.getCliente() != null ? n.getCliente().getRazaoSocial() : null)
                .valorTotal(n.getValorTotal())
                .totalItens(n.getItens().size())
                .build();
    }
}
