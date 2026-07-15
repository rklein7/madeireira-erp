CREATE SEQUENCE seq_pedido_compra_numero START 1 INCREMENT 1;

CREATE TABLE pedidos_compra (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    numero          VARCHAR(20)  NOT NULL UNIQUE,
    status          VARCHAR(30)  NOT NULL,
    fornecedor_id   UUID         NOT NULL REFERENCES fornecedores(id),
    condicao_pagamento VARCHAR(30) NOT NULL,
    parcelas        INTEGER      NOT NULL DEFAULT 1,
    valor_frete     NUMERIC(12,2) NOT NULL DEFAULT 0,
    valor_total     NUMERIC(12,2) NOT NULL DEFAULT 0,
    observacoes     TEXT,
    usuario_id      UUID         REFERENCES usuarios(id),
    criado_em       TIMESTAMP    NOT NULL DEFAULT NOW(),
    atualizado_em   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE itens_pedido_compra (
    id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_compra_id  UUID         NOT NULL REFERENCES pedidos_compra(id) ON DELETE CASCADE,
    produto_id        UUID         NOT NULL REFERENCES produtos(id),
    numero_item       INTEGER      NOT NULL,
    quantidade        NUMERIC(12,4) NOT NULL,
    preco_unitario    NUMERIC(12,4) NOT NULL,
    valor_total       NUMERIC(12,2) NOT NULL,
    criado_em         TIMESTAMP    NOT NULL DEFAULT NOW(),
    atualizado_em     TIMESTAMP    NOT NULL DEFAULT NOW()
);

ALTER TABLE notas_fiscais ADD COLUMN pedido_compra_id UUID REFERENCES pedidos_compra(id);

CREATE INDEX idx_pedidos_compra_fornecedor   ON pedidos_compra(fornecedor_id);
CREATE INDEX idx_pedidos_compra_status       ON pedidos_compra(status);
CREATE INDEX idx_itens_pedido_compra_pedido  ON itens_pedido_compra(pedido_compra_id);
CREATE INDEX idx_itens_pedido_compra_produto ON itens_pedido_compra(produto_id);
CREATE INDEX idx_notas_fiscais_pedido_compra ON notas_fiscais(pedido_compra_id);
