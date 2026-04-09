package com.ontop.wms.service;

import com.ontop.wms.entity.Asset;
import com.ontop.wms.entity.Warehouse;
import com.ontop.wms.repository.AssetRepository;
import com.ontop.wms.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AssetServiceImpl implements AssetService {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Override
    public Double calculateDepreciation(Integer id) {
        // Assuming a simple straight-line depreciation for 5 years (60 months)
        Asset asset = getAssetById(id);
        if (asset == null || asset.getCreatedAt() == null) {
            return 0.0;
        }

        long months = Duration.between(asset.getCreatedAt(), LocalDateTime.now()).toDays() / 30;
        double originalValue = 1000; // Placeholder for asset's original value
        double depreciationPerMonth = originalValue / 60;

        double accumulatedDepreciation = months * depreciationPerMonth;
        return Math.max(0, originalValue - accumulatedDepreciation);
    }

    @Override
    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }

    @Override
    public Asset getAssetById(Integer id) {
        return assetRepository.findById(id).orElseThrow(() -> new RuntimeException("Asset not found with id: " + id));
    }

    @Override
    public Asset createAsset(Asset asset) {
        // Make sure to set the warehouse correctly if its ID is provided
        if (asset.getWarehouse() != null && asset.getWarehouse().getId() != null) {
            Warehouse warehouse = warehouseRepository.findById(asset.getWarehouse().getId())
                    .orElseThrow(() -> new RuntimeException("Warehouse not found"));
            asset.setWarehouse(warehouse);
        }
        return assetRepository.save(asset);
    }

    @Override
    public Asset updateAsset(Integer id, Asset assetDetails) {
        Asset asset = getAssetById(id);

        asset.setAssetCode(assetDetails.getAssetCode());
        asset.setName(assetDetails.getName());
        asset.setDescription(assetDetails.getDescription());
        asset.setQuantity(assetDetails.getQuantity());

        if (assetDetails.getWarehouse() != null && assetDetails.getWarehouse().getId() != null) {
            Warehouse warehouse = warehouseRepository.findById(assetDetails.getWarehouse().getId())
                    .orElseThrow(() -> new RuntimeException("Warehouse not found"));
            asset.setWarehouse(warehouse);
        }

        return assetRepository.save(asset);
    }

    @Override
    public void deleteAsset(Integer id) {
        Asset asset = getAssetById(id);
        assetRepository.delete(asset);
    }
}
