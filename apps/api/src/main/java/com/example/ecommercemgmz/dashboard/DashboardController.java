package com.example.ecommercemgmz.dashboard;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    DashboardSummaryResponse getSummary() {
        return dashboardService.getSummary();
    }

    @GetMapping("/low-stock-products")
    List<LowStockProductResponse> findLowStockProducts(@RequestParam(defaultValue = "5") int threshold) {
        return dashboardService.findLowStockProducts(threshold);
    }
}
