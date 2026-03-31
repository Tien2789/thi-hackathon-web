package com.ontop.wms.service;

import com.ontop.wms.entity.Asset;
import com.ontop.wms.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AssetService {
    private final AssetRepository assetRepository;

    public Double calculateDepreciation(Integer assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        // Formula: Simple Straight-Line Depreciation simulation
        // Assumes 10% annual depreciation based on age in days
        long daysOld = Duration.between(asset.getCreatedAt(), LocalDateTime.now()).toDays();
        double annualRate = 0.10;
        double dailyRate = annualRate / 365.0;
        
        return daysOld * dailyRate;
    }
}
