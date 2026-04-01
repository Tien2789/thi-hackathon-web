package com.ontop.wms.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ontop.wms.entity.Asset;
import com.ontop.wms.repository.AssetRepository;
import com.ontop.wms.service.AssetService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetController {
    private final AssetService assetService;
    private final AssetRepository assetRepository;

    @GetMapping
    public ResponseEntity<List<Asset>> getAllAssets() {
        return ResponseEntity.ok(assetRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Asset> createAsset(@RequestBody Asset asset) {
        if (asset == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(assetRepository.save(asset));
    }

    @GetMapping("/{id}/depreciation")
    public ResponseEntity<Map<String, Double>> getDepreciation(@PathVariable Integer id) {
        Double depreciation = assetService.calculateDepreciation(id);
        return ResponseEntity.ok(Map.of("depreciationValue", depreciation));
    }
}
