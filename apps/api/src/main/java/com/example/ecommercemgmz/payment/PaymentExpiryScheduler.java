package com.example.ecommercemgmz.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentExpiryScheduler {
    private final PaymentService paymentService;

    @Scheduled(fixedDelay = 60_000)
    public void releaseExpiredHolds() {
        int released = paymentService.expireStaleOrders();
        if (released > 0) {
            log.info("Payment expiry sweep released stock for {} order(s)", released);
        }
    }
}
