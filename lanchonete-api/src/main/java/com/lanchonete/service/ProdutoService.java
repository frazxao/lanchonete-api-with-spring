package com.lanchonete.service;

import com.lanchonete.dto.ProdutoRequestDTO;
import com.lanchonete.dto.ProdutoResponseDTO;
import com.lanchonete.exception.RecursoNaoEncontradoException;
import com.lanchonete.exception.RegraNegocioException;
import com.lanchonete.model.Produto;
import com.lanchonete.repository.ItemPedidoRepository;
import com.lanchonete.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    public ProdutoService(ProdutoRepository produtoRepository, ItemPedidoRepository itemPedidoRepository) {
        this.produtoRepository = produtoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    @Transactional
    public ProdutoResponseDTO salvar(ProdutoRequestDTO dto) {
        Produto produto = new Produto(dto.nome().trim(), dto.preco());
        return ProdutoResponseDTO.fromEntity(produtoRepository.save(produto));
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> listar(String nome) {
        List<Produto> produtos = (nome == null || nome.isBlank())
                ? produtoRepository.findAll()
                : produtoRepository.findByNomeContainingIgnoreCase(nome.trim());
        return produtos.stream().map(ProdutoResponseDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponseDTO buscarPorId(Long id) {
        return ProdutoResponseDTO.fromEntity(buscarEntidade(id));
    }

    @Transactional
    public ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO dto) {
        Produto produto = buscarEntidade(id);
        produto.setNome(dto.nome().trim());
        produto.setPreco(dto.preco());
        return ProdutoResponseDTO.fromEntity(produtoRepository.save(produto));
    }

    @Transactional
    public void excluir(Long id) {
        Produto produto = buscarEntidade(id);

        if (itemPedidoRepository.existsByProdutoId(id)) {
            throw new RegraNegocioException("Não é possível excluir o produto " + id + ": ele já consta em pedidos");
        }

        produtoRepository.delete(produto);
    }

    private Produto buscarEntidade(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado: id " + id));
    }
}
