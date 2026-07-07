package com.madeireira.erp.modules.cadastro.controller;

import com.madeireira.erp.modules.cadastro.repository.CategoriaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorias", description = "Listagem de categorias de produtos")
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;

    @GetMapping
    @Operation(summary = "Listar categorias ativas")
    public ResponseEntity<List<Map<String, Object>>> listar() {
        List<Map<String, Object>> resultado = categoriaRepository.findByAtivoTrueOrderByNome()
                .stream()
                .map(c -> Map.<String, Object>of("id", c.getId().toString(), "nome", c.getNome()))
                .toList();
        return ResponseEntity.ok(resultado);
    }
}
