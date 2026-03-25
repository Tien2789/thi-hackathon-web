package com.ontop.wms.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ontop.wms.entity.Product;
import com.ontop.wms.repository.InventoryInRepository;
import com.ontop.wms.repository.InventoryOutRepository;
import com.ontop.wms.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ProductRepository productRepository;
    private final InventoryInRepository inventoryInRepository;
    private final InventoryOutRepository inventoryOutRepository;

    // --- Dashboard Stats (dữ liệu thực từ DB) ---
    @GetMapping("/dashboard-stats")
    public ResponseEntity<List<Map<String, Object>>> getDashboardStats() {
        long totalProducts = productRepository.count();
        long totalInbounds = inventoryInRepository.count();
        long totalOutbounds = inventoryOutRepository.count();

        long lowStockCount = productRepository.findAll().stream()
                .filter(p -> p.getCurrentStock() != null && p.getMinStock() != null
                        && p.getCurrentStock() <= p.getMinStock())
                .count();

        List<Map<String, Object>> stats = new ArrayList<>();

        Map<String, Object> stat1 = new HashMap<>();
        stat1.put("title", "Sản phẩm trong kho");
        stat1.put("value", String.valueOf(totalProducts));
        stat1.put("color", "primary");
        stat1.put("trend", "Tổng SKU");
        stat1.put("isUp", true);
        stats.add(stat1);

        Map<String, Object> stat2 = new HashMap<>();
        stat2.put("title", "Phiếu nhập kho");
        stat2.put("value", String.valueOf(totalInbounds));
        stat2.put("color", "success");
        stat2.put("trend", "Tổng phiếu");
        stat2.put("isUp", true);
        stats.add(stat2);

        Map<String, Object> stat3 = new HashMap<>();
        stat3.put("title", "Lệnh xuất kho");
        stat3.put("value", String.valueOf(totalOutbounds));
        stat3.put("color", "warning");
        stat3.put("trend", "Tổng lệnh");
        stat3.put("isUp", true);
        stats.add(stat3);

        Map<String, Object> stat4 = new HashMap<>();
        stat4.put("title", "Cảnh báo tồn kho");
        stat4.put("value", String.valueOf(lowStockCount));
        stat4.put("color", "danger");
        stat4.put("trend", "Sản phẩm sắp hết");
        stat4.put("isUp", false);
        stats.add(stat4);

        return ResponseEntity.ok(stats);
    }

    // --- Recent Activities (10 phiếu nhập/xuất gần nhất) ---
    @GetMapping("/recent-activities")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivities() {
        List<Map<String, Object>> activities = new ArrayList<>();

        // Lấy 5 phiếu nhập gần nhất
        inventoryInRepository.findAll().stream()
                .sorted((a, b) -> {
                    if (b.getCreatedAt() == null) return -1;
                    if (a.getCreatedAt() == null) return 1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .limit(5)
                .forEach(inv -> {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("id", inv.getId());
                    activity.put("type", "inbound");
                    activity.put("text", "Nhập kho: " + inv.getReceiptCode() + " (" + inv.getStatus() + ")");
                    activity.put("time", inv.getCreatedAt() != null ? inv.getCreatedAt().toString() : "N/A");
                    activity.put("user", inv.getApprovedBy() != null ? inv.getApprovedBy().getUsername() : "Hệ thống");
                    activities.add(activity);
                });

        // Lấy 5 lệnh xuất gần nhất
        inventoryOutRepository.findAll().stream()
                .sorted((a, b) -> {
                    if (b.getCreatedAt() == null) return -1;
                    if (a.getCreatedAt() == null) return 1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .limit(5)
                .forEach(inv -> {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("id", inv.getId() + 10000);
                    activity.put("type", "outbound");
                    activity.put("text", "Xuất kho: " + inv.getIssueCode() + " (" + inv.getStatus() + ")");
                    activity.put("time", inv.getCreatedAt() != null ? inv.getCreatedAt().toString() : "N/A");
                    activity.put("user", inv.getApprovedBy() != null ? inv.getApprovedBy().getUsername() : "Hệ thống");
                    activities.add(activity);
                });

        return ResponseEntity.ok(activities);
    }

    // --- Inventory Status (cho Reports.vue) ---
    @GetMapping("/inventory-status")
    public ResponseEntity<List<Map<String, Object>>> getInventoryStatus() {
        List<Map<String, Object>> result = productRepository.findAll().stream()
                .map(p -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("productName", p.getProductName());
                    item.put("skuCode", p.getSkuCode());
                    item.put("currentStock", p.getCurrentStock() != null ? p.getCurrentStock() : 0);
                    item.put("minStock", p.getMinStock() != null ? p.getMinStock() : 0);
                    item.put("categoryName", p.getCategory() != null ? p.getCategory().getName() : "N/A");
                    return item;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // --- Alerts (sản phẩm có tồn kho thấp, cho Reports.vue) ---
    @GetMapping("/alerts")
    public ResponseEntity<List<Map<String, Object>>> getAlerts() {
        List<Map<String, Object>> alerts = productRepository.findAll().stream()
                .filter(p -> p.getCurrentStock() != null && p.getMinStock() != null
                        && p.getCurrentStock() <= p.getMinStock())
                .map(p -> {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("message", "Sắp hết hàng: " + p.getProductName()
                            + " (SKU: " + p.getSkuCode() + ") - Tồn kho: " + p.getCurrentStock());
                    alert.put("severity", p.getCurrentStock() == 0 ? "CRITICAL" : "WARNING");
                    return alert;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(alerts);
    }

    // --- Inventory Report tổng hợp ---
    @GetMapping("/inventory")
    public ResponseEntity<Map<String, Object>> getInventoryReport() {
        List<Product> products = productRepository.findAll();
        long totalProducts = products.size();
        int totalStock = products.stream()
                .mapToInt(p -> p.getCurrentStock() != null ? p.getCurrentStock() : 0)
                .sum();

        Map<String, Object> report = new HashMap<>();
        report.put("totalProducts", totalProducts);
        report.put("totalStock", totalStock);
        report.put("status", "GENERATED");
        return ResponseEntity.ok(report);
    }

    // --- Dashboard chart data ---
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        long inboundCount = inventoryInRepository.count();
        long outboundCount = inventoryOutRepository.count();

        Map<String, Object> data = new HashMap<>();
        data.put("monthlyInbound", List.of(inboundCount));
        data.put("monthlyOutbound", List.of(outboundCount));
        return ResponseEntity.ok(data);
    }
}
