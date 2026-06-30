# ERP Madeireira — Backend Spring Boot

## Stack

- Java 21 + Spring Boot 3.3
- PostgreSQL 16
- Flyway (migrations)
- Spring Security (JWT)
- SpringDoc OpenAPI (Swagger)
- Lombok + MapStruct

## Setup rápido

### 1. Subir o banco
```bash
docker compose up -d
```

### 2. Rodar a aplicação
```bash
./mvnw spring-boot:run
```

A aplicação sobe na porta `8080`.  
Swagger disponível em: http://localhost:8080/swagger-ui.html

### 3. Variáveis de ambiente (opcional)
```bash
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=seu-secret-aqui
```

---

## Módulos e endpoints — Sprint 1 (Cadastros)

### Produtos `GET /api/v1/produtos`
| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/v1/produtos?busca=pinus&page=0&size=20` | Listagem paginada com busca |
| GET | `/api/v1/produtos/{id}` | Detalhe do produto |
| POST | `/api/v1/produtos` | Criar produto |
| PUT | `/api/v1/produtos/{id}` | Atualizar produto |
| DELETE | `/api/v1/produtos/{id}` | Inativar produto |
| GET | `/api/v1/produtos/alertas/estoque-minimo` | Produtos abaixo do mínimo |

### Clientes `GET /api/v1/clientes`
| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/v1/clientes?busca=joao&page=0` | Listagem paginada |
| GET | `/api/v1/clientes/{id}` | Detalhe do cliente |
| POST | `/api/v1/clientes` | Cadastrar cliente |
| PUT | `/api/v1/clientes/{id}` | Atualizar cliente |
| DELETE | `/api/v1/clientes/{id}` | Inativar cliente |

### Fornecedores `GET /api/v1/fornecedores`
| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/v1/fornecedores?busca=madex` | Listagem paginada |
| GET | `/api/v1/fornecedores/{id}` | Detalhe |
| POST | `/api/v1/fornecedores` | Cadastrar |
| PUT | `/api/v1/fornecedores/{id}` | Atualizar |
| DELETE | `/api/v1/fornecedores/{id}` | Inativar |

---

## Estrutura do projeto

```
src/main/java/com/madeireira/erp/
├── ErpApplication.java
├── modules/
│   ├── cadastro/
│   │   ├── controller/       ← REST controllers
│   │   ├── service/          ← Regras de negócio
│   │   ├── repository/       ← JPA repositories
│   │   ├── dto/              ← Request/Response DTOs
│   │   └── entity/           ← Entidades JPA
│   ├── estoque/              ← Sprint 2
│   ├── vendas/               ← Sprint 3
│   ├── fiscal/               ← Sprint 4
│   └── financeiro/           ← Sprint 5
└── shared/
    ├── config/               ← SecurityConfig, etc.
    ├── exception/            ← GlobalExceptionHandler
    └── BaseEntity.java       ← Auditoria (id, criadoEm, atualizadoEm)
```

---

## Unidades de medida (madeireira)

| Enum | Símbolo | Uso |
|------|---------|-----|
| M2   | m²      | Chapas, placas, compensados |
| M3   | m³      | Madeira em tora, vigas |
| KG   | kg      | Parafusos, pregos, insumos |
| PECA | pç      | Itens unitários |
| ML   | ml      | Molduras, rodapés (metro linear) |
| ROLO | rl      | Lonas, filmes |

---

## Próximas sprints

- **Sprint 2 — Estoque**: `MovimentoEstoque` (ENTRADA/SAIDA/AJUSTE), saldo em tempo real
- **Sprint 3 — Vendas**: `Pedido` → `ItemPedido`, baixa automática de estoque
- **Sprint 4 — Fiscal**: `NotaFiscal` com CFOP, destinatário, tributos
- **Sprint 5 — Financeiro**: `ContaReceber`, `ContaPagar`, fluxo de caixa
