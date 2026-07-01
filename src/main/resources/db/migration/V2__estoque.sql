-- V2__estoque.sql
-- Sprint 2: Módulo de Estoque — movimentações de entrada, saída e ajuste

-- =============================================
-- MOVIMENTOS DE ESTOQUE
-- =============================================
CREATE TABLE movimentos_estoque (
    id              UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    produto_id      UUID          NOT NULL REFERENCES produtos(id),
    tipo            VARCHAR(20)   NOT NULL,                -- ENTRADA_MANUAL, SAIDA_MANUAL, AJUSTE
    quantidade      NUMERIC(12,4) NOT NULL,
    custo_unitario  NUMERIC(12,4),                        -- preenchido em entradas
    saldo_apos      NUMERIC(12,4) NOT NULL,               -- saldo do produto após o movimento
    fornecedor_id   UUID          REFERENCES fornecedores(id),
    documento       VARCHAR(60),                          -- número de NF avulsa, pedido, etc.
    observacoes     TEXT,
    usuario_id      UUID          REFERENCES usuarios(id),
    criado_em       TIMESTAMP     NOT NULL DEFAULT NOW(),
    atualizado_em   TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_mov_produto      ON movimentos_estoque(produto_id);
CREATE INDEX idx_mov_tipo         ON movimentos_estoque(tipo);
CREATE INDEX idx_mov_criado_em    ON movimentos_estoque(criado_em);
CREATE INDEX idx_mov_produto_data ON movimentos_estoque(produto_id, criado_em DESC);
