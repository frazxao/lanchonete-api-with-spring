package com.lanchonete.exception;

/** Violação de regra de negócio -> HTTP 409. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
