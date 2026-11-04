package com.lanchonete.dto;

import com.lanchonete.model.StatusPedido;
import jakarta.validation.constraints.NotNull;

public record StatusPedidoRequestDTO(
        @NotNull(message = "O novo status é obrigatório")
        StatusPedido status) {
}
