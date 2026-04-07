package com.ontop.wms.service;

import com.ontop.wms.model.Asset;

import java.util.List;

public interface AssetService {
    Double calculateDepreciation(Integer id);

    List<Asset> getAllAssets();

    Asset getAssetById(Long id);

    Asset createAsset(Asset asset);

    Asset updateAsset(Long id, Asset assetDetails);

    void deleteAsset(Long id);
}
