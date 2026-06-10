package com.example.ecommercemgmz.dashboard;

import com.example.ecommercemgmz.order.CustomerOrder;
import com.example.ecommercemgmz.order.CustomerOrderRepository;
import com.example.ecommercemgmz.order.OrderStatus;
import com.example.ecommercemgmz.payment.PaymentStatus;
import com.example.ecommercemgmz.product.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 5;

    private final CustomerOrderRepository orderRepository;
    private final ProductRepository productRepository;

    public DashboardService(CustomerOrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        List<CustomerOrder> orders = orderRepository.findAll();
        BigDecimal grossRevenue = orders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .map(CustomerOrder::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paidRevenue = orders.stream()
                .filter(order -> order.getPaymentStatus() == PaymentStatus.PAID)
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .map(CustomerOrder::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardSummaryResponse(
                orders.size(),
                countByStatus(orders, OrderStatus.PENDING_PAYMENT),
                countByStatus(orders, OrderStatus.PAID),
                countByStatus(orders, OrderStatus.PROCESSING),
                countByStatus(orders, OrderStatus.SHIPPED),
                countByStatus(orders, OrderStatus.COMPLETED),
                countByStatus(orders, OrderStatus.CANCELLED),
                grossRevenue,
                paidRevenue,
                productRepository.findTop20ByStockLessThanEqualOrderByStockAscNameAsc(DEFAULT_LOW_STOCK_THRESHOLD).size(),
                orderRepository.findTop10ByOrderByCreatedAtDesc().stream().map(RecentOrderResponse::from).toList()
        );
    }

    @Transactional(readOnly = true)
    public List<LowStockProductResponse> findLowStockProducts(int threshold) {
        int safeThreshold = Math.max(threshold, 0);
        return productRepository.findTop20ByStockLessThanEqualOrderByStockAscNameAsc(safeThreshold).stream()
                .map(LowStockProductResponse::from)
                .toList();
    }

    private long countByStatus(List<CustomerOrder> orders, OrderStatus status) {
        return orders.stream().filter(order -> order.getStatus() == status).count();
    }
}
