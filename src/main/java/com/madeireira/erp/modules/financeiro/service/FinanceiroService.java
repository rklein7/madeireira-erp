package com.madeireira.erp.modules.financeiro.service;

import com.madeireira.erp.modules.cadastro.repository.ClienteRepository;
import com.madeireira.erp.modules.cadastro.repository.FornecedorRepository;
import com.madeireira.erp.modules.financeiro.dto.ContaPagarDTO;
import com.madeireira.erp.modules.financeiro.dto.ContaReceberDTO;
import com.madeireira.erp.modules.financeiro.dto.FinanceiroDTO;
import com.madeireira.erp.modules.financeiro.dto.FluxoCaixaDTO;
import com.madeireira.erp.modules.financeiro.entity.ContaPagar;
import com.madeireira.erp.modules.financeiro.entity.ContaReceber;
import com.madeireira.erp.modules.financeiro.entity.StatusConta;
import com.madeireira.erp.modules.financeiro.repository.ContaPagarRepository;
import com.madeireira.erp.modules.financeiro.repository.ContaReceberRepository;
import com.madeireira.erp.modules.vendas.entity.Pedido;
import com.madeireira.erp.modules.vendas.repository.PedidoRepository;
import com.madeireira.erp.shared.exception.BusinessException;
import com.madeireira.erp.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinanceiroService {

    private final ContaReceberRepository contaReceberRepository;
    private final ContaPagarRepository contaPagarRepository;
    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final FornecedorRepository fornecedorRepository;

    // -------------------------------------------------------------------------
    // Contas a Receber
    // -------------------------------------------------------------------------

    @Transactional
    public List<ContaReceber> gerarContasReceberDoPedido(UUID pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado: " + pedidoId));

        if (!contaReceberRepository.findByPedidoId(pedidoId).isEmpty()) {
            throw new BusinessException(
                    "Contas a receber já geradas para o pedido: " + pedido.getNumero());
        }

        int totalParcelas = pedido.getParcelas() != null && pedido.getParcelas() > 0
                ? pedido.getParcelas() : 1;

        BigDecimal total = pedido.getValorTotal();
        BigDecimal valorParcela = total.divide(BigDecimal.valueOf(totalParcelas), 2, RoundingMode.DOWN);
        BigDecimal totalDemais = valorParcela.multiply(BigDecimal.valueOf(totalParcelas - 1));
        BigDecimal ultimaParcela = total.subtract(totalDemais);

        LocalDate base = LocalDate.now();
        List<ContaReceber> contas = new ArrayList<>();

        for (int i = 1; i <= totalParcelas; i++) {
            BigDecimal valor = (i == totalParcelas) ? ultimaParcela : valorParcela;

            ContaReceber conta = ContaReceber.builder()
                    .pedido(pedido)
                    .cliente(pedido.getCliente())
                    .descricao(String.format("Pedido %s - Parcela %d/%d", pedido.getNumero(), i, totalParcelas))
                    .valor(valor)
                    .dataVencimento(base.plusDays(30L * i))
                    .status(StatusConta.ABERTO)
                    .parcela(i)
                    .totalParcelas(totalParcelas)
                    .build();

            contas.add(conta);
        }

        return contaReceberRepository.saveAll(contas);
    }

    @Transactional(readOnly = true)
    public ContaReceberDTO.Response buscarContaReceberPorId(UUID id) {
        return toResponseCR(contaReceberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Conta a receber não encontrada: " + id)));
    }

    @Transactional
    public ContaReceberDTO.Response registrarPagamentoReceber(UUID id, ContaReceberDTO.PagarRequest req) {
        ContaReceber conta = contaReceberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Conta a receber não encontrada: " + id));

        if (conta.getStatus() != StatusConta.ABERTO) {
            throw new BusinessException(
                    "Conta não pode ser quitada. Status atual: " + conta.getStatus().getDescricao());
        }

        conta.setDataPagamento(req.getDataPagamento());
        conta.setValorPago(req.getValorPago());
        conta.setFormaPagamento(req.getFormaPagamento());
        conta.setCodigoBanco(req.getCodigoBanco());
        conta.setNomeBanco(req.getNomeBanco());
        conta.setStatus(StatusConta.PAGO);
        if (req.getObservacoes() != null) {
            conta.setObservacoes(req.getObservacoes());
        }

        return toResponseCR(contaReceberRepository.save(conta));
    }

    @Transactional(readOnly = true)
    public Page<ContaReceberDTO.Resumo> listarContasReceber(
            UUID clienteId, StatusConta status, Pageable pageable) {

        Page<ContaReceber> page;
        if (clienteId != null && status != null) {
            page = contaReceberRepository.findByClienteIdAndStatus(clienteId, status, pageable);
        } else if (clienteId != null) {
            page = contaReceberRepository.findByClienteId(clienteId, pageable);
        } else if (status != null) {
            page = contaReceberRepository.findByStatus(status, pageable);
        } else {
            page = contaReceberRepository.findAll(pageable);
        }

        return page.map(this::toResumoCR);
    }

    // -------------------------------------------------------------------------
    // Contas a Pagar
    // -------------------------------------------------------------------------

    @Transactional
    public ContaPagarDTO.Response lancarContaPagar(ContaPagarDTO.Request req) {
        ContaPagar.ContaPagarBuilder builder = ContaPagar.builder()
                .descricao(req.getDescricao())
                .valor(req.getValor())
                .dataVencimento(req.getDataVencimento())
                .documento(req.getDocumento())
                .observacoes(req.getObservacoes())
                .status(StatusConta.ABERTO);

        if (req.getFornecedorId() != null) {
            var fornecedor = fornecedorRepository.findById(req.getFornecedorId())
                    .orElseThrow(() -> new NotFoundException(
                            "Fornecedor não encontrado: " + req.getFornecedorId()));
            builder.fornecedor(fornecedor);
        }

        return toResponseCP(contaPagarRepository.save(builder.build()));
    }

    @Transactional
    public ContaPagarDTO.Response registrarPagamentoPagar(UUID id, ContaPagarDTO.PagarRequest req) {
        ContaPagar conta = contaPagarRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Conta a pagar não encontrada: " + id));

        if (conta.getStatus() != StatusConta.ABERTO) {
            throw new BusinessException(
                    "Conta não pode ser quitada. Status atual: " + conta.getStatus().getDescricao());
        }

        conta.setDataPagamento(req.getDataPagamento());
        conta.setValorPago(req.getValorPago());
        conta.setFormaPagamento(req.getFormaPagamento());
        conta.setCodigoBanco(req.getCodigoBanco());
        conta.setNomeBanco(req.getNomeBanco());
        conta.setStatus(StatusConta.PAGO);
        if (req.getObservacoes() != null) {
            conta.setObservacoes(req.getObservacoes());
        }

        return toResponseCP(contaPagarRepository.save(conta));
    }

    @Transactional(readOnly = true)
    public Page<ContaPagarDTO.Response> listarContasPagar(StatusConta status, Pageable pageable) {
        Page<ContaPagar> page = (status != null)
                ? contaPagarRepository.findByStatus(status, pageable)
                : contaPagarRepository.findAll(pageable);

        return page.map(this::toResponseCP);
    }

    // -------------------------------------------------------------------------
    // Lançamentos consolidados (recebimentos + pagamentos efetivados)
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<FinanceiroDTO.LancamentoResponse> listarLancamentos(
            String tipo, String codigoBanco, LocalDate de, LocalDate ate, Pageable pageable) {

        boolean incluirRecebimentos = tipo == null || "RECEBIMENTO".equalsIgnoreCase(tipo);
        boolean incluirPagamentos   = tipo == null || "PAGAMENTO".equalsIgnoreCase(tipo);

        List<FinanceiroDTO.LancamentoResponse> lancamentos = new ArrayList<>();

        if (incluirRecebimentos) {
            contaReceberRepository.findByStatus(StatusConta.PAGO).stream()
                    .filter(c -> codigoBanco == null || codigoBanco.equals(c.getCodigoBanco()))
                    .filter(c -> de  == null || !c.getDataPagamento().isBefore(de))
                    .filter(c -> ate == null || !c.getDataPagamento().isAfter(ate))
                    .map(this::toLancamentoRecebimento)
                    .forEach(lancamentos::add);
        }

        if (incluirPagamentos) {
            contaPagarRepository.findByStatus(StatusConta.PAGO).stream()
                    .filter(c -> codigoBanco == null || codigoBanco.equals(c.getCodigoBanco()))
                    .filter(c -> de  == null || !c.getDataPagamento().isBefore(de))
                    .filter(c -> ate == null || !c.getDataPagamento().isAfter(ate))
                    .map(this::toLancamentoPagamento)
                    .forEach(lancamentos::add);
        }

        lancamentos.sort(Comparator.comparing(FinanceiroDTO.LancamentoResponse::getData).reversed());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), lancamentos.size());
        List<FinanceiroDTO.LancamentoResponse> pageContent =
                start >= lancamentos.size() ? List.of() : lancamentos.subList(start, end);

        return new PageImpl<>(pageContent, pageable, lancamentos.size());
    }

    // -------------------------------------------------------------------------
    // Fluxo de Caixa
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public FluxoCaixaDTO.Response fluxoCaixa(LocalDate de, LocalDate ate) {
        List<ContaReceber> entradas = contaReceberRepository.findByDataVencimentoBetween(de, ate);
        List<ContaPagar> saidas = contaPagarRepository.findByDataVencimentoBetween(de, ate);

        Map<YearMonth, BigDecimal[]> meses = new TreeMap<>();

        for (ContaReceber cr : entradas) {
            YearMonth mes = YearMonth.from(cr.getDataVencimento());
            meses.computeIfAbsent(mes, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            meses.get(mes)[0] = meses.get(mes)[0].add(cr.getValor());
        }

        for (ContaPagar cp : saidas) {
            YearMonth mes = YearMonth.from(cp.getDataVencimento());
            meses.computeIfAbsent(mes, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            meses.get(mes)[1] = meses.get(mes)[1].add(cp.getValor());
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        BigDecimal saldoAcumulado = BigDecimal.ZERO;
        BigDecimal totalEntradas = BigDecimal.ZERO;
        BigDecimal totalSaidas = BigDecimal.ZERO;
        List<FluxoCaixaDTO.ItemFluxo> itens = new ArrayList<>();

        for (Map.Entry<YearMonth, BigDecimal[]> entry : meses.entrySet()) {
            BigDecimal entrada = entry.getValue()[0].setScale(2, RoundingMode.HALF_UP);
            BigDecimal saida = entry.getValue()[1].setScale(2, RoundingMode.HALF_UP);
            BigDecimal saldo = entrada.subtract(saida);
            saldoAcumulado = saldoAcumulado.add(saldo);

            totalEntradas = totalEntradas.add(entrada);
            totalSaidas = totalSaidas.add(saida);

            itens.add(FluxoCaixaDTO.ItemFluxo.builder()
                    .periodo(entry.getKey().format(fmt))
                    .entradas(entrada)
                    .saidas(saida)
                    .saldo(saldo)
                    .saldoAcumulado(saldoAcumulado)
                    .build());
        }

        return FluxoCaixaDTO.Response.builder()
                .de(de)
                .ate(ate)
                .agrupamento("MENSAL")
                .totalEntradas(totalEntradas)
                .totalSaidas(totalSaidas)
                .saldoPeriodo(totalEntradas.subtract(totalSaidas))
                .itens(itens)
                .build();
    }

    // -------------------------------------------------------------------------
    // Mapeadores
    // -------------------------------------------------------------------------

    private ContaReceberDTO.Response toResponseCR(ContaReceber c) {
        return ContaReceberDTO.Response.builder()
                .id(c.getId())
                .pedidoId(c.getPedido() != null ? c.getPedido().getId() : null)
                .pedidoNumero(c.getPedido() != null ? c.getPedido().getNumero() : null)
                .clienteId(c.getCliente().getId())
                .clienteNome(c.getCliente().getRazaoSocial())
                .descricao(c.getDescricao())
                .valor(c.getValor())
                .dataVencimento(c.getDataVencimento())
                .dataPagamento(c.getDataPagamento())
                .valorPago(c.getValorPago())
                .status(c.getStatus())
                .formaPagamento(c.getFormaPagamento())
                .codigoBanco(c.getCodigoBanco())
                .nomeBanco(c.getNomeBanco())
                .parcela(c.getParcela())
                .totalParcelas(c.getTotalParcelas())
                .observacoes(c.getObservacoes())
                .vencida(c.isVencida())
                .criadoEm(c.getCriadoEm())
                .build();
    }

    private ContaReceberDTO.Resumo toResumoCR(ContaReceber c) {
        return ContaReceberDTO.Resumo.builder()
                .id(c.getId())
                .clienteNome(c.getCliente().getRazaoSocial())
                .descricao(c.getDescricao())
                .valor(c.getValor())
                .dataVencimento(c.getDataVencimento())
                .status(c.getStatus())
                .parcela(c.getParcela())
                .totalParcelas(c.getTotalParcelas())
                .vencida(c.isVencida())
                .build();
    }

    private ContaPagarDTO.Response toResponseCP(ContaPagar c) {
        return ContaPagarDTO.Response.builder()
                .id(c.getId())
                .fornecedorId(c.getFornecedor() != null ? c.getFornecedor().getId() : null)
                .fornecedorNome(c.getFornecedor() != null ? c.getFornecedor().getRazaoSocial() : null)
                .descricao(c.getDescricao())
                .valor(c.getValor())
                .dataVencimento(c.getDataVencimento())
                .dataPagamento(c.getDataPagamento())
                .valorPago(c.getValorPago())
                .status(c.getStatus())
                .formaPagamento(c.getFormaPagamento())
                .codigoBanco(c.getCodigoBanco())
                .nomeBanco(c.getNomeBanco())
                .documento(c.getDocumento())
                .observacoes(c.getObservacoes())
                .vencida(c.isVencida())
                .criadoEm(c.getCriadoEm())
                .build();
    }

    private FinanceiroDTO.LancamentoResponse toLancamentoRecebimento(ContaReceber c) {
        return FinanceiroDTO.LancamentoResponse.builder()
                .id(c.getId())
                .tipo("RECEBIMENTO")
                .data(c.getDataPagamento())
                .descricao(c.getDescricao())
                .valor(c.getValor())
                .valorPago(c.getValorPago())
                .codigoBanco(c.getCodigoBanco())
                .nomeBanco(c.getNomeBanco())
                .formaPagamento(c.getFormaPagamento())
                .clienteNome(c.getCliente() != null ? c.getCliente().getRazaoSocial() : null)
                .origem("CONTA_RECEBER")
                .build();
    }

    private FinanceiroDTO.LancamentoResponse toLancamentoPagamento(ContaPagar c) {
        return FinanceiroDTO.LancamentoResponse.builder()
                .id(c.getId())
                .tipo("PAGAMENTO")
                .data(c.getDataPagamento())
                .descricao(c.getDescricao())
                .valor(c.getValor())
                .valorPago(c.getValorPago())
                .codigoBanco(c.getCodigoBanco())
                .nomeBanco(c.getNomeBanco())
                .formaPagamento(c.getFormaPagamento())
                .fornecedorNome(c.getFornecedor() != null ? c.getFornecedor().getRazaoSocial() : null)
                .origem("CONTA_PAGAR")
                .build();
    }
}
