package com.example.coffeeorderingproject.domain.orderitem.repository;

import com.example.coffeeorderingproject.domain.orderitem.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder_OrderId(Long orderId);
}
