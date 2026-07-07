ALTER TABLE pedidos
    ADD COLUMN vendedor_id UUID REFERENCES usuarios(id);

CREATE INDEX idx_pedidos_vendedor ON pedidos(vendedor_id);
