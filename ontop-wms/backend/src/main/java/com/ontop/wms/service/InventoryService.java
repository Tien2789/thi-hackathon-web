package com.ontop.wms.service;

import java.util.List;

import com.ontop.wms.dto.ApproveRequest;
import com.ontop.wms.dto.InboundDTO;
import com.ontop.wms.dto.InventoryRequest;
import com.ontop.wms.dto.OutboundDTO;
import com.ontop.wms.entity.InventoryIn;
import com.ontop.wms.entity.InventoryOut;

public interface InventoryService {
    List<InboundDTO> getAllInbounds();
    InventoryIn createInbound(InventoryRequest request);
    InventoryIn approveInbound(Integer id, ApproveRequest request);
    void undoInbound(Integer id);
    List<OutboundDTO> getAllOutbounds();
    InventoryOut createOutbound(InventoryRequest request);
    InventoryOut approveOutbound(Integer id, ApproveRequest request);
    InventoryOut undoOutbound(Integer id);
}