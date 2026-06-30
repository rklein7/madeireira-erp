package com.madeireira.erp.modules.cadastro.service;

import com.madeireira.erp.modules.cadastro.dto.ClienteDTO;
import com.madeireira.erp.modules.cadastro.entity.Cliente;
import com.madeireira.erp.modules.cadastro.entity.TabelaPreco;
import com.madeireira.erp.modules.cadastro.repository.ClienteRepository;
import com.madeireira.erp.modules.cadastro.repository.TabelaPrecoRepository;
import com.madeireira.erp.shared.exception.BusinessException;
import com.madeireira.erp.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final TabelaPrecoRepository tabelaPrecoRepository;

    @Transactional(readOnly = true)
    public Page<ClienteDTO.Resumo> listar(String busca, Pageable pageable) {
        Page<Cliente> page = (busca != null && !busca.isBlank())
                ? clienteRepository.buscar(busca, pageable)
                : clienteRepository.findByAtivoTrue(pageable);
        return page.map(this::toResumo);
    }

    @Transactional(readOnly = true)
    public ClienteDTO.Response buscarPorId(UUID id) {
        return toResponse(findById(id));
    }

    @Transactional
    public ClienteDTO.Response criar(ClienteDTO.Request request) {
        if (clienteRepository.existsByCpfCnpj(request.getCpfCnpj())) {
            throw new BusinessException("Já existe um cliente com CPF/CNPJ: " + request.getCpfCnpj());
        }
        Cliente cliente = fromRequest(request, new Cliente());
        return toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteDTO.Response atualizar(UUID id, ClienteDTO.Request request) {
        Cliente cliente = findById(id);
        if (!cliente.getCpfCnpj().equals(request.getCpfCnpj()) &&
            clienteRepository.existsByCpfCnpj(request.getCpfCnpj())) {
            throw new BusinessException("Já existe um cliente com CPF/CNPJ: " + request.getCpfCnpj());
        }
        fromRequest(request, cliente);
        return toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public void inativar(UUID id) {
        Cliente cliente = findById(id);
        cliente.setAtivo(false);
        clienteRepository.save(cliente);
    }

    private Cliente findById(UUID id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado: " + id));
    }

    private Cliente fromRequest(ClienteDTO.Request req, Cliente cliente) {
        cliente.setTipoPessoa(req.getTipoPessoa());
        cliente.setRazaoSocial(req.getRazaoSocial());
        cliente.setNomeFantasia(req.getNomeFantasia());
        cliente.setCpfCnpj(req.getCpfCnpj());
        cliente.setIe(req.getIe());
        cliente.setIm(req.getIm());
        cliente.setEmail(req.getEmail());
        cliente.setTelefone(req.getTelefone());
        cliente.setCelular(req.getCelular());
        cliente.setCep(req.getCep());
        cliente.setLogradouro(req.getLogradouro());
        cliente.setNumero(req.getNumero());
        cliente.setComplemento(req.getComplemento());
        cliente.setBairro(req.getBairro());
        cliente.setCidade(req.getCidade());
        cliente.setUf(req.getUf());
        cliente.setLimiteCredito(req.getLimiteCredito() != null ? req.getLimiteCredito() : BigDecimal.ZERO);
        cliente.setDiasPrazo(req.getDiasPrazo() != null ? req.getDiasPrazo() : 30);
        cliente.setObservacoes(req.getObservacoes());
        if (req.getTabelaPrecoId() != null) {
            TabelaPreco tabela = tabelaPrecoRepository.findById(req.getTabelaPrecoId())
                    .orElseThrow(() -> new NotFoundException("Tabela de preço não encontrada"));
            cliente.setTabelaPreco(tabela);
        }
        return cliente;
    }

    private ClienteDTO.Response toResponse(Cliente c) {
        return ClienteDTO.Response.builder()
                .id(c.getId())
                .tipoPessoa(c.getTipoPessoa())
                .razaoSocial(c.getRazaoSocial())
                .nomeFantasia(c.getNomeFantasia())
                .cpfCnpj(c.getCpfCnpj())
                .ie(c.getIe())
                .im(c.getIm())
                .email(c.getEmail())
                .telefone(c.getTelefone())
                .celular(c.getCelular())
                .cep(c.getCep())
                .logradouro(c.getLogradouro())
                .numero(c.getNumero())
                .complemento(c.getComplemento())
                .bairro(c.getBairro())
                .cidade(c.getCidade())
                .uf(c.getUf())
                .limiteCredito(c.getLimiteCredito())
                .diasPrazo(c.getDiasPrazo())
                .tabelaPrecoId(c.getTabelaPreco() != null ? c.getTabelaPreco().getId() : null)
                .tabelaPrecoNome(c.getTabelaPreco() != null ? c.getTabelaPreco().getNome() : null)
                .ativo(c.getAtivo())
                .observacoes(c.getObservacoes())
                .criadoEm(c.getCriadoEm())
                .atualizadoEm(c.getAtualizadoEm())
                .build();
    }

    private ClienteDTO.Resumo toResumo(Cliente c) {
        return ClienteDTO.Resumo.builder()
                .id(c.getId())
                .razaoSocial(c.getRazaoSocial())
                .nomeFantasia(c.getNomeFantasia())
                .cpfCnpj(c.getCpfCnpj())
                .telefone(c.getTelefone())
                .cidade(c.getCidade())
                .uf(c.getUf())
                .ativo(c.getAtivo())
                .build();
    }
}
