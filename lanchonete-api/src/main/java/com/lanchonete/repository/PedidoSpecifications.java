package com.lanchonete.repository;

import com.lanchonete.model.Pedido;
import com.lanchonete.model.StatusPedido;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Filtros opcionais de Pedido. Uma Specification que retorna null é ignorada na composição,
 * então cada filtro só entra na query se o parâmetro foi informado.
 */
public final class PedidoSpecifications {

    private PedidoSpecifications() {
    }

    public static Specification<Pedido> comStatus(StatusPedido status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    /** Pedidos criados a partir das 00:00 da data informada (inclusive). */
    public static Specification<Pedido> criadoEmOuApos(LocalDate inicio) {
        return (root, query, cb) -> inicio == null ? null
                : cb.greaterThanOrEqualTo(root.<LocalDateTime>get("dataCriacao"), inicio.atStartOfDay());
    }

    /** Pedidos criados até o fim do dia informado (inclusive). */
    public static Specification<Pedido> criadoAte(LocalDate fim) {
        return (root, query, cb) -> fim == null ? null
                : cb.lessThan(root.<LocalDateTime>get("dataCriacao"), fim.plusDays(1).atStartOfDay());
    }
}
