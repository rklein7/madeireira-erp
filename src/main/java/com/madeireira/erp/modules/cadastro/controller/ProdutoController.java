package com.madeireira.erp.modules.cadastro.controller;

import com.madeireira.erp.modules.cadastro.dto.ProdutoDTO;
import com.madeireira.erp.modules.cadastro.service.ProdutoService;
import com.madeireira.erp.shared.exception.GlobalExceptionHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/produtos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Produtos", description = "Gestão de produtos da madeireira")
public class ProdutoController {

    private final ProdutoService produtoService;

    @GetMapping
    @Operation(
        summary = "Listar produtos",
        description = "Retorna lista paginada de produtos ativos. " +
                      "Use `?busca=termo` para filtrar por código ou descrição (case-insensitive). " +
                      "Paginação via `?page=0&size=20&sort=descricao,asc`."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<Page<ProdutoDTO.Resumo>> listar(
            @RequestParam(required = false) String busca,
            @PageableDefault(size = 20, sort = "descricao") Pageable pageable) {
        return ResponseEntity.ok(produtoService.listar(busca, pageable));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar produto por ID",
        description = "Retorna todos os dados do produto, incluindo dimensões, estoque e categoria. " +
                      "O campo `abaixoDoMinimo` indica se o estoque atual está abaixo do mínimo configurado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Produto encontrado"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<ProdutoDTO.Response> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    @PostMapping
    @Operation(
        summary = "Criar produto",
        description = "Cadastra um novo produto. O `codigo` deve ser único no sistema. " +
                      "O `estoqueAtual` começa em zero e é atualizado via movimentações de estoque."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Produto criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou código duplicado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<ProdutoDTO.Response> criar(@Valid @RequestBody ProdutoDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoService.criar(request));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar produto",
        description = "Atualiza os dados cadastrais do produto. " +
                      "O código pode ser alterado desde que o novo código não exista em outro produto. " +
                      "O `estoqueAtual` não é alterado por este endpoint."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou código já usado por outro produto",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<ProdutoDTO.Response> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ProdutoDTO.Request request) {
        return ResponseEntity.ok(produtoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Inativar produto",
        description = "Inativação lógica: o produto é marcado como `ativo = false` e não aparece " +
                      "mais nas listagens, mas permanece no banco para preservar o histórico de pedidos. " +
                      "Não é possível reativar via API."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Produto inativado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<Void> inativar(@PathVariable UUID id) {
        produtoService.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/alertas/estoque-minimo")
    @Operation(
        summary = "Produtos abaixo do estoque mínimo",
        description = "Lista todos os produtos ativos cujo `estoqueAtual` está abaixo do `estoqueMinimo` configurado. " +
                      "Use para gerar ordens de compra de reposição."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de alertas retornada (pode ser vazia)"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<List<ProdutoDTO.Resumo>> alertasEstoqueMinimo() {
        return ResponseEntity.ok(produtoService.alertasEstoqueMinimo());
    }
}
