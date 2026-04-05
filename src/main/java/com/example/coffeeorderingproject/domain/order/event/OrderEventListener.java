package com.example.coffeeorderingproject.domain.order.event;

import com.example.coffeeorderingproject.domain.menu.service.PopularMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final PopularMenuService popularMenuService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCompleted(OrderCompletedEvent event) {
        popularMenuService.incrementMenuScore(event.menuId());
    }
}
