package com.madeireira.erp.modules.cadastro.service;

import com.madeireira.erp.modules.cadastro.dto.ClienteDTO;
import com.madeireira.erp.modules.cadastro.repository.ClienteRepository;
import com.madeireira.erp.shared.exception.BusinessException;
import com.madeireira.erp.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ClienteServiceTest {

    @Autowired private ClienteService clienteService;
    @Autowired private ClienteRepository clienteRepository;

    @BeforeEach
    void limpar() {
        clienteRepository.deleteAll();
    }

    private ClienteDTO.Request clienteValido() {
        return ClienteDTO.Request.builder()
                .tipoPessoa("PJ")
                .razaoSocial("Construtora ABC LTDA")
                .cpfCnpj("11.222.333/0001-44")
                .email("contato@construtoraabc.com.br")
                .cidade("Curitiba")
                .uf("PR")
                .build();
    }

    @Test
    void criar_comDadosValidos_retornaIdGerado() {
        var response = clienteService.criar(clienteValido());

        assertNotNull(response.getId());
        assertEquals("Construtora ABC LTDA", response.getRazaoSocial());
        assertEquals("11.222.333/0001-44", response.getCpfCnpj());
        assertTrue(response.getAtivo());
    }

    @Test
    void criar_comCpfCnpjDuplicado_lancaBusinessException() {
        clienteService.criar(clienteValido());

        assertThrows(BusinessException.class, () -> clienteService.criar(clienteValido()));
    }

    @Test
    void buscarPorId_idInexistente_lancaNotFoundException() {
        assertThrows(NotFoundException.class, () -> clienteService.buscarPorId(UUID.randomUUID()));
    }

    @Test
    void atualizar_refleteMudancas() {
        var criado = clienteService.criar(clienteValido());

        var novoRequest = ClienteDTO.Request.builder()
                .tipoPessoa("PJ")
                .razaoSocial("Construtora ABC LTDA ATUALIZADA")
                .cpfCnpj("11.222.333/0001-44")
                .cidade("São Paulo")
                .uf("SP")
                .build();

        var atualizado = clienteService.atualizar(criado.getId(), novoRequest);

        assertEquals(criado.getId(), atualizado.getId());
        assertEquals("Construtora ABC LTDA ATUALIZADA", atualizado.getRazaoSocial());
        assertEquals("São Paulo", atualizado.getCidade());
        assertEquals("SP", atualizado.getUf());
    }

    @Test
    void inativar_marcaAtivoFalso() {
        var criado = clienteService.criar(clienteValido());

        clienteService.inativar(criado.getId());

        var cliente = clienteService.buscarPorId(criado.getId());
        assertFalse(cliente.getAtivo());
    }
}
