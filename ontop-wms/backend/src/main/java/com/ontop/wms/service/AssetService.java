package com.ontop.wms.service;

import java.util.List;

import com.ontop.wms.entity.Asset;

public interface AssetService {
    Double calculateDepreciation(Integer id);

    List<Asset> getAllAssets();

    Asset getAssetById(Integer id);

    Asset createAsset(Asset asset);

    Asset updateAsset(Integer id, Asset assetDetails);

    void deleteAsset(Integer id);
}
