-- V3__vendas.sql
-- Sprint 3: Módulo de Vendas — pedidos e itens de pedido

-- =============================================
-- SEQUENCE PARA NUMERAÇÃO AUTOMÁTICA
-- =============================================
CREATE SEQUENCE seq_pedido_numero START 1;

-- =============================================
-- PEDIDOS
-- =============================================
CREATE TABLE pedidos (
    id                  UUID            PRIMARY KEY DEFAULT uuid_generate_v4(),
    numero              VARCHAR(20)     NOT NULL UNIQUE,
    cliente_id          UUID            NOT NULL REFERENCES clientes(id),
    status              VARCHAR(20)     NOT NULL DEFAULT 'RASCUNHO',
    condicao_pagamento  VARCHAR(20)     NOT NULL,
    parcelas            INTEGER                  DEFAULT 1,
    valor_subtotal      NUMERIC(12,2)   NOT NULL DEFAULT 0,
    valor_desconto      NUMERIC(12,2)   NOT NULL DEFAULT 0,
    valor_frete         NUMERIC(12,2)   NOT NULL DEFAULT 0,
    valor_total         NUMERIC(12,2)   NOT NULL DEFAULT 0,
    observacoes         TEXT,
    usuario_id          UUID            REFERENCES usuarios(id),
    criado_em           TIMESTAMP       NOT NULL DEFAULT NOW(),
    atualizado_em       TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pedidos_cliente    ON pedidos(cliente_id);
CREATE INDEX idx_pedidos_status     ON pedidos(status);
CREATE INDEX idx_pedidos_criado_em  ON pedidos(criado_em);

-- =============================================
-- ITENS DE PEDIDO
-- =============================================
CREATE TABLE itens_pedido (
    id              UUID            PRIMARY KEY DEFAULT uuid_generate_v4(),
    pedido_id       UUID            NOT NULL REFERENCES pedidos(id) ON DELETE CASCADE,
    produto_id      UUID            NOT NULL REFERENCES produtos(id),
    quantidade      NUMERIC(12,4)   NOT NULL,
    preco_unitario  NUMERIC(12,4)   NOT NULL,
    desconto_perc   NUMERIC(5,2)    NOT NULL DEFAULT 0,
    valor_total     NUMERIC(12,2)   NOT NULL,
    criado_em       TIMESTAMP       NOT NULL DEFAULT NOW(),
    atualizado_em   TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_itens_pedido   ON itens_pedido(pedido_id);
CREATE INDEX idx_itens_produto  ON itens_pedido(produto_id);
