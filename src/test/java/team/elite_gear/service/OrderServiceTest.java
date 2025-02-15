package team.elite_gear.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import teamnova.elite_gear.domain.*;
import teamnova.elite_gear.model.CreateOrderDTO;
import teamnova.elite_gear.model.CreateOrderItemDTO;
import teamnova.elite_gear.model.OrderDTO;
import teamnova.elite_gear.model.OrderItemDTO;
import teamnova.elite_gear.repos.*;
import teamnova.elite_gear.service.ConvertTo_orderDTO_orderDTOs;
import teamnova.elite_gear.service.OrderService;
import teamnova.elite_gear.util.OrderStatus;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ShippingRepository shippingRepository;

    @Mock
    private ConvertTo_orderDTO_orderDTOs convertTo_orderDTO_orderDTOs;

    private UUID customerId;
    private UUID orderId;
    private UUID productVariantId;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        customerId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        productVariantId = UUID.randomUUID();
    }

    @Test
    void testGetAllOrders() {
        // Arrange
        UUID productVariantId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        ProductVariant productVariant = new ProductVariant();
        productVariant.setVariantId(productVariantId);
        productVariant.setStockQuantity(10);
        productVariant.setPriceAdjustment(200);

        OrderItem orderItem01 = new OrderItem();
        orderItem01.setOrderItemID(UUID.randomUUID());
        orderItem01.setQuantity(1);
        orderItem01.setUnitPrice(1000);
        orderItem01.setProductVariant(productVariant);

        Order order01 = new Order();
        order01.setOrderID(orderId);
        order01.setOrderDate(LocalDateTime.now());
        order01.setStatus(OrderStatus.PENDING);
        order01.setTotalAmount(1000);
        order01.setOrderItems(Set.of(orderItem01));
        order01.setCustomer(new Customer());

        Order order02 = new Order();
        order02.setOrderID(UUID.randomUUID());
        order02.setOrderDate(LocalDateTime.now());
        order02.setStatus(OrderStatus.PENDING);
        order02.setTotalAmount(2000);
        order02.setOrderItems(Set.of(orderItem01));
        order02.setCustomer(new Customer());

        List<Order> orders = Arrays.asList(order01, order02);

        // Mock repository response
        when(orderRepository.findAll()).thenReturn(orders);

        // Mock conversion logic
        List<OrderDTO> mockOrderDTOs = orders.stream().map(order -> {
            OrderDTO dto = new OrderDTO();
            dto.setOrderID(order.getOrderID());
            dto.setOrderDate(order.getOrderDate());
            dto.setTotalAmount(order.getTotalAmount());
            dto.setStatus(order.getStatus().name());
            dto.setCustomerId(order.getCustomer().getCustomerID());
            Set<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                    .map(item -> {
                        OrderItemDTO itemDTO = new OrderItemDTO();
                        itemDTO.setOrderItemID(item.getOrderItemID());
                        itemDTO.setQuantity(item.getQuantity());
                        itemDTO.setUnitPrice(item.getUnitPrice());
                        itemDTO.setTotalPrice(item.getTotalPrice());
                        itemDTO.setProductVariantId(item.getProductVariant().getVariantId());
                        return itemDTO;
                    })
                    .collect(Collectors.toSet());
            dto.setOrderItems(itemDTOs);
            return dto;
        }).toList();

        when(convertTo_orderDTO_orderDTOs.convertToDTOs(orders)).thenReturn(mockOrderDTOs);

        // Act
        List<OrderDTO> orderDTOs = orderService.getAllOrders();

        // Assert
        assertNotNull(orderDTOs);
        assertEquals(2, orderDTOs.size());
        assertEquals(orderId, orderDTOs.get(0).getOrderID());
    }



    @Test
    void testGetOrderById() {
        Order mockOrder = createMockOrder();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        OrderDTO orderDTO = orderService.getOrderById(orderId);

        assertNotNull(orderDTO);
        assertEquals(orderId, orderDTO.getOrderID());
    }

    @Test
    void testCreateOrders() {
        CreateOrderDTO createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setCustomerId(customerId);
        createOrderDTO.setItems(Set.of(createMockCreateOrderItemDTO()));

        Customer mockCustomer = createMockCustomer();
        ProductVariant mockVariant = createMockProductVariant();
        Order mockOrder = createMockOrder();

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));
        when(productVariantRepository.findById(productVariantId)).thenReturn(Optional.of(mockVariant));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        UUID createdOrderId = orderService.createOrders(createOrderDTO);

        assertNotNull(createdOrderId);
        assertEquals(orderId, createdOrderId);
    }

    @Test
    void testUpdateOrderStatus() {
        Order mockOrder = createMockOrder();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, "CONFIRMED");

        assertNotNull(updatedOrder);
        assertEquals(OrderStatus.CONFIRMED.name(), updatedOrder.getStatus());
    }

    @Test
    void testDeleteOrder() {
        Order mockOrder = createMockOrder();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        orderService.deleteOrder(orderId);

        verify(orderRepository, times(1)).delete(mockOrder);
    }

    // Helper methods to create mock data

    private Order createMockOrder() {
        Order order = new Order();
        order.setOrderID(orderId);
        order.setCustomer(createMockCustomer());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(1000);
        order.setOrderItems(Set.of(createMockOrderItem()));
        return order;
    }

    private Customer createMockCustomer() {
        Customer customer = new Customer();
        customer.setCustomerID(customerId);
        customer.setName("Test Customer");
        return customer;
    }

    private ProductVariant createMockProductVariant() {
        ProductVariant variant = new ProductVariant();
        variant.setVariantId(productVariantId);
        variant.setStockQuantity(10);
        variant.setPriceAdjustment(200);
        Product product = new Product();
        product.setBasePrice(800);
        variant.setProduct(product);
        return variant;
    }

    private OrderItem createMockOrderItem() {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderItemID(UUID.randomUUID());
        orderItem.setQuantity(1);
        orderItem.setUnitPrice(1000);
        orderItem.setProductVariant(createMockProductVariant());
        return orderItem;
    }

    private CreateOrderItemDTO createMockCreateOrderItemDTO() {
        CreateOrderItemDTO itemDTO = new CreateOrderItemDTO();
        itemDTO.setProductVariantId(productVariantId);
        itemDTO.setQuantity(1);
        return itemDTO;
    }
}
