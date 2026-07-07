-- Adiciona campos de banco nas tabelas financeiras
ALTER TABLE contas_receber
    ADD COLUMN codigo_banco VARCHAR(10),
    ADD COLUMN nome_banco   VARCHAR(100);

ALTER TABLE contas_pagar
    ADD COLUMN codigo_banco VARCHAR(10),
    ADD COLUMN nome_banco   VARCHAR(100);
