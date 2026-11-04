package com.lanchonete.dto;

import com.lanchonete.model.Cliente;

public record ClienteResponseDTO(Long id, String nome, String email, String telefone) {

    public static ClienteResponseDTO fromEntity(Cliente c) {
        return new ClienteResponseDTO(c.getId(), c.getNome(), c.getEmail(), c.getTelefone());
    }
}
