package com.ontop.wms.service;

import java.util.List;

import com.ontop.wms.entity.Asset;

public interface AssetService {
    Double calculateDepreciation(Integer id);

    List<Asset> getAllAssets();

    Asset getAssetById(Long id);

    Asset createAsset(Asset asset);

    Asset updateAsset(Long id, Asset assetDetails);

    void deleteAsset(Long id);
}
