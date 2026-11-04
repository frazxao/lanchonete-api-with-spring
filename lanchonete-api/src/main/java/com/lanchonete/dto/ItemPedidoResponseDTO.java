package com.lanchonete.dto;

import com.lanchonete.model.ItemPedido;

import java.math.BigDecimal;

public record ItemPedidoResponseDTO(
        Long produtoId,
        String produtoNome,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal) {

    public static ItemPedidoResponseDTO fromEntity(ItemPedido i) {
        return new ItemPedidoResponseDTO(
                i.getProduto().getId(),
                i.getProduto().getNome(),
                i.getQuantidade(),
                i.getPrecoUnitario(),
                i.getSubtotal());
    }
}
