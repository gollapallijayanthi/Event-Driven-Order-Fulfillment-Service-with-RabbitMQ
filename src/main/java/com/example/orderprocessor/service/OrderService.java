package com.example.orderprocessor.service;

import com.example.orderprocessor.event.OrderPlacedEvent;
import com.example.orderprocessor.event.OrderProcessedEvent;
import com.example.orderprocessor.model.Order;
import com.example.orderprocessor.model.OrderStatus;
import com.example.orderprocessor.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;
public void handleOrderPlaced(OrderPlacedEvent event) {
    processOrder(event);
}


    @Transactional
    public void processOrder(OrderPlacedEvent event) {

        Order order = orderRepository
                .findById(event.getOrderId())
                .orElseGet(() -> createNewOrder(event));

        
        if (order.getStatus() == OrderStatus.PROCESSED) {
            log.info(
                "Duplicate event ignored | orderId={}",
                event.getOrderId()
            );
            return;
        }

        
        order.setStatus(OrderStatus.PROCESSING);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        
        order.setStatus(OrderStatus.PROCESSED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

  
        publishOrderProcessedEvent(order.getId());

        log.info(
            "Order processed successfully | orderId={}",
            order.getId()
        );
    }

    @Transactional
    public void markOrderFailed(String orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(OrderStatus.FAILED);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            log.warn(
                "Order marked FAILED | orderId={}",
                orderId
            );
        });
    }

    private Order createNewOrder(OrderPlacedEvent event) {
        Order order = new Order();
        order.setId(event.getOrderId());
        order.setProductId(event.getProductId());
        order.setCustomerId(event.getCustomerId());
        order.setQuantity(event.getQuantity());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }

    private void publishOrderProcessedEvent(String orderId) {

        OrderProcessedEvent event = new OrderProcessedEvent(
                orderId,
                "PROCESSED",
                LocalDateTime.now().toString()
        );

        rabbitTemplate.convertAndSend(
                "order.events",
                "order.processed",
                event
        );

        log.info(
            "Published OrderProcessedEvent | orderId={}",
            orderId
        );
    }
}
