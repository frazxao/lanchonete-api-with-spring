package com.lanchonete.dto;

import com.lanchonete.model.Pedido;
import com.lanchonete.model.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponseDTO(
        Long id,
        Long clienteId,
        String clienteNome,
        StatusPedido status,
        LocalDateTime dataCriacao,
        List<ItemPedidoResponseDTO> itens,
        BigDecimal valorTotal) {

    public static PedidoResponseDTO fromEntity(Pedido p) {
        return new PedidoResponseDTO(
                p.getId(),
                p.getCliente().getId(),
                p.getCliente().getNome(),
                p.getStatus(),
                p.getDataCriacao(),
                p.getItens().stream().map(ItemPedidoResponseDTO::fromEntity).toList(),
                p.getValorTotal());
    }
}
