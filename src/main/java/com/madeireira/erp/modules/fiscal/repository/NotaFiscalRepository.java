package com.madeireira.erp.modules.fiscal.repository;

import com.madeireira.erp.modules.fiscal.entity.NotaFiscal;
import com.madeireira.erp.modules.fiscal.entity.StatusNF;
import com.madeireira.erp.modules.fiscal.entity.TipoNF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotaFiscalRepository extends JpaRepository<NotaFiscal, UUID>,
        JpaSpecificationExecutor<NotaFiscal> {

    Optional<NotaFiscal> findByPedidoId(UUID pedidoId);

    Optional<NotaFiscal> findByPedidoCompraIdAndStatusNot(UUID pedidoCompraId, StatusNF status);

    boolean existsByNumeroAndSerieAndTipoAndFornecedorId(
            String numero, String serie, TipoNF tipo, UUID fornecedorId);

    @Query("SELECT n FROM NotaFiscal n WHERE n.dataEmissao BETWEEN :de AND :ate")
    List<NotaFiscal> findByPeriodo(
            @Param("de") LocalDate de,
            @Param("ate") LocalDate ate);

    /**
     * Retorna por linha: [tipo (String), somaValorProdutos, somaValorIpi,
     * somaValorIcms, somaValorPis, somaValorCofins, somaValorTotal]
     * para o período informado, agrupado por tipo de NF.
     * Usado pelo resumo do contador / SPED.
     */
    @Query("SELECT n.tipo, " +
           "SUM(n.valorProdutos), SUM(n.valorIpi), SUM(n.valorIcms), " +
           "SUM(n.valorPis), SUM(n.valorCofins), SUM(n.valorTotal) " +
           "FROM NotaFiscal n " +
           "WHERE n.dataEmissao BETWEEN :de AND :ate " +
           "AND n.status <> com.madeireira.erp.modules.fiscal.entity.StatusNF.CANCELADA " +
           "GROUP BY n.tipo")
    List<Object[]> sumTributosPorTipoPeriodo(
            @Param("de") LocalDate de,
            @Param("ate") LocalDate ate);
}
