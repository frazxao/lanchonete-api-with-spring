package com.lanchonete.controller;

import com.lanchonete.dto.PedidoRequestDTO;
import com.lanchonete.dto.PedidoResponseDTO;
import com.lanchonete.dto.StatusPedidoRequestDTO;
import com.lanchonete.model.StatusPedido;
import com.lanchonete.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/pedidos")
@Tag(name = "Pedidos", description = "Criação e acompanhamento de pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @Operation(summary = "Cria um pedido",
            description = "O preço de cada item é copiado do produto no momento da criação e o valorTotal é calculado automaticamente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Cliente ou produto não encontrado"),
            @ApiResponse(responseCode = "409", description = "Produto repetido na lista de itens")})
    public ResponseEntity<PedidoResponseDTO> criar(@Valid @RequestBody PedidoRequestDTO dto) {
        PedidoResponseDTO criado = pedidoService.salvar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping
    @Operation(summary = "Lista pedidos", description = "Filtros opcionais e combináveis: status e intervalo de datas de criação (yyyy-MM-dd, inclusivo).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de pedidos"),
            @ApiResponse(responseCode = "400", description = "Parâmetro inválido ou dataInicio > dataFim")})
    public ResponseEntity<List<PedidoResponseDTO>> listar(
            @Parameter(description = "PENDENTE, PREPARANDO, ENTREGUE ou CANCELADO")
            @RequestParam(required = false) StatusPedido status,
            @Parameter(description = "Data inicial (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data final (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        return ResponseEntity.ok(pedidoService.listar(status, dataInicio, dataFim));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca pedido por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")})
    public ResponseEntity<PedidoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Altera cliente e itens de um pedido", description = "Permitido apenas para pedidos PENDENTES.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Pedido, cliente ou produto não encontrado"),
            @ApiResponse(responseCode = "409", description = "Pedido não está PENDENTE")})
    public ResponseEntity<PedidoResponseDTO> atualizar(@PathVariable Long id,
                                                       @Valid @RequestBody PedidoRequestDTO dto) {
        return ResponseEntity.ok(pedidoService.atualizar(id, dto));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Altera o status do pedido",
            description = "PENDENTE -> PREPARANDO | CANCELADO; PREPARANDO -> ENTREGUE | CANCELADO. ENTREGUE e CANCELADO são finais.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado"),
            @ApiResponse(responseCode = "400", description = "Status ausente ou inexistente"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "409", description = "Transição de status inválida")})
    public ResponseEntity<PedidoResponseDTO> atualizarStatus(@PathVariable Long id,
                                                             @Valid @RequestBody StatusPedidoRequestDTO dto) {
        return ResponseEntity.ok(pedidoService.atualizarStatus(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um pedido", description = "Apenas pedidos CANCELADOS podem ser excluídos.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pedido excluído"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "409", description = "Pedido não está CANCELADO")})
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        pedidoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
