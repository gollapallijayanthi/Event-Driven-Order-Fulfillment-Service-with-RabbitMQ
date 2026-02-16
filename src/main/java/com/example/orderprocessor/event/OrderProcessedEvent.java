package com.example.orderprocessor.event;

public class OrderProcessedEvent {

    private String orderId;
    private String status;
    private String processedAt;

    public OrderProcessedEvent(String orderId, String status, String processedAt) {
        this.orderId = orderId;
        this.status = status;
        this.processedAt = processedAt;
    }

    public String getOrderId() { return orderId; }
    public String getStatus() { return status; }
    public String getProcessedAt() { return processedAt; }
}
