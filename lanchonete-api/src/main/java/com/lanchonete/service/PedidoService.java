package com.lanchonete.service;

import com.lanchonete.dto.ItemPedidoRequestDTO;
import com.lanchonete.dto.PedidoRequestDTO;
import com.lanchonete.dto.PedidoResponseDTO;
import com.lanchonete.dto.StatusPedidoRequestDTO;
import com.lanchonete.exception.RecursoNaoEncontradoException;
import com.lanchonete.exception.RegraNegocioException;
import com.lanchonete.exception.RequisicaoInvalidaException;
import com.lanchonete.model.Cliente;
import com.lanchonete.model.ItemPedido;
import com.lanchonete.model.Pedido;
import com.lanchonete.model.Produto;
import com.lanchonete.model.StatusPedido;
import com.lanchonete.repository.ClienteRepository;
import com.lanchonete.repository.PedidoRepository;
import com.lanchonete.repository.PedidoSpecifications;
import com.lanchonete.repository.ProdutoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.lanchonete.model.StatusPedido.*;

@Service
public class PedidoService {

    /** Máquina de estados (Regra 3). Estados terminais têm conjunto vazio. */
    private static final Map<StatusPedido, Set<StatusPedido>> TRANSICOES_PERMITIDAS = Map.of(
            PENDENTE, EnumSet.of(PREPARANDO, CANCELADO),
            PREPARANDO, EnumSet.of(ENTREGUE, CANCELADO),
            ENTREGUE, EnumSet.noneOf(StatusPedido.class),
            CANCELADO, EnumSet.noneOf(StatusPedido.class));

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         ClienteRepository clienteRepository,
                         ProdutoRepository produtoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public PedidoResponseDTO salvar(PedidoRequestDTO dto) {
        Cliente cliente = buscarCliente(dto.clienteId());

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setStatus(PENDENTE);
        montarItensEValorTotal(pedido, dto.itens());

        return PedidoResponseDTO.fromEntity(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listar(StatusPedido status, LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new RequisicaoInvalidaException("A dataInicio não pode ser posterior à dataFim");
        }

        // Filtro 2: status e/ou intervalo de datas (todos opcionais)
        Specification<Pedido> spec = Specification
                .where(PedidoSpecifications.comStatus(status))
                .and(PedidoSpecifications.criadoEmOuApos(dataInicio))
                .and(PedidoSpecifications.criadoAte(dataFim));

        return pedidoRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "dataCriacao"))
                .stream().map(PedidoResponseDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPorId(Long id) {
        return PedidoResponseDTO.fromEntity(buscarEntidade(id));
    }

    /** Só pedidos PENDENTES podem ter cliente/itens alterados. */
    @Transactional
    public PedidoResponseDTO atualizar(Long id, PedidoRequestDTO dto) {
        Pedido pedido = buscarEntidade(id);

        if (pedido.getStatus() != PENDENTE) {
            throw new RegraNegocioException("Somente pedidos PENDENTES podem ser alterados. Status atual: " + pedido.getStatus());
        }

        pedido.setCliente(buscarCliente(dto.clienteId()));
        pedido.getItens().clear(); // orphanRemoval apaga os itens antigos
        montarItensEValorTotal(pedido, dto.itens());

        return PedidoResponseDTO.fromEntity(pedidoRepository.save(pedido));
    }

    /** Regra 3: máquina de estados. */
    @Transactional
    public PedidoResponseDTO atualizarStatus(Long id, StatusPedidoRequestDTO dto) {
        Pedido pedido = buscarEntidade(id);
        StatusPedido atual = pedido.getStatus();
        StatusPedido novo = dto.status();
        Set<StatusPedido> permitidos = TRANSICOES_PERMITIDAS.get(atual);

        if (permitidos.isEmpty()) {
            throw new RegraNegocioException(
                    "O pedido já está " + atual + " e não pode mais ter o status alterado");
        }
        if (!permitidos.contains(novo)) {
            throw new RegraNegocioException(
                    "Transição inválida: " + atual + " -> " + novo + ". A partir de " + atual + " só é possível ir para " + permitidos);
        }

        pedido.setStatus(novo);
        return PedidoResponseDTO.fromEntity(pedidoRepository.save(pedido));
    }

    /** Só pedidos CANCELADOS podem ser excluídos; os demais fazem parte do histórico. */
    @Transactional
    public void excluir(Long id) {
        Pedido pedido = buscarEntidade(id);

        if (pedido.getStatus() != CANCELADO) {
            throw new RegraNegocioException("Somente pedidos CANCELADOS podem ser excluídos. Status atual: " + pedido.getStatus());
        }

        pedidoRepository.delete(pedido);
    }

    // ------------------------------------------------------------------

    /**
     * Para cada item: busca o produto, "congela" o preço atual em precoUnitario,
     * vincula ao pedido e acumula o total.
     */
    private void montarItensEValorTotal(Pedido pedido, List<ItemPedidoRequestDTO> itensDto) {
        Set<Long> produtosJaAdicionados = new HashSet<>();
        BigDecimal total = BigDecimal.ZERO;

        for (ItemPedidoRequestDTO itemDto : itensDto) {
            if (!produtosJaAdicionados.add(itemDto.produtoId())) {
                throw new RegraNegocioException(
                        "O produto " + itemDto.produtoId() + " aparece mais de uma vez no pedido. Some as quantidades em um único item");
            }

            Produto produto = produtoRepository.findById(itemDto.produtoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado: id " + itemDto.produtoId()));

            ItemPedido item = new ItemPedido();
            item.setProduto(produto);
            item.setQuantidade(itemDto.quantidade());
            item.setPrecoUnitario(produto.getPreco());
            pedido.adicionarItem(item);

            total = total.add(item.getSubtotal());
        }

        pedido.setValorTotal(total);
    }

    private Cliente buscarCliente(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado: id " + clienteId));
    }

    private Pedido buscarEntidade(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado: id " + id));
    }
}
