package com.lanchonete.dto;

import com.lanchonete.model.Produto;

import java.math.BigDecimal;

public record ProdutoResponseDTO(Long id, String nome, BigDecimal preco) {

    public static ProdutoResponseDTO fromEntity(Produto p) {
        return new ProdutoResponseDTO(p.getId(), p.getNome(), p.getPreco());
    }
}
