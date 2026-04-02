package com.example.coffeeorderingproject.domain.order.entity;

import com.example.coffeeorderingproject.common.entity.CreatableEntity;
import com.example.coffeeorderingproject.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends CreatableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Long totalAmount;

    public static Order create(User user, Long totalAmount) {
        Order order = new Order();
        order.user = user;
        order.totalAmount = totalAmount;
        return order;
    }
}
