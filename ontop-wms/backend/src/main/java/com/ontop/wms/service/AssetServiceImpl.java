package com.ontop.wms.service;

import com.ontop.wms.entity.Asset;
import com.ontop.wms.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;

    @Override
    public Double calculateDepreciation(Integer id) {
        if (id == null) {
            return 0.0;
        }
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Asset not found with id: " + id));

        // Simplified placeholder depreciation logic:
        // Assume a base value of 1000.0 and 5% annual depreciation from createdAt.
        double baseValue = 1000.0;
        double annualRate = 0.05;

        LocalDateTime start = asset.getCreatedAt() != null ? asset.getCreatedAt().toLocalDateTime() : LocalDateTime.now();
        long years = ChronoUnit.YEARS.between(start, LocalDateTime.now());

        double currentDepreciationValue = baseValue * Math.pow(1 - annualRate, Math.max(0, years));
        return baseValue - currentDepreciationValue;
    }
}
