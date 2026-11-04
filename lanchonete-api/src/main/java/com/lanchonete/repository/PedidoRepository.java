package com.lanchonete.repository;

import com.lanchonete.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JpaSpecificationExecutor permite montar o Filtro 2 (status e/ou intervalo de datas)
 * com parâmetros opcionais no Service, sem JPQL com ":param IS NULL"
 * (que quebra no PostgreSQL com datas nulas).
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long>, JpaSpecificationExecutor<Pedido> {

    // Regra 2: cliente possui pedidos?
    boolean existsByClienteId(Long clienteId);

    List<Pedido> findByClienteId(Long clienteId);
}
