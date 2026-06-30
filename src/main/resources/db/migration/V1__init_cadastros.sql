-- V1__init_cadastros.sql
-- Sprint 1: Módulo de Cadastros

-- Extensão para UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================
-- USUÁRIOS DO SISTEMA
-- =============================================
CREATE TABLE usuarios (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome        VARCHAR(150) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    senha       VARCHAR(255) NOT NULL,
    perfil      VARCHAR(30)  NOT NULL DEFAULT 'OPERADOR', -- ADMIN, GERENTE, OPERADOR, FINANCEIRO
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em   TIMESTAMP    NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP  NOT NULL DEFAULT NOW()
);

-- =============================================
-- CATEGORIAS DE PRODUTO
-- =============================================
CREATE TABLE categorias (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome        VARCHAR(100) NOT NULL UNIQUE,
    descricao   VARCHAR(255),
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em   TIMESTAMP    NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP    NOT NULL DEFAULT NOW()
);

INSERT INTO categorias (nome) VALUES
    ('Pinus'),
    ('Eucalipto'),
    ('MDF'),
    ('OSB'),
    ('Compensado'),
    ('Tábua Bruta'),
    ('Viga'),
    ('Caibro'),
    ('Ripão'),
    ('Outros');

-- =============================================
-- PRODUTOS
-- =============================================
CREATE TABLE produtos (
    id              UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    codigo          VARCHAR(30)  NOT NULL UNIQUE,
    descricao       VARCHAR(255) NOT NULL,
    descricao_curta VARCHAR(100),
    unidade_medida  VARCHAR(10)  NOT NULL, -- M2, M3, KG, PECA, ML, ROLO
    preco_custo     NUMERIC(12,4),
    preco_venda     NUMERIC(12,4) NOT NULL,
    estoque_atual   NUMERIC(12,4) NOT NULL DEFAULT 0,
    estoque_minimo  NUMERIC(12,4) NOT NULL DEFAULT 0,
    estoque_maximo  NUMERIC(12,4),
    ncm             VARCHAR(10),           -- código fiscal
    peso_unitario   NUMERIC(10,4),         -- em kg
    largura_cm      NUMERIC(8,2),
    comprimento_cm  NUMERIC(8,2),
    espessura_mm    NUMERIC(6,2),
    categoria_id    UUID REFERENCES categorias(id),
    ativo           BOOLEAN      NOT NULL DEFAULT TRUE,
    observacoes     TEXT,
    criado_em       TIMESTAMP    NOT NULL DEFAULT NOW(),
    atualizado_em   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_produtos_codigo      ON produtos(codigo);
CREATE INDEX idx_produtos_categoria   ON produtos(categoria_id);
CREATE INDEX idx_produtos_ativo       ON produtos(ativo);

-- =============================================
-- CLIENTES
-- =============================================
CREATE TABLE clientes (
    id              UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    tipo_pessoa     VARCHAR(2)  NOT NULL DEFAULT 'PJ', -- PF, PJ
    razao_social    VARCHAR(200) NOT NULL,
    nome_fantasia   VARCHAR(200),
    cpf_cnpj        VARCHAR(18)  NOT NULL UNIQUE,
    ie              VARCHAR(20),             -- inscrição estadual
    im              VARCHAR(20),             -- inscrição municipal
    email           VARCHAR(150),
    telefone        VARCHAR(20),
    celular         VARCHAR(20),
    -- Endereço
    cep             VARCHAR(9),
    logradouro      VARCHAR(200),
    numero          VARCHAR(10),
    complemento     VARCHAR(100),
    bairro          VARCHAR(100),
    cidade          VARCHAR(100),
    uf              VARCHAR(2),
    -- Controle
    limite_credito  NUMERIC(12,2) DEFAULT 0,
    dias_prazo      INTEGER       DEFAULT 30,
    observacoes     TEXT,
    ativo           BOOLEAN       NOT NULL DEFAULT TRUE,
    criado_em       TIMESTAMP     NOT NULL DEFAULT NOW(),
    atualizado_em   TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_clientes_cpf_cnpj ON clientes(cpf_cnpj);
CREATE INDEX idx_clientes_nome     ON clientes(razao_social);
CREATE INDEX idx_clientes_ativo    ON clientes(ativo);

-- =============================================
-- FORNECEDORES
-- =============================================
CREATE TABLE fornecedores (
    id              UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    tipo_pessoa     VARCHAR(2)  NOT NULL DEFAULT 'PJ',
    razao_social    VARCHAR(200) NOT NULL,
    nome_fantasia   VARCHAR(200),
    cpf_cnpj        VARCHAR(18)  NOT NULL UNIQUE,
    ie              VARCHAR(20),
    email           VARCHAR(150),
    telefone        VARCHAR(20),
    celular         VARCHAR(20),
    contato         VARCHAR(150),           -- nome do contato comercial
    -- Endereço
    cep             VARCHAR(9),
    logradouro      VARCHAR(200),
    numero          VARCHAR(10),
    complemento     VARCHAR(100),
    bairro          VARCHAR(100),
    cidade          VARCHAR(100),
    uf              VARCHAR(2),
    -- Controle
    prazo_entrega   INTEGER,               -- dias
    observacoes     TEXT,
    ativo           BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em       TIMESTAMP    NOT NULL DEFAULT NOW(),
    atualizado_em   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_fornecedores_cpf_cnpj ON fornecedores(cpf_cnpj);
CREATE INDEX idx_fornecedores_nome     ON fornecedores(razao_social);

-- =============================================
-- TABELA DE PREÇOS (por cliente ou grupo)
-- =============================================
CREATE TABLE tabelas_preco (
    id          UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome        VARCHAR(100) NOT NULL,
    descricao   VARCHAR(255),
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em   TIMESTAMP    NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP  NOT NULL DEFAULT NOW()
);

INSERT INTO tabelas_preco (nome, descricao) VALUES
    ('Tabela Padrão', 'Preços padrão de venda'),
    ('Tabela Atacado', 'Preços para clientes atacadistas'),
    ('Tabela Especial', 'Preços negociados individualmente');

CREATE TABLE tabela_preco_itens (
    id              UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    tabela_id       UUID          NOT NULL REFERENCES tabelas_preco(id),
    produto_id      UUID          NOT NULL REFERENCES produtos(id),
    preco           NUMERIC(12,4) NOT NULL,
    desconto_max    NUMERIC(5,2)  DEFAULT 0, -- % máximo de desconto permitido
    vigencia_inicio DATE,
    vigencia_fim    DATE,
    criado_em       TIMESTAMP     NOT NULL DEFAULT NOW(),
    atualizado_em   TIMESTAMP     NOT NULL DEFAULT NOW(),
    UNIQUE (tabela_id, produto_id)
);

-- Relaciona clientes à tabela de preços
ALTER TABLE clientes ADD COLUMN tabela_preco_id UUID REFERENCES tabelas_preco(id);

-- =============================================
-- AUDITORIA SIMPLES
-- =============================================
CREATE TABLE audit_log (
    id          BIGSERIAL    PRIMARY KEY,
    tabela      VARCHAR(60)  NOT NULL,
    registro_id UUID         NOT NULL,
    acao        VARCHAR(10)  NOT NULL, -- INSERT, UPDATE, DELETE
    usuario_id  UUID         REFERENCES usuarios(id),
    dados_antes JSONB,
    dados_apos  JSONB,
    criado_em   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_tabela     ON audit_log(tabela, registro_id);
CREATE INDEX idx_audit_criado_em  ON audit_log(criado_em);
