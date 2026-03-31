package com.ontop.wms.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {
    
    @GetMapping("/inventory")
    public ResponseEntity<Map<String, Object>> getInventoryReport() {
        return ResponseEntity.ok(Map.of(
            "totalProducts", 150,
            "totalStockValue", 250000.00,
            "status", "GENERATED"
        ));
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<List<Map<String, Object>>> getDashboardStats() {
        return ResponseEntity.ok(List.of(
            Map.of("title", "Doanh thu tháng", "value", "428,500,000đ", "color", "primary", "trend", "+12.5%", "isUp", true),
            Map.of("title", "Đơn hàng mới", "value", "1,284", "color", "success", "trend", "+5.2%", "isUp", true),
            Map.of("title", "Sản phẩm trong kho", "value", "8,432", "color", "warning", "trend", "-2.1%", "isUp", false),
            Map.of("title", "Khách hàng mới", "value", "456", "color", "info", "trend", "+8.4%", "isUp", true)
        ));
    }

    @GetMapping("/recent-activities")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivities() {
        return ResponseEntity.ok(List.of(
            Map.of("id", 1, "type", "inbound", "text", "Nhập kho 500 SKU: IPHONE-15-PRO", "time", "10 phút trước", "user", "Hoàng Nam"),
            Map.of("id", 2, "type", "outbound", "text", "Xuất kho 120 SKU: MACBOOK-M3-AIR", "time", "25 phút trước", "user", "Minh Thu"),
            Map.of("id", 3, "type", "alert", "text", "Cảnh báo: SKU SAM-S24-ULTRA sắp hết hàng", "time", "1 giờ trước", "user", "Hệ thống")
        ));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        return ResponseEntity.ok(Map.of(
            "monthlyInbound", List.of(10, 25, 15, 40),
            "monthlyOutbound", List.of(5, 20, 10, 35)
        ));
    }
}
