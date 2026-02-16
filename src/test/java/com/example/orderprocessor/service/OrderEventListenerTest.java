package com.example.orderprocessor.service;

import com.example.orderprocessor.event.OrderPlacedEvent;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderEventListenerTest {

    private OrderService orderService;
    private Channel channel;
    private OrderEventListener listener;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        channel = mock(Channel.class);
        listener = new OrderEventListener(orderService);
    }

    private Message message(long tag) {
        MessageProperties props = new MessageProperties();
        props.setDeliveryTag(tag);
        return new Message(new byte[0], props);
    }

    @Test
    void shouldAckOnSuccessfulProcessing() throws Exception {
        OrderPlacedEvent event = new OrderPlacedEvent();
        event.setOrderId("order1");

        doNothing().when(orderService).handleOrderPlaced(any());

        listener.handleOrderPlaced(event, message(1L), channel);

        verify(channel).basicAck(1L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void shouldAckWhenAlreadyProcessed() throws Exception {
        OrderPlacedEvent event = new OrderPlacedEvent();
        event.setOrderId("order2");

        doNothing().when(orderService).handleOrderPlaced(any());

        listener.handleOrderPlaced(event, message(2L), channel);

        verify(channel).basicAck(2L, false);
    }

    @Test
    void shouldNackOnFailure() throws Exception {
        OrderPlacedEvent event = new OrderPlacedEvent();
        event.setOrderId("order3");

        doThrow(new RuntimeException("boom"))
                .when(orderService)
                .handleOrderPlaced(any());

        listener.handleOrderPlaced(event, message(3L), channel);

        verify(channel).basicNack(3L, false, true);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }
}
