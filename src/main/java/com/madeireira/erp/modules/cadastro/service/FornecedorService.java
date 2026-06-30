package com.madeireira.erp.modules.cadastro.service;

import com.madeireira.erp.modules.cadastro.dto.FornecedorDTO;
import com.madeireira.erp.modules.cadastro.entity.Fornecedor;
import com.madeireira.erp.modules.cadastro.repository.FornecedorRepository;
import com.madeireira.erp.shared.exception.BusinessException;
import com.madeireira.erp.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;

    @Transactional(readOnly = true)
    public Page<FornecedorDTO.Resumo> listar(String busca, Pageable pageable) {
        Page<Fornecedor> page = (busca != null && !busca.isBlank())
                ? fornecedorRepository.buscar(busca, pageable)
                : fornecedorRepository.findByAtivoTrue(pageable);
        return page.map(this::toResumo);
    }

    @Transactional(readOnly = true)
    public FornecedorDTO.Response buscarPorId(UUID id) {
        return toResponse(findById(id));
    }

    @Transactional
    public FornecedorDTO.Response criar(FornecedorDTO.Request request) {
        if (fornecedorRepository.existsByCpfCnpj(request.getCpfCnpj())) {
            throw new BusinessException("Já existe um fornecedor com CPF/CNPJ: " + request.getCpfCnpj());
        }
        Fornecedor fornecedor = fromRequest(request, new Fornecedor());
        return toResponse(fornecedorRepository.save(fornecedor));
    }

    @Transactional
    public FornecedorDTO.Response atualizar(UUID id, FornecedorDTO.Request request) {
        Fornecedor fornecedor = findById(id);
        if (!fornecedor.getCpfCnpj().equals(request.getCpfCnpj()) &&
            fornecedorRepository.existsByCpfCnpj(request.getCpfCnpj())) {
            throw new BusinessException("Já existe um fornecedor com CPF/CNPJ: " + request.getCpfCnpj());
        }
        fromRequest(request, fornecedor);
        return toResponse(fornecedorRepository.save(fornecedor));
    }

    @Transactional
    public void inativar(UUID id) {
        Fornecedor fornecedor = findById(id);
        fornecedor.setAtivo(false);
        fornecedorRepository.save(fornecedor);
    }

    private Fornecedor findById(UUID id) {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fornecedor não encontrado: " + id));
    }

    private Fornecedor fromRequest(FornecedorDTO.Request req, Fornecedor fornecedor) {
        fornecedor.setTipoPessoa(req.getTipoPessoa());
        fornecedor.setRazaoSocial(req.getRazaoSocial());
        fornecedor.setNomeFantasia(req.getNomeFantasia());
        fornecedor.setCpfCnpj(req.getCpfCnpj());
        fornecedor.setIe(req.getIe());
        fornecedor.setEmail(req.getEmail());
        fornecedor.setTelefone(req.getTelefone());
        fornecedor.setCelular(req.getCelular());
        fornecedor.setContato(req.getContato());
        fornecedor.setCep(req.getCep());
        fornecedor.setLogradouro(req.getLogradouro());
        fornecedor.setNumero(req.getNumero());
        fornecedor.setComplemento(req.getComplemento());
        fornecedor.setBairro(req.getBairro());
        fornecedor.setCidade(req.getCidade());
        fornecedor.setUf(req.getUf());
        fornecedor.setPrazoEntrega(req.getPrazoEntrega());
        fornecedor.setObservacoes(req.getObservacoes());
        return fornecedor;
    }

    private FornecedorDTO.Response toResponse(Fornecedor f) {
        return FornecedorDTO.Response.builder()
                .id(f.getId())
                .tipoPessoa(f.getTipoPessoa())
                .razaoSocial(f.getRazaoSocial())
                .nomeFantasia(f.getNomeFantasia())
                .cpfCnpj(f.getCpfCnpj())
                .ie(f.getIe())
                .email(f.getEmail())
                .telefone(f.getTelefone())
                .celular(f.getCelular())
                .contato(f.getContato())
                .cep(f.getCep())
                .logradouro(f.getLogradouro())
                .numero(f.getNumero())
                .complemento(f.getComplemento())
                .bairro(f.getBairro())
                .cidade(f.getCidade())
                .uf(f.getUf())
                .prazoEntrega(f.getPrazoEntrega())
                .ativo(f.getAtivo())
                .observacoes(f.getObservacoes())
                .criadoEm(f.getCriadoEm())
                .atualizadoEm(f.getAtualizadoEm())
                .build();
    }

    private FornecedorDTO.Resumo toResumo(Fornecedor f) {
        return FornecedorDTO.Resumo.builder()
                .id(f.getId())
                .razaoSocial(f.getRazaoSocial())
                .nomeFantasia(f.getNomeFantasia())
                .cpfCnpj(f.getCpfCnpj())
                .telefone(f.getTelefone())
                .cidade(f.getCidade())
                .uf(f.getUf())
                .ativo(f.getAtivo())
                .build();
    }
}
