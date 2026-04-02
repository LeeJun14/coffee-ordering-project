package com.example.coffeeorderingproject.domain.orderitem.entity;

import com.example.coffeeorderingproject.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "menu_id", nullable = false)
    private Long menuId;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private Integer quantity;

    public static OrderItem create(Order order, Long menuId, Long price, Integer quantity) {
        OrderItem orderItem = new OrderItem();
        orderItem.order = order;
        orderItem.menuId = menuId;
        orderItem.amount = price;
        orderItem.quantity = quantity;
        return orderItem;
    }
}
