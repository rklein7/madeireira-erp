package com.madeireira.erp.modules.cadastro.repository;

import com.madeireira.erp.modules.cadastro.entity.TabelaPreco;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TabelaPrecoRepository extends JpaRepository<TabelaPreco, UUID> {
    List<TabelaPreco> findByAtivoTrueOrderByNome();
}
