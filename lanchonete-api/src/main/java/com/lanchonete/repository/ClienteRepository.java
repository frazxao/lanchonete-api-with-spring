package com.lanchonete.repository;

import com.lanchonete.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Regra 1: cadastro com e-mail duplicado
    boolean existsByEmail(String email);

    // Regra 1 no update: ignora o próprio registro
    boolean existsByEmailAndIdNot(String email, Long id);

    // Filtro 1: busca por parte do nome, sem diferenciar maiúsculas/minúsculas
    List<Cliente> findByNomeContainingIgnoreCase(String nome);
}
