package com.madeireira.erp.modules.cadastro.repository;

import com.madeireira.erp.modules.cadastro.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {
    List<Categoria> findByAtivoTrueOrderByNome();
    boolean existsByNome(String nome);
}
