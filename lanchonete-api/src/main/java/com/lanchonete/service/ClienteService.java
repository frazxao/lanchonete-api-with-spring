package com.lanchonete.service;

import com.lanchonete.dto.ClienteRequestDTO;
import com.lanchonete.dto.ClienteResponseDTO;
import com.lanchonete.exception.RecursoNaoEncontradoException;
import com.lanchonete.exception.RegraNegocioException;
import com.lanchonete.model.Cliente;
import com.lanchonete.repository.ClienteRepository;
import com.lanchonete.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;

    public ClienteService(ClienteRepository clienteRepository, PedidoRepository pedidoRepository) {
        this.clienteRepository = clienteRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional
    public ClienteResponseDTO salvar(ClienteRequestDTO dto) {
        String email = normalizarEmail(dto.email());

        // Regra 1: e-mail duplicado
        if (clienteRepository.existsByEmail(email)) {
            throw new RegraNegocioException("Já existe um cliente cadastrado com o e-mail " + email);
        }

        Cliente cliente = new Cliente(dto.nome().trim(), email, dto.telefone());
        return ClienteResponseDTO.fromEntity(clienteRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listar(String nome) {
        // Filtro 1: parte do nome, ignorando maiúsculas/minúsculas
        List<Cliente> clientes = (nome == null || nome.isBlank())
                ? clienteRepository.findAll()
                : clienteRepository.findByNomeContainingIgnoreCase(nome.trim());
        return clientes.stream().map(ClienteResponseDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorId(Long id) {
        return ClienteResponseDTO.fromEntity(buscarEntidade(id));
    }

    @Transactional
    public ClienteResponseDTO atualizar(Long id, ClienteRequestDTO dto) {
        Cliente cliente = buscarEntidade(id);
        String email = normalizarEmail(dto.email());

        // Regra 1 também na atualização, ignorando o próprio registro
        if (clienteRepository.existsByEmailAndIdNot(email, id)) {
            throw new RegraNegocioException("Já existe outro cliente cadastrado com o e-mail " + email);
        }

        cliente.setNome(dto.nome().trim());
        cliente.setEmail(email);
        cliente.setTelefone(dto.telefone());
        // saveAndFlush: se a constraint unique for violada, o erro ocorre aqui e não só no commit
        return ClienteResponseDTO.fromEntity(clienteRepository.saveAndFlush(cliente));
    }

    @Transactional
    public void excluir(Long id) {
        Cliente cliente = buscarEntidade(id);

        // Regra 2: cliente com pedidos não pode ser excluído
        if (pedidoRepository.existsByClienteId(id)) {
            throw new RegraNegocioException("Não é possível excluir o cliente " + id + ": existem pedidos vinculados a ele");
        }

        clienteRepository.delete(cliente);
    }

    private Cliente buscarEntidade(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado: id " + id));
    }

    // Normaliza para que "Ana@x.com" e "ana@x.com" sejam tratados como o mesmo e-mail
    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }
}
