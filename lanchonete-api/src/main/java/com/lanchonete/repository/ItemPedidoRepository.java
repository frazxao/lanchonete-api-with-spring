package com.lanchonete.repository;

import com.lanchonete.model.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    // Impede excluir produto que já foi vendido
    boolean existsByProdutoId(Long produtoId);
}
