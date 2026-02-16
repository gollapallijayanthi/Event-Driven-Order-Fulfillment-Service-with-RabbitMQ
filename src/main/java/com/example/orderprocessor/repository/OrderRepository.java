package com.example.orderprocessor.repository;

import com.example.orderprocessor.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {
}
