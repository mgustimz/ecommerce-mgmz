package com.example.ecommercemgmz.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/summary")
    DashboardSummaryResponse getSummary() {
        return dashboardService.getSummary();
    }

    @GetMapping("/low-stock-products")
    List<LowStockProductResponse> findLowStockProducts(@RequestParam(defaultValue = "5") int threshold) {
        return dashboardService.findLowStockProducts(threshold);
    }
}
