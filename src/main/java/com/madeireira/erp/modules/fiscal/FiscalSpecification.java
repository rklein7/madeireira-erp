package com.madeireira.erp.modules.fiscal;

import com.madeireira.erp.modules.fiscal.entity.NotaFiscal;
import com.madeireira.erp.modules.fiscal.entity.StatusNF;
import com.madeireira.erp.modules.fiscal.entity.TipoNF;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FiscalSpecification {

    private FiscalSpecification() {}

    public static Specification<NotaFiscal> build(
            TipoNF tipo, StatusNF status,
            UUID fornecedorId, UUID clienteId,
            LocalDate de, LocalDate ate) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (tipo != null) {
                predicates.add(cb.equal(root.get("tipo"), tipo));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (fornecedorId != null) {
                predicates.add(cb.equal(root.get("fornecedor").get("id"), fornecedorId));
            }
            if (clienteId != null) {
                predicates.add(cb.equal(root.get("cliente").get("id"), clienteId));
            }
            if (de != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataEmissao"), de));
            }
            if (ate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dataEmissao"), ate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
