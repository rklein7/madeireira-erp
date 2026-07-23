# CLAUDE.md — madeireira-erp

Contexto completo do projeto para sessões futuras. Tudo aqui foi extraído do código, não inferido.

---

## Visão geral

ERP para uma madeireira em Carazinho/RS. Sistema web com backend Spring Boot e frontend separado (não está neste repositório). O backend expõe uma API REST consumida pelo frontend.

**Usuários do sistema:** operadores internos da madeireira (ADMIN, GERENTE, OPERADOR, FINANCEIRO, VENDEDOR).

**Estado atual — 8 sprints concluídas:**

| Sprint | Módulo |
|--------|--------|
| 1 | Cadastros (produtos, clientes, fornecedores, categorias, tabelas de preço) |
| 2 | Estoque (movimentações e saldos) |
| 3 | Vendas (pedidos de venda) |
| 4 | Financeiro (contas a receber, contas a pagar, fluxo de caixa) |
| 5 | Fiscal (notas fiscais de entrada e saída) |
| 6 | Financeiro — campos de banco nas contas e endpoint de lançamentos |
| 7 | Vendas — vendedor vinculado ao pedido |
| 8 | Compras (pedidos de compra com integração à NF de entrada) |

---

## Stack

Todas as versões vêm do `pom.xml`.

| Dependência | Versão |
|---|---|
| Java | 21 |
| Spring Boot | 3.3.4 |
| Lombok | 1.18.34 |
| MapStruct | 1.6.2 |
| jjwt (JWT) | 0.12.6 |
| springdoc-openapi | 2.6.0 |
| Flyway | gerenciado pelo Boot parent |
| H2 (testes) | gerenciado pelo Boot parent |
| PostgreSQL driver | gerenciado pelo Boot parent |

MapStruct está no `pom.xml` como dependência declarada, mas **não é usado no código** — todos os mapeamentos são feitos manualmente nos services via builder do Lombok. A dependência está presente mas é letra morta.

---

## Como rodar

**Pré-requisitos:**
- JDK 21
- PostgreSQL rodando em `localhost:5432` — **não Docker** (veja nota abaixo)
- Banco criado: `CREATE DATABASE madeireira_erp;`
- Usuário padrão: `postgres/postgres` (sobrescreva com `DB_USERNAME` e `DB_PASSWORD`)

**Subir:**
```bash
./mvnw spring-boot:run
# ou com variáveis de ambiente:
DB_USERNAME=xxx DB_PASSWORD=yyy JWT_SECRET=chave-longa-de-256-bits ./mvnw spring-boot:run
```

**application.yml** tem `spring.docker.compose.enabled: false` — o Postgres é esperado localmente, não em container gerenciado pelo Boot.

**URLs:**
- API base: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`
- Health: `http://localhost:8080/actuator/health`

**Credencial de teste:** `teste@test.com` / `Senha@123` (se o seed existir no banco).

**JWT:** validade de 24h (`jwt.expiration=86400000`). Secret padrão no yml é suficiente para desenvolvimento local.

**Rodar testes:**
```bash
./mvnw test
# usa H2 in-memory com application-test.yml (Flyway desabilitado, ddl-auto=create-drop)
```

---

## Estrutura de pacotes

```
src/main/java/com/madeireira/erp/
├── ErpApplication.java
├── modules/
│   ├── auth/          # Autenticação JWT, entidade Usuario
│   ├── cadastro/      # Clientes, Fornecedores, Produtos, Categorias, TabelaPreco
│   ├── estoque/       # MovimentoEstoque, saldos
│   ├── vendas/        # Pedido de venda, ItemPedido
│   ├── financeiro/    # ContaReceber, ContaPagar, fluxo de caixa
│   ├── fiscal/        # NotaFiscal, ItemNotaFiscal, FiscalSpecification
│   └── compras/       # PedidoCompra, ItemPedidoCompra
└── shared/
    ├── BaseEntity.java                    # @MappedSuperclass: id UUID, criadoEm, atualizadoEm
    ├── config/
    │   ├── SecurityConfig.java
    │   └── OpenApiConfig.java
    └── exception/
        ├── BusinessException.java         # → HTTP 400
        ├── NotFoundException.java         # → HTTP 404
        └── GlobalExceptionHandler.java    # @RestControllerAdvice
```

**Padrão de cada módulo:** `entity/` → `repository/` → `service/` → `controller/` → `dto/`

`auth` tem adicionalmente `filter/JwtAuthenticationFilter.java`.
`fiscal` tem `FiscalSpecification.java` solto na raiz do pacote (fora de `entity/`).

---

## Migrations

| Versão | Arquivo | O que criou/alterou |
|--------|---------|---------------------|
| V1 | `V1__init_cadastros.sql` | `usuarios`, `categorias` (com 10 seeds), `produtos`, `clientes`, `fornecedores`, `tabelas_preco`, `tabela_preco_itens`, `audit_log`; ADD COLUMN `tabela_preco_id` em `clientes` |
| V2 | `V2__estoque.sql` | `movimentos_estoque` com 4 índices |
| V3 | `V3__vendas.sql` | `seq_pedido_numero`, `pedidos`, `itens_pedido` |
| V4 | `V4__financeiro.sql` | `contas_receber`, `contas_pagar` |
| V5 | `V5__fiscal.sql` | `notas_fiscais` (com UNIQUE em tipo+numero+serie+fornecedor_id), `itens_nota_fiscal` |
| V6 | `V6__financeiro_banco.sql` | ADD COLUMN `codigo_banco` e `nome_banco` em `contas_receber` e `contas_pagar` |
| V7 | `V7__vendas_vendedor.sql` | ADD COLUMN `vendedor_id UUID` em `pedidos`; índice `idx_pedidos_vendedor` |
| V8 | `V8__compras.sql` | `seq_pedido_compra_numero`, `pedidos_compra`, `itens_pedido_compra`; ADD COLUMN `pedido_compra_id` em `notas_fiscais`; 5 índices |

---

## Módulos e regras de negócio

### auth

**Entidades:** `Usuario` (tabela `usuarios`) — campos: `nome`, `email`, `senha` (BCrypt), `perfil` (padrão `"OPERADOR"`), `ativo`.

**Perfis válidos** (string livre, sem enum): `ADMIN`, `GERENTE`, `OPERADOR`, `FINANCEIRO`, `VENDEDOR`.

**Endpoints:**

| Método | Path | Auth |
|--------|------|------|
| POST | `/api/v1/auth/login` | público |
| POST | `/api/v1/auth/registrar` | público |
| GET | `/api/v1/usuarios` | JWT |

`GET /usuarios?perfil=VENDEDOR` filtra por perfil (string, case-insensitive via `.toUpperCase()`).

**Regras:**
- `registrar` cria usuário e já retorna o token JWT — sem confirmação de e-mail.
- E-mail é único; duplicata lança `BusinessException` (400).
- `login` usa `AuthenticationManager` do Spring Security; credencial errada resulta em 401.
- Token usa HS384 (definido implicitamente pelo jjwt ao usar chave HMAC de 256+ bits).

---

### cadastro

**Entidades:** `Categoria`, `Cliente`, `Fornecedor`, `Produto`, `TabelaPreco`, `TabelaPrecoItem`.

**Campo nome:** em `Cliente` e `Fornecedor` o nome fica em `razaoSocial` — não há campo `nome`.

**UnidadeMedida (enum):** `M2`, `M3`, `KG`, `PECA`, `ML`, `ROLO`, `DUZIA`, `CENTO`.

**Produto — campos relevantes:**
- `codigo` — único no sistema (índice único)
- `estoqueAtual` — atualizado apenas via `EstoqueService.registrarMovimento()`, nunca diretamente
- `precoCusto` — nullable
- `precoVenda` — non-null, usado como snapshot em pedidos de venda

**Endpoints:**

| Método | Path | Retorno |
|--------|------|---------|
| GET | `/api/v1/categorias` | `List<{id, nome}>` (sem paginação) — só ativas |
| GET/POST/PUT/DELETE | `/api/v1/clientes` | CRUD completo |
| GET/POST/PUT/DELETE | `/api/v1/fornecedores` | CRUD completo |
| GET/POST/PUT/DELETE | `/api/v1/produtos` | CRUD + inativação lógica |
| GET | `/api/v1/produtos/alertas/estoque-minimo` | produtos abaixo do mínimo |
| GET/POST | `/api/v1/tabelas-preco` | listagem e criação |
| GET/POST itens/DELETE item | `/api/v1/tabelas-preco/{id}/itens` | gerenciar itens |

`GET /produtos?busca=termo` filtra por código ou descrição (case-insensitive).

**Regras:**
- Inativação de cliente, fornecedor e produto é lógica (`ativo = false`) — sem DELETE físico.
- `DELETE /produtos/{id}` retorna 204 e apenas marca `ativo = false`.
- Um produto não pode aparecer duas vezes na mesma tabela de preço (UNIQUE em tabela_id + produto_id).
- `CategoriaController` acessa o repositório diretamente, sem service.

---

### estoque

**Entidades:** `MovimentoEstoque`.

**TipoMovimento (enum):**
- `ENTRADA_MANUAL` — soma ao saldo
- `ENTRADA_NF` — soma ao saldo (usado automaticamente pela NF de entrada)
- `SAIDA_MANUAL` — subtrai do saldo; valida se saldo é suficiente
- `SAIDA_PEDIDO` — subtrai do saldo (usado automaticamente ao confirmar pedido)
- `AJUSTE` — substitui o saldo pelo valor informado (saldo absoluto, não incremental)

**Endpoints:**

| Método | Path | Descrição |
|--------|------|-----------|
| POST | `/api/v1/estoque/movimentos` | registrar movimento manual |
| GET | `/api/v1/estoque/movimentos?produtoId=&tipo=&de=&ate=` | histórico por produto |
| GET | `/api/v1/estoque/saldo/{produtoId}` | saldo atual + último movimento |
| GET | `/api/v1/estoque/posicao` | posição de todos os produtos ativos |

**Regras:**
- `AJUSTE`: a `quantidade` no payload é o **novo saldo absoluto**, não um delta. Ex: `quantidade=50` significa "o estoque agora é 50", seja qual for o valor anterior.
- `SAIDA_MANUAL`: valida estoque disponível antes de registrar. `SAIDA_PEDIDO` não valida (a validação é feita antes em `VendasService.confirmarPedido`).
- Cada movimento grava `saldoApos` para auditoria — o histórico mostra a evolução do saldo.
- O `usuarioAtual` é resolvido via `SecurityContextHolder` (pode ser `null` em chamadas internas de outros services).
- Quando `de` e `ate` são informados no filtro, o parâmetro `tipo` é **ignorado**.

---

### vendas

**Entidades:** `Pedido`, `ItemPedido`.

**StatusPedido (enum):** `RASCUNHO` → `CONFIRMADO` → `FATURADO` → `ENTREGUE` / `CANCELADO`.

- `podeCancelar()` retorna `true` só em `RASCUNHO` e `CONFIRMADO`.

**CondicaoPagamento (enum):** `A_VISTA`, `A_PRAZO`, `PARCELADO`, `CHEQUE`, `CARTAO`.

**Endpoints:**

| Método | Path | Descrição |
|--------|------|-----------|
| GET | `/api/v1/pedidos` | listar com filtros |
| GET | `/api/v1/pedidos/{id}` | detalhe completo |
| POST | `/api/v1/pedidos` | criar (status inicial: RASCUNHO) |
| PATCH | `/api/v1/pedidos/{id}/confirmar` | RASCUNHO → CONFIRMADO |
| PATCH | `/api/v1/pedidos/{id}/faturar` | CONFIRMADO → FATURADO |
| PATCH | `/api/v1/pedidos/{id}/entregar` | FATURADO → ENTREGUE |
| PATCH | `/api/v1/pedidos/{id}/cancelar` | RASCUNHO/CONFIRMADO → CANCELADO |

`GET /pedidos?clienteId=&status=&semNfVinculada=true`

**Regras:**
- Numeração automática via `seq_pedido_numero`: formato `PED-AAAA-NNNNN` (ex: `PED-2026-00001`).
- `precoUnitario` do item é snapshot do `produto.precoVenda` no momento da criação — **não se atualiza se o preço do produto mudar depois**.
- `confirmarPedido`: valida estoque de **todos os itens** antes de movimentar qualquer um (two-loop). Se um produto falhar, nenhum movimento é registrado.
- `faturarPedido`: chama `FinanceiroService.gerarContasReceberDoPedido()` na mesma transação — gera parcelas de ContaReceber automaticamente.
- `cancelarPedido`: estorna estoque (via `ENTRADA_MANUAL`) **somente se estava CONFIRMADO**. Se estava RASCUNHO, não há estorno (estoque nunca foi baixado).
- `vendedorId` é opcional no `Request`; `PedidoDTO.Response` expõe `vendedorId` e `vendedorNome` (podem ser null).
- Filtro `semNfVinculada=true`: retorna pedidos FATURADOS que não têm NF de saída não-cancelada vinculada. Útil para emissão de NF.
- `PedidoDTO.Response` inclui 8 campos de endereço e contato do cliente (para uso no PDF do pedido).

---

### financeiro

**Entidades:** `ContaReceber`, `ContaPagar`.

**StatusConta (enum):** `ABERTO`, `PAGO`, `VENCIDO`, `CANCELADO`.

**FormaPagamento (enum):** `DINHEIRO`, `PIX`, `BOLETO`, `CARTAO_CREDITO`, `CARTAO_DEBITO`, `CHEQUE`, `TRANSFERENCIA`.

**Endpoints:**

| Método | Path | Descrição |
|--------|------|-----------|
| GET | `/api/v1/financeiro/contas-receber` | listagem com filtros |
| GET | `/api/v1/financeiro/contas-receber/{id}` | detalhe |
| POST | `/api/v1/financeiro/contas-receber/{id}/pagar` | quitar conta |
| GET | `/api/v1/financeiro/contas-pagar` | listagem |
| POST | `/api/v1/financeiro/contas-pagar` | lançamento manual |
| POST | `/api/v1/financeiro/contas-pagar/{id}/pagar` | quitar conta |
| GET | `/api/v1/financeiro/lancamentos` | recebimentos + pagamentos efetivados |
| GET | `/api/v1/financeiro/fluxo-caixa?de=&ate=` | agregado mensal |

**Regras:**
- `gerarContasReceberDoPedido`: cria N parcelas onde a **última absorve os centavos** do arredondamento (divisão com `ROUND_DOWN` nas demais). Vencimentos: hoje + 30 dias por parcela.
- Conta a pagar sem `fornecedorId` é válida — permite lançamentos avulsos (aluguel, energia).
- Campos `codigoBanco` e `nomeBanco` ficam em `ContaReceber` e `ContaPagar` e são preenchidos no momento do pagamento.
- `GET /lancamentos`: une em memória todas as contas com status `PAGO` de ambas as tabelas, filtra e pagina com `PageImpl`. **Atenção:** não usa SQL — se o volume for grande, pode ser lento.
- `fluxoCaixa`: agrupa por mês usando `TreeMap<YearMonth>` (ordem cronológica garantida). Inclui contas de **todos** os status, não só pagas.
- Registrar pagamento em conta que não está `ABERTO` lança `BusinessException` (400).

---

### fiscal

**Entidades:** `NotaFiscal`, `ItemNotaFiscal`.

**TipoNF (enum):** `ENTRADA`, `SAIDA`.

**StatusNF (enum):** `ESCRITURADA_MANUAL`, `EMITIDA_MANUALMENTE`, `CANCELADA`.
- `podeCancelar()` retorna `true` para `ESCRITURADA_MANUAL` e `EMITIDA_MANUALMENTE`.

**Endpoints:**

| Método | Path | Descrição |
|--------|------|-----------|
| POST | `/api/v1/fiscal/entrada` | escriturar NF de entrada |
| POST | `/api/v1/fiscal/saida` | emitir NF de saída |
| GET | `/api/v1/fiscal` | listar com filtros opcionais |
| GET | `/api/v1/fiscal/{id}` | detalhe |
| PATCH | `/api/v1/fiscal/{id}/cancelar` | cancelar NF |
| GET | `/api/v1/fiscal/resumo-tributos?de=&ate=` | totais por mês |

**Regras:**

**NF de Entrada (`POST /fiscal/entrada`):**
- Duplicidade impedida: UNIQUE em `(tipo, numero, serie, fornecedor_id)`.
- Gera `ENTRADA_NF` no estoque para **cada item** da nota, na mesma transação.
- Gera uma `ContaPagar` para o fornecedor com vencimento em `dataEntradaSaida + 30 dias`.
- Campo `pedidoCompraId` opcional: se informado, vincula a NF ao pedido de compra e chama `ComprasService.marcarComoRecebido()` na mesma transação.
- Tributos (ICMS, IPI, PIS, COFINS) são calculados por item a partir das alíquotas informadas no payload. Se não informados, ficam zerados.

**NF de Saída (`POST /fiscal/saida`):**
- Exige que o pedido esteja em status `FATURADO`.
- Só pode existir **uma** NF de saída não-cancelada por pedido.
- Não movimenta estoque (já foi baixado no `confirmarPedido`).
- Não gera ContaReceber (já foi gerada no `faturarPedido`).

**Cancelamento (`PATCH /fiscal/{id}/cancelar`):**
- Cancela qualquer NF com status `ESCRITURADA_MANUAL` ou `EMITIDA_MANUALMENTE`.
- **Não estorna estoque automaticamente** — cancelamento fiscal é complexo e requer ajuste manual via `POST /estoque/movimentos` com `ENTRADA_MANUAL` para garantir rastreabilidade.
- Se a NF de entrada tinha `pedidoCompra` vinculado, chama `ComprasService.reverterRecebimento()` (RECEBIDO → CONFIRMADO).

**Filtros em `GET /fiscal`:**
- Usa `JpaSpecificationExecutor` + `FiscalSpecification` (Criteria API), **não** JPQL com `? IS NULL OR campo = ?`.
- Razão: PostgreSQL não consegue inferir o tipo do parâmetro nulo em JPQL com `LocalDate`, gerando `could not determine data type of parameter`. A Specification só adiciona o predicado quando o valor é não-nulo.

**Tributos:** todos os campos de tributo são opcionais no payload. Se não informados, ficam `0`. Não há tabela de tributos por produto — os valores são inseridos manualmente a cada NF.

---

### compras

**Entidades:** `PedidoCompra`, `ItemPedidoCompra`.

**StatusPedidoCompra (enum):** `RASCUNHO` → `CONFIRMADO` → `RECEBIDO` / `CANCELADO`.
- `podeCancelar()` retorna `true` para `RASCUNHO` e `CONFIRMADO`.

**Endpoints:**

| Método | Path | Descrição |
|--------|------|-----------|
| GET | `/api/v1/pedidos-compra` | listar com filtros |
| GET | `/api/v1/pedidos-compra/{id}` | detalhe |
| POST | `/api/v1/pedidos-compra` | criar (status inicial: RASCUNHO) |
| PUT | `/api/v1/pedidos-compra/{id}` | atualizar (somente RASCUNHO) |
| PATCH | `/api/v1/pedidos-compra/{id}/confirmar` | RASCUNHO → CONFIRMADO |
| PATCH | `/api/v1/pedidos-compra/{id}/cancelar` | RASCUNHO/CONFIRMADO → CANCELADO |

`GET /pedidos-compra?fornecedorId=&status=&semNfVinculada=true`

**Regras:**
- Numeração automática via `seq_pedido_compra_numero`: formato `CMP-AAAA-NNNNN`.
- `precoUnitario` por item é **obrigatório** no payload e vem do payload (preço negociado com fornecedor). Diferente de Vendas, onde o backend usa `produto.precoVenda`.
- `confirmarPedido`: não movimenta estoque nem financeiro. Apenas muda o status.
- O estoque e a ContaPagar só são gerados quando a **NF de entrada** for escriturada com `pedidoCompraId` preenchido.
- `atualizar`: substitui todos os itens (clear + addAll). Só permitido em RASCUNHO.
- Filtro `semNfVinculada=true`: pedidos CONFIRMADOS sem NF de entrada não-cancelada vinculada (query `NOT EXISTS` em JPQL com FQN do enum).
- `marcarComoRecebido` e `reverterRecebimento` são métodos internos chamados pelo `FiscalService`, não expostos via controller.
- Cancelar pedido RECEBIDO lança `BusinessException` (400).

---

## Integrações entre módulos

```
VendasService.confirmarPedido()
  └─→ EstoqueService.registrarMovimento(SAIDA_PEDIDO)   [mesma transação, por item]

VendasService.faturarPedido()
  └─→ FinanceiroService.gerarContasReceberDoPedido()    [mesma transação]

VendasService.cancelarPedido() [se estava CONFIRMADO]
  └─→ EstoqueService.registrarMovimento(ENTRADA_MANUAL) [mesma transação, por item]

FiscalService.escriturarEntrada()
  ├─→ EstoqueService.registrarMovimento(ENTRADA_NF)      [por item, mesma transação]
  ├─→ FinanceiroService.lancarContaPagar()               [mesma transação]
  └─→ ComprasService.marcarComoRecebido()                [se pedidoCompraId != null, mesma transação]

FiscalService.cancelarNF() [se NF de entrada com pedidoCompra != null]
  └─→ ComprasService.reverterRecebimento()               [mesma transação]
```

Todos os fluxos acima são `@Transactional` — qualquer erro reverte tudo.

---

## Convenções do projeto

**IDs:** todos UUID, gerados pelo banco (`gen_random_uuid()` ou `uuid_generate_v4()`). No Java, `@GeneratedValue(strategy = GenerationType.UUID)`.

**BaseEntity:** `@MappedSuperclass` com `id UUID`, `criadoEm LocalDateTime` (`updatable=false`), `atualizadoEm LocalDateTime`. Populados via `@EnableJpaAuditing` + `AuditingEntityListener`.

**Numeração de documentos:**
- Pedidos de venda: `PED-AAAA-NNNNN` via `seq_pedido_numero`
- Pedidos de compra: `CMP-AAAA-NNNNN` via `seq_pedido_compra_numero`

**Campos de texto:** `cpfCnpj` armazenado **com máscara** (ex: `11.222.333/0001-44`). Não há normalização — o frontend envia como mostrado. Telefone/celular/CEP sem validação de formato definida no backend.

**JSON:** `jackson.default-property-inclusion: non_null` — campos `null` não aparecem na resposta.

**Paginação:** todos os endpoints de listagem retornam `Page<T>` do Spring Data. Parâmetros: `page`, `size`, `sort`. Padrões variam por endpoint (ver controllers).

**GlobalExceptionHandler:**
- `NotFoundException` → 400 `"Not Found"` ... espera: na verdade `HttpStatus.NOT_FOUND` = 404. Confira em `GlobalExceptionHandler.java:35`.
- `BusinessException` → 400 `"Bad Request"`
- `MethodArgumentNotValidException` → 400 com `fieldErrors: {campo: mensagem}`
- `Exception` genérica → 500 com mensagem genérica (sem stack trace exposto)

**CORS:** configurado para `http://localhost:5173` e `http://localhost:3000` com `allowCredentials: true`.

**Filtros com múltiplos parâmetros opcionais:**
- **NÃO usar** JPQL com `(:param IS NULL OR campo = :param)` quando o parâmetro for `LocalDate`, `LocalDateTime` ou enum — PostgreSQL lança `could not determine data type of parameter $N` com parâmetro nulo.
- **Usar** `JpaSpecificationExecutor` + classe `Specification` (Criteria API) que só adiciona o predicado quando o valor é não-nulo. Ver `FiscalSpecification.java` como referência.
- Para parâmetros simples como UUID e String, o JPQL com IS NULL pode funcionar, mas a Specification é mais segura.

**JPQL com enums em subqueries:** usar FQN completo do enum (ex: `com.madeireira.erp.modules.vendas.entity.StatusPedido.FATURADO`) para evitar ambiguidade do Hibernate.

---

## Segurança

**Rotas públicas** (do `SecurityConfig.PUBLIC_ROUTES`):
```
/api/v1/auth/**
/swagger-ui/**
/swagger-ui.html
/api-docs/**
/actuator/health
```
Todas as demais rotas requerem `Authorization: Bearer <token>`.

**Fluxo JWT:**
1. `POST /auth/login` retorna o token
2. Cada requisição passa pelo `JwtAuthenticationFilter` (executa antes do `UsernamePasswordAuthenticationFilter`)
3. Se não há header `Authorization: Bearer ...` → passa adiante (rotas públicas são liberadas; protegidas recebem 401 via `AuthenticationEntryPoint`)
4. Se há token mas é inválido/expirado → `sendUnauthorized()` retorna 401 com body `{"error":"Token inválido ou expirado","status":401}` — **interrompe a cadeia, não passa adiante**
5. Se token válido → popula `SecurityContextHolder` e passa adiante

**Senha:** BCrypt (`BCryptPasswordEncoder`).

**Sessão:** stateless (`SessionCreationPolicy.STATELESS`).

---

## Testes

**Como rodar:**
```bash
./mvnw test
```

**Configuração de teste** (`src/test/resources/application-test.yml`):
- Banco: H2 in-memory com `MODE=PostgreSQL`
- Flyway: desabilitado
- `ddl-auto: create-drop` (Hibernate cria as tabelas pela JPA)
- `src/test/resources/import.sql`: cria as sequences `seq_pedido_numero` e `seq_pedido_compra_numero` que o H2 não cria automaticamente (Flyway desabilitado)

**Testes existentes:**

| Arquivo | Cobertura |
|---------|-----------|
| `ClienteServiceTest` | criar (válido), criar duplicado, buscar inexistente, atualizar, inativar |
| `ProdutoServiceTest` | criar, código duplicado, buscar inexistente, atualizar, inativar, validações Bean Validation |
| `ComprasServiceTest` | criar (válido, fornecedor inexistente, produto inexistente), confirmar, confirmar fora de RASCUNHO, cancelar RASCUNHO, cancelar RECEBIDO, ciclo marcarComoRecebido + reverterRecebimento |

Total: **20 testes, 0 falhas**.

Validação de integração real (contra banco PostgreSQL) foi feita manualmente via `curl` em cada sprint.

---

## Pendências e próximos passos

**Limitações conhecidas:**
- **Tributos por produto não configurados.** Não há tabela de alíquotas por produto/NCM. Os valores de ICMS, IPI, PIS, COFINS são informados manualmente em cada NF.
- **Sem transmissão SEFAZ.** Toda escrituração é manual (`StatusNF.ESCRITURADA_MANUAL`). Não há integração com webservice da SEFAZ, geração de XML ou DANFE.
- **Cancelar NF de entrada não estorna estoque por design.** O cancelamento fiscal não gera movimento de estoque automaticamente. O operador deve registrar um `ENTRADA_MANUAL` manualmente para restaurar o saldo, mantendo rastreabilidade contábil.
- **`GET /financeiro/lancamentos` faz union em memória.** Carrega todas as contas PAGAS de ambas as tabelas antes de filtrar e paginar via `PageImpl`. Pode ser lento com grande volume.
- **`audit_log` criado na V1 mas não é populado.** A tabela existe no banco mas nenhum service grava nela.

**Planejado (não implementado):**
- Módulo de Relatórios — agregações por vendas, produtos, financeiro e compras
- Tabela de dados da empresa — nome e CNPJ hoje estão hardcoded no frontend para geração de PDF
- Módulo de tributos por produto/NCM para preencher NFs automaticamente
