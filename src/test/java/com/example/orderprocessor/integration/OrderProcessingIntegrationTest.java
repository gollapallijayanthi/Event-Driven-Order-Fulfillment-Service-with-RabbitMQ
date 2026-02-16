package com.example.orderprocessor.integration;

import com.example.orderprocessor.event.OrderPlacedEvent;
import com.example.orderprocessor.model.Order;
import com.example.orderprocessor.model.OrderStatus;
import com.example.orderprocessor.repository.OrderRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * ⚠️ Integration test
 * Disabled by default.
 * Enable ONLY with:
 * mvn test -Dintegration=true
 */
@Disabled("Requires Docker/Testcontainers")
@ActiveProfiles("integration")
@Testcontainers
@SpringBootTest
class OrderProcessingIntegrationTest {

    @Container
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("orderdb")
                    .withUsername("user")
                    .withPassword("password");

    @Container
    static RabbitMQContainer rabbit =
            new RabbitMQContainer("rabbitmq:3.13-management");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);

        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
    }

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    OrderRepository orderRepository;

    @Test
    void shouldProcessOrderEndToEnd() {

        OrderPlacedEvent event = new OrderPlacedEvent();
        event.setOrderId("it-order-1");
        event.setProductId("prod-1");
        event.setCustomerId("cust-1");
        event.setQuantity(2);

        rabbitTemplate.convertAndSend(
                "order.events",
                "order.placed",
                event
        );

        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    Order order = orderRepository
                            .findById("it-order-1")
                            .orElseThrow();

                    assertThat(order.getStatus())
                            .isEqualTo(OrderStatus.PROCESSED);
                });
    }
}
