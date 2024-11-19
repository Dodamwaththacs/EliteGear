package teamnova.elite_gear.service;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import teamnova.elite_gear.domain.Order;
import teamnova.elite_gear.domain.OrderItem;
import teamnova.elite_gear.domain.Product;
import teamnova.elite_gear.model.OrderItemDTO;
import teamnova.elite_gear.repos.OrderItemRepository;
import teamnova.elite_gear.repos.OrderRepository;
import teamnova.elite_gear.repos.ProductRepository;
import teamnova.elite_gear.util.NotFoundException;


@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderItemService(final OrderItemRepository orderItemRepository,
                            final OrderRepository orderRepository, final ProductRepository productRepository) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public List<OrderItemDTO> findAll() {
        final List<OrderItem> orderItems = orderItemRepository.findAll(Sort.by("orderItemID"));
        return orderItems.stream()
                .map(orderItem -> mapToDTO(orderItem, new OrderItemDTO()))
                .toList();
    }

    public OrderItemDTO get(final UUID orderItemID) {
        return orderItemRepository.findById(orderItemID)
                .map(orderItem -> mapToDTO(orderItem, new OrderItemDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public UUID create(final OrderItemDTO orderItemDTO) {
        final OrderItem orderItem = new OrderItem();
        mapToEntity(orderItemDTO, orderItem);
        return orderItemRepository.save(orderItem).getOrderItemID();
    }

    public void update(final UUID orderItemID, final OrderItemDTO orderItemDTO) {
        final OrderItem orderItem = orderItemRepository.findById(orderItemID)
                .orElseThrow(NotFoundException::new);
        mapToEntity(orderItemDTO, orderItem);
        orderItemRepository.save(orderItem);
    }

    public void delete(final UUID orderItemID) {
        orderItemRepository.deleteById(orderItemID);
    }

    private OrderItemDTO mapToDTO(final OrderItem orderItem, final OrderItemDTO orderItemDTO) {
        orderItemDTO.setOrderItemID(orderItem.getOrderItemID());
        orderItemDTO.setQuantity(orderItem.getQuantity());
        orderItemDTO.setPrice(orderItem.getPrice());
        orderItemDTO.setOrder(orderItem.getOrder() == null ? null : orderItem.getOrder().getOrderID());
        orderItemDTO.setProduct(orderItem.getProduct() == null ? null : orderItem.getProduct().getProductID());
        return orderItemDTO;
    }

    private OrderItem mapToEntity(final OrderItemDTO orderItemDTO, final OrderItem orderItem) {
        orderItem.setQuantity(orderItemDTO.getQuantity());
        orderItem.setPrice(orderItemDTO.getPrice());
        final Order order = orderItemDTO.getOrder() == null ? null : orderRepository.findById(orderItemDTO.getOrder())
                .orElseThrow(() -> new NotFoundException("order not found"));
        orderItem.setOrder(order);
        final Product product = orderItemDTO.getProduct() == null ? null : productRepository.findById(orderItemDTO.getProduct())
                .orElseThrow(() -> new NotFoundException("product not found"));
        orderItem.setProduct(product);
        return orderItem;
    }

}