package com.madeireira.erp.modules.auth.controller;

import com.madeireira.erp.modules.auth.dto.UsuarioDTO;
import com.madeireira.erp.modules.auth.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Listagem de usuários do sistema")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @Operation(summary = "Listar usuários (use ?perfil=VENDEDOR para filtrar vendedores)")
    public ResponseEntity<Page<UsuarioDTO.Resumo>> listar(
            @RequestParam(required = false) String perfil,
            @PageableDefault(size = 50) Pageable pageable) {

        Page<UsuarioDTO.Resumo> page = (perfil != null && !perfil.isBlank())
                ? usuarioRepository.findByPerfil(perfil.toUpperCase(), pageable).map(this::toResumo)
                : usuarioRepository.findAll(pageable).map(this::toResumo);

        return ResponseEntity.ok(page);
    }

    private UsuarioDTO.Resumo toResumo(com.madeireira.erp.modules.auth.entity.Usuario u) {
        return UsuarioDTO.Resumo.builder()
                .id(u.getId())
                .nome(u.getNome())
                .email(u.getEmail())
                .perfil(u.getPerfil())
                .ativo(u.getAtivo())
                .build();
    }
}
