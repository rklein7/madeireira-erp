# Relatório de Testes — ERP Madeireira (Sprint 1: Cadastros)

**Data do teste:** ___/___/______
**Testador:** Rafael Augusto Klein
**Ambiente:** Local (PostgreSQL nativo, porta 5432)
**Versão da API:** Spring Boot 3.3.4 / Java 21

---

## 1. Resumo executivo

| Métrica | Valor |
|---|---|
| Total de cenários testados | 24 |
| Aprovados | ___ |
| Reprovados | ___ |
| Taxa de sucesso | ___% |
| Tempo médio de resposta | ___ ms |

---

## 2. Pré-requisitos verificados

| Item | Status |
|---|---|
| Aplicação rodando em `localhost:8080` | ☐ |
| Banco `madeireira_erp` acessível | ☐ |
| Migrations do Flyway aplicadas (8 tabelas) | ☐ |
| Swagger acessível em `/swagger-ui.html` | ☐ |

---

## 3. Módulo: Produtos

| # | Cenário | Endpoint | Resultado esperado | Status | Tempo (ms) | Observações |
|---|---|---|---|---|---|---|
| 1 | Criar produto válido | `POST /produtos` | 201 + ID gerado | ☐ Pass ☐ Fail | | |
| 2 | Criar com código duplicado | `POST /produtos` | 400 + mensagem de erro | ☐ Pass ☐ Fail | | |
| 3 | Criar sem preço (validação) | `POST /produtos` | 400 + fieldErrors.precoVenda | ☐ Pass ☐ Fail | | |
| 4 | Listar produtos paginado | `GET /produtos` | 200 + estrutura Page | ☐ Pass ☐ Fail | | |
| 5 | Buscar por termo (ex: "pinus") | `GET /produtos?busca=` | 200 + resultado filtrado | ☐ Pass ☐ Fail | | |
| 6 | Buscar por ID existente | `GET /produtos/{id}` | 200 + dados completos | ☐ Pass ☐ Fail | | |
| 7 | Buscar por ID inexistente | `GET /produtos/{id}` | 404 | ☐ Pass ☐ Fail | | |
| 8 | Atualizar produto | `PUT /produtos/{id}` | 200 + dados atualizados | ☐ Pass ☐ Fail | | |
| 9 | Alertas de estoque mínimo | `GET /produtos/alertas/estoque-minimo` | 200 + array | ☐ Pass ☐ Fail | | |
| 10 | Inativar produto | `DELETE /produtos/{id}` | 204 | ☐ Pass ☐ Fail | | |
| 11 | Confirmar que inativo some da busca | `GET /produtos?busca=` | 200 + não aparece | ☐ Pass ☐ Fail | | |

**Bugs encontrados no módulo Produtos:**
-

---

## 4. Módulo: Clientes

| # | Cenário | Endpoint | Resultado esperado | Status | Tempo (ms) | Observações |
|---|---|---|---|---|---|---|
| 1 | Criar cliente PJ válido | `POST /clientes` | 201 + ID gerado | ☐ Pass ☐ Fail | | |
| 2 | Criar com CNPJ duplicado | `POST /clientes` | 400 | ☐ Pass ☐ Fail | | |
| 3 | Criar com e-mail inválido | `POST /clientes` | 400 + fieldErrors.email | ☐ Pass ☐ Fail | | |
| 4 | Listar clientes paginado | `GET /clientes` | 200 + estrutura Page | ☐ Pass ☐ Fail | | |
| 5 | Buscar por nome/razão social | `GET /clientes?busca=` | 200 + resultado filtrado | ☐ Pass ☐ Fail | | |
| 6 | Buscar por ID existente | `GET /clientes/{id}` | 200 + dados completos | ☐ Pass ☐ Fail | | |
| 7 | Atualizar cliente | `PUT /clientes/{id}` | 200 + dados atualizados | ☐ Pass ☐ Fail | | |
| 8 | Inativar cliente | `DELETE /clientes/{id}` | 204 | ☐ Pass ☐ Fail | | |

**Bugs encontrados no módulo Clientes:**
-

---

## 5. Módulo: Fornecedores

| # | Cenário | Endpoint | Resultado esperado | Status | Tempo (ms) | Observações |
|---|---|---|---|---|---|---|
| 1 | Criar fornecedor válido | `POST /fornecedores` | 201 + ID gerado | ☐ Pass ☐ Fail | | |
| 2 | Listar fornecedores | `GET /fornecedores` | 200 + estrutura Page | ☐ Pass ☐ Fail | | |
| 3 | Buscar por ID | `GET /fornecedores/{id}` | 200 + dados completos | ☐ Pass ☐ Fail | | |
| 4 | Atualizar fornecedor | `PUT /fornecedores/{id}` | 200 + dados atualizados | ☐ Pass ☐ Fail | | |
| 5 | Inativar fornecedor | `DELETE /fornecedores/{id}` | 204 | ☐ Pass ☐ Fail | | |

**Bugs encontrados no módulo Fornecedores:**
-

---

## 6. Validações de regra de negócio

| Regra | Verificado em | Status |
|---|---|---|
| Código de produto é único | Cenário Produtos #2 | ☐ Pass ☐ Fail |
| CPF/CNPJ de cliente é único | Cenário Clientes #2 | ☐ Pass ☐ Fail |
| Soft delete (inativar, não apagar) | Cenários #10/#11 Produtos | ☐ Pass ☐ Fail |
| Campos de auditoria preenchidos automaticamente | Verificar `criadoEm`/`atualizadoEm` em qualquer resposta | ☐ Pass ☐ Fail |
| Unidade de medida retorna símbolo correto | Cenário Produtos #1 (m³) | ☐ Pass ☐ Fail |

---

## 7. Problemas/observações gerais

| Severidade | Descrição | Endpoint afetado | Status |
|---|---|---|---|
| | | | ☐ Aberto ☐ Corrigido |

---

## 8. Próximos passos

- [ ] Corrigir bugs identificados acima
- [ ] Adicionar testes de fornecedor com documento duplicado (faltou na coleção atual)
- [ ] Testar paginação com mais de 20 registros
- [ ] Validar comportamento de `tabelaPreco` vinculada a cliente
- [ ] Iniciar planejamento do Sprint 2 (Estoque)

---

**Conclusão geral:**
_(escrever aqui se o Sprint 1 está pronto para avançar pro frontend / Sprint 2, ou se precisa de retrabalho)_
