package com.example.coffeeorderingproject.domain.order.repository;

import com.example.coffeeorderingproject.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
