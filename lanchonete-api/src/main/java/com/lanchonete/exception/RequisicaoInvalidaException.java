package com.lanchonete.exception;

/** Parâmetros incoerentes enviados pelo cliente (ex.: data inicial maior que a final) -> HTTP 400. */
public class RequisicaoInvalidaException extends RuntimeException {

    public RequisicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
