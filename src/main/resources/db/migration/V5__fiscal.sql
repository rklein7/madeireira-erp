-- V5__fiscal.sql
-- Sprint 5: Módulo Fiscal — notas fiscais e itens de nota fiscal

-- =============================================
-- NOTAS FISCAIS
-- =============================================
CREATE TABLE notas_fiscais (
    id                  UUID            PRIMARY KEY DEFAULT uuid_generate_v4(),
    tipo                VARCHAR(10)     NOT NULL,
    status              VARCHAR(30)     NOT NULL DEFAULT 'ESCRITURADA_MANUAL',
    numero              VARCHAR(20)     NOT NULL,
    serie               VARCHAR(5)      NOT NULL DEFAULT '1',
    cfop                VARCHAR(10)     NOT NULL,
    chave_acesso        VARCHAR(44),
    data_emissao        DATE            NOT NULL,
    data_entrada_saida  DATE            NOT NULL,
    natureza_operacao   VARCHAR(100),
    -- Emitente / Destinatário
    fornecedor_id       UUID            REFERENCES fornecedores(id),
    cliente_id          UUID            REFERENCES clientes(id),
    pedido_id           UUID            REFERENCES pedidos(id),
    -- Totais
    valor_produtos      NUMERIC(12,2)   NOT NULL DEFAULT 0,
    valor_frete         NUMERIC(12,2)   NOT NULL DEFAULT 0,
    valor_seguro        NUMERIC(12,2)   NOT NULL DEFAULT 0,
    valor_desconto      NUMERIC(12,2)   NOT NULL DEFAULT 0,
    valor_ipi           NUMERIC(12,2)   NOT NULL DEFAULT 0,
    valor_icms          NUMERIC(12,2)   NOT NULL DEFAULT 0,
    valor_pis           NUMERIC(12,2)   NOT NULL DEFAULT 0,
    valor_cofins        NUMERIC(12,2)   NOT NULL DEFAULT 0,
    valor_total         NUMERIC(12,2)   NOT NULL DEFAULT 0,
    -- Controle
    observacoes         TEXT,
    usuario_id          UUID            REFERENCES usuarios(id),
    criado_em           TIMESTAMP       NOT NULL DEFAULT NOW(),
    atualizado_em       TIMESTAMP       NOT NULL DEFAULT NOW(),
    -- Evita duplicidade de NF de entrada do mesmo fornecedor
    UNIQUE (tipo, numero, serie, fornecedor_id)
);

CREATE INDEX idx_nf_tipo_status ON notas_fiscais(tipo, status);
CREATE INDEX idx_nf_fornecedor  ON notas_fiscais(fornecedor_id);
CREATE INDEX idx_nf_cliente     ON notas_fiscais(cliente_id);
CREATE INDEX idx_nf_pedido      ON notas_fiscais(pedido_id);
CREATE INDEX idx_nf_emissao     ON notas_fiscais(data_emissao);

-- =============================================
-- ITENS DE NOTA FISCAL
-- =============================================
CREATE TABLE itens_nota_fiscal (
    id              UUID            PRIMARY KEY DEFAULT uuid_generate_v4(),
    nota_fiscal_id  UUID            NOT NULL REFERENCES notas_fiscais(id) ON DELETE CASCADE,
    produto_id      UUID            NOT NULL REFERENCES produtos(id),
    numero_item     INTEGER         NOT NULL,
    quantidade      NUMERIC(12,4)   NOT NULL,
    valor_unitario  NUMERIC(12,4)   NOT NULL,
    valor_total     NUMERIC(12,2)   NOT NULL,
    -- ICMS
    cst_icms        VARCHAR(5),
    aliq_icms       NUMERIC(5,2)    DEFAULT 0,
    valor_icms      NUMERIC(12,2)   DEFAULT 0,
    -- IPI
    cst_ipi         VARCHAR(5),
    aliq_ipi        NUMERIC(5,2)    DEFAULT 0,
    valor_ipi       NUMERIC(12,2)   DEFAULT 0,
    -- PIS
    cst_pis         VARCHAR(5),
    aliq_pis        NUMERIC(5,2)    DEFAULT 0,
    valor_pis       NUMERIC(12,2)   DEFAULT 0,
    -- COFINS
    cst_cofins      VARCHAR(5),
    aliq_cofins     NUMERIC(5,2)    DEFAULT 0,
    valor_cofins    NUMERIC(12,2)   DEFAULT 0,
    -- Auditoria
    criado_em       TIMESTAMP       NOT NULL DEFAULT NOW(),
    atualizado_em   TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_item_nf ON itens_nota_fiscal(nota_fiscal_id);
