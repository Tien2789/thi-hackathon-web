package com.ontop.wms.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ontop.wms.repository.AssetRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;

    @Override
    public Double calculateDepreciation(Integer id) {
        if (id == null) {
            return 0.0;
        }
        com.ontop.wms.entity.Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Asset not found with id: " + id));

        // Simplified placeholder depreciation logic:
        // Assume a base value of 1000.0 and 5% annual depreciation from createdAt.
        double baseValue = 1000.0;
        double annualRate = 0.05;

        LocalDateTime start = asset.getCreatedAt() != null ? asset.getCreatedAt() : LocalDateTime.now();
        long years = ChronoUnit.YEARS.between(start, LocalDateTime.now());

        double currentDepreciationValue = baseValue * Math.pow(1 - annualRate, Math.max(0, years));
        return baseValue - currentDepreciationValue;
    }

    @Override
    public List<com.ontop.wms.model.Asset> getAllAssets() {
        // TODO: Implement actual logic
        return Collections.emptyList();
    }

    @Override
    public com.ontop.wms.model.Asset getAssetById(Long id) {
        // TODO: Implement actual logic
        return null;
    }

    @Override
    public com.ontop.wms.model.Asset createAsset(com.ontop.wms.model.Asset asset) {
        // TODO: Implement actual logic
        return null;
    }

    @Override
    public com.ontop.wms.model.Asset updateAsset(Long id, com.ontop.wms.model.Asset assetDetails) {
        // TODO: Implement actual logic
        return null;
    }

    @Override
    public void deleteAsset(Long id) {
        // TODO: Implement actual logic
    }
}
