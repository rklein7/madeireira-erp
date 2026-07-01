-- V4__financeiro.sql
-- Sprint 4: Módulo Financeiro — contas a receber e contas a pagar

-- =============================================
-- CONTAS A RECEBER
-- =============================================
CREATE TABLE contas_receber (
    id               UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
    pedido_id        UUID           REFERENCES pedidos(id),
    cliente_id       UUID           NOT NULL REFERENCES clientes(id),
    descricao        VARCHAR(255)   NOT NULL,
    valor            NUMERIC(12,2)  NOT NULL,
    data_vencimento  DATE           NOT NULL,
    data_pagamento   DATE,
    valor_pago       NUMERIC(12,2),
    status           VARCHAR(20)    NOT NULL DEFAULT 'ABERTO',
    forma_pagamento  VARCHAR(30),
    parcela          INTEGER        NOT NULL DEFAULT 1,
    total_parcelas   INTEGER        NOT NULL DEFAULT 1,
    observacoes      TEXT,
    usuario_id       UUID           REFERENCES usuarios(id),
    criado_em        TIMESTAMP      NOT NULL DEFAULT NOW(),
    atualizado_em    TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cr_cliente    ON contas_receber(cliente_id);
CREATE INDEX idx_cr_status     ON contas_receber(status);
CREATE INDEX idx_cr_vencimento ON contas_receber(data_vencimento);
CREATE INDEX idx_cr_pedido     ON contas_receber(pedido_id);

-- =============================================
-- CONTAS A PAGAR
-- =============================================
CREATE TABLE contas_pagar (
    id               UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
    fornecedor_id    UUID           REFERENCES fornecedores(id),
    descricao        VARCHAR(255)   NOT NULL,
    valor            NUMERIC(12,2)  NOT NULL,
    data_vencimento  DATE           NOT NULL,
    data_pagamento   DATE,
    valor_pago       NUMERIC(12,2),
    status           VARCHAR(20)    NOT NULL DEFAULT 'ABERTO',
    forma_pagamento  VARCHAR(30),
    documento        VARCHAR(60),
    observacoes      TEXT,
    usuario_id       UUID           REFERENCES usuarios(id),
    criado_em        TIMESTAMP      NOT NULL DEFAULT NOW(),
    atualizado_em    TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cp_status     ON contas_pagar(status);
CREATE INDEX idx_cp_vencimento ON contas_pagar(data_vencimento);
