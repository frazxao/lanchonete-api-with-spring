package com.lanchonete.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/** Formato padrão de erro devolvido por toda a API. */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        List<String> detalhes) {
}
