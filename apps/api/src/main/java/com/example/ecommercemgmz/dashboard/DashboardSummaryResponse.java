package com.example.ecommercemgmz.dashboard;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryResponse(
        long totalOrders,
        long pendingPaymentOrders,
        long paidOrders,
        long processingOrders,
        long shippedOrders,
        long completedOrders,
        long cancelledOrders,
        BigDecimal grossRevenue,
        BigDecimal paidRevenue,
        long lowStockProducts,
        List<RecentOrderResponse> recentOrders
) {
}
