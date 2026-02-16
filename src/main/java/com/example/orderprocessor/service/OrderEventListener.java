package com.example.orderprocessor.service;

import com.example.orderprocessor.event.OrderPlacedEvent;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderService orderService;

    @RabbitListener(
            queues = "order.placed.queue",
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleOrderPlaced(
            OrderPlacedEvent event,
            Message message,
            Channel channel
    ) throws Exception {

        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            log.info("Received OrderPlacedEvent | orderId={}", event.getOrderId());

            
            orderService.handleOrderPlaced(event);

            
            channel.basicAck(deliveryTag, false);

        } catch (Exception ex) {

            log.error(
                    "Error processing OrderPlacedEvent | orderId={}",
                    event.getOrderId(),
                    ex
            );

            
            channel.basicNack(deliveryTag, false, true);
        }
    }
}
