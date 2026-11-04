package com.lanchonete.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemPedidoRequestDTO(
        @NotNull(message = "O produtoId é obrigatório")
        Long produtoId,

        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        @Max(value = 100, message = "A quantidade máxima por item é 100")
        Integer quantidade) {
}
