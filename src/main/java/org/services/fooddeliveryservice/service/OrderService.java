package org.services.fooddeliveryservice.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.services.fooddeliveryservice.domain.AppUser;
import org.services.fooddeliveryservice.domain.DeliveryAssignment;
import org.services.fooddeliveryservice.domain.DeliveryPartner;
import org.services.fooddeliveryservice.domain.FoodOrder;
import org.services.fooddeliveryservice.domain.MenuItem;
import org.services.fooddeliveryservice.domain.OrderStatus;
import org.services.fooddeliveryservice.domain.Restaurant;
import org.services.fooddeliveryservice.dto.Requests.OrderLineRequest;
import org.services.fooddeliveryservice.dto.Requests.PlaceOrderRequest;
import org.services.fooddeliveryservice.dto.Responses.OrderResponse;
import org.services.fooddeliveryservice.exception.InvalidRequestException;
import org.services.fooddeliveryservice.exception.ResourceConflictException;
import org.services.fooddeliveryservice.exception.ResourceNotFoundException;
import org.services.fooddeliveryservice.exception.UnauthorizedResourceAccessException;
import org.services.fooddeliveryservice.repository.DeliveryAssignmentRepository;
import org.services.fooddeliveryservice.repository.DeliveryPartnerRepository;
import org.services.fooddeliveryservice.repository.FoodOrderRepository;
import org.services.fooddeliveryservice.repository.MenuItemRepository;
import org.services.fooddeliveryservice.repository.RestaurantRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final FoodOrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;
    private final NotificationService notificationService;

    public OrderService(FoodOrderRepository orderRepository, RestaurantRepository restaurantRepository,
                        MenuItemRepository menuItemRepository, DeliveryPartnerRepository deliveryPartnerRepository,
                        DeliveryAssignmentRepository deliveryAssignmentRepository, NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.deliveryPartnerRepository = deliveryPartnerRepository;
        this.deliveryAssignmentRepository = deliveryAssignmentRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request, AppUser customer) {
        Restaurant restaurant = restaurantRepository.findById(request.restaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        Map<Long, Integer> quantities = mergeQuantities(request.items());
        List<MenuItem> menuItems = menuItemRepository.findAllByIdForUpdate(quantities.keySet());
        if (menuItems.size() != quantities.size()) {
            throw new ResourceNotFoundException("Menu item not found");
        }

        FoodOrder order = new FoodOrder(customer, restaurant);
        for (MenuItem item : menuItems) {
            if (!item.isActive() || !item.getRestaurant().getId().equals(restaurant.getId())) {
                throw new InvalidRequestException("Menu items must belong to the selected restaurant", "MENU_ITEM_RESTAURANT_MISMATCH");
            }
            int quantity = quantities.get(item.getId());
            if (item.getStock() < quantity) {
                throw new InvalidRequestException("Insufficient stock", "INSUFFICIENT_STOCK");
            }
            item.deduct(quantity);
            order.addItem(item, quantity);
        }
        order.attachPayment(request.paymentStatus());
        FoodOrder saved = orderRepository.save(order);
        notificationService.log(restaurant.getOwner(), "New order placed: " + saved.getId());
        notificationService.log(customer, "Order placed: " + saved.getId());
        return OrderResponse.from(saved);
    }

    public OrderResponse customerOrder(Long orderId, AppUser customer) {
        return OrderResponse.from(orderRepository.findByIdAndCustomerId(orderId, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found")));
    }

    @Transactional
    public OrderResponse ownerTransition(Long orderId, AppUser owner, OrderStatus next) {
        FoodOrder order = lockedOrder(orderId);
        if (!order.getRestaurant().getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedResourceAccessException("Restaurant owner cannot update another restaurant's order");
        }
        order.transitionTo(next);
        notificationService.log(order.getCustomer(), "Order " + order.getId() + " moved to " + next);
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse acceptDelivery(Long orderId, AppUser partnerUser) {
        FoodOrder order = lockedOrder(orderId);
        if (order.getStatus() != OrderStatus.PREPARING) {
            throw new InvalidRequestException("Only preparing orders can be accepted for delivery", "INVALID_ORDER_STATUS");
        }
        if (deliveryAssignmentRepository.existsByOrderId(orderId)) {
            throw new ResourceConflictException("Order already assigned", "ORDER_ALREADY_ASSIGNED");
        }
        DeliveryPartner partner = deliveryPartnerRepository.findByUserId(partnerUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Delivery partner not found"));
        return assignPartner(order, partner);
    }

    @Transactional
    public OrderResponse assignDeliveryPartner(Long orderId, Long partnerId) {
        FoodOrder order = lockedOrder(orderId);
        if (order.getStatus() != OrderStatus.PREPARING) {
            throw new InvalidRequestException("Only preparing orders can be assigned for delivery", "INVALID_ORDER_STATUS");
        }
        if (deliveryAssignmentRepository.existsByOrderId(orderId)) {
            throw new ResourceConflictException("Order already assigned", "ORDER_ALREADY_ASSIGNED");
        }
        DeliveryPartner partner = deliveryPartnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery partner not found"));
        return assignPartner(order, partner);
    }

    @Transactional
    public OrderResponse partnerTransition(Long orderId, AppUser partnerUser, OrderStatus next) {
        FoodOrder order = lockedOrder(orderId);
        DeliveryAssignment assignment = deliveryAssignmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceConflictException("Order is not assigned", "ORDER_NOT_ASSIGNED"));
        if (!assignment.getDeliveryPartner().getUser().getId().equals(partnerUser.getId())) {
            throw new UnauthorizedResourceAccessException("Delivery partner cannot update another partner's order");
        }
        order.transitionTo(next);
        if (next == OrderStatus.DELIVERED) {
            assignment.getDeliveryPartner().markAvailable();
        }
        notificationService.log(order.getCustomer(), "Order " + order.getId() + " moved to " + next);
        return OrderResponse.from(order);
    }

    private FoodOrder lockedOrder(Long orderId) {
        return orderRepository.findByIdForUpdate(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private Map<Long, Integer> mergeQuantities(List<OrderLineRequest> items) {
        Map<Long, Integer> quantities = new HashMap<>();
        for (OrderLineRequest item : items) {
            quantities.merge(item.menuItemId(), item.quantity(), Integer::sum);
        }
        return quantities;
    }

    private OrderResponse assignPartner(FoodOrder order, DeliveryPartner partner) {
        if (!partner.isAvailable()) {
            throw new ResourceConflictException("Delivery partner is not available", "DELIVERY_PARTNER_NOT_AVAILABLE");
        }
        try {
            DeliveryAssignment assignment = deliveryAssignmentRepository.save(new DeliveryAssignment(order, partner));
            partner.markUnavailable();
            order.attachAssignment(assignment);
            notificationService.log(order.getCustomer(), "Delivery partner assigned for order " + order.getId());
            return OrderResponse.from(order);
        } catch (DataIntegrityViolationException exception) {
            throw new ResourceConflictException("Order already assigned", "ORDER_ALREADY_ASSIGNED");
        }
    }
}
