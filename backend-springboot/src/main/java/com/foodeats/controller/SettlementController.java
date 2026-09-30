package com.foodeats.controller;

import com.foodeats.model.Settlement;
import com.foodeats.service.SettlementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<List<Settlement>> getMerchantSettlements(@PathVariable Long merchantId) {
        List<Settlement> list = settlementService.getSettlementsByMerchant(merchantId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/merchant/{merchantId}/summary")
    public ResponseEntity<Map<String, Object>> getMerchantSettlementSummary(@PathVariable Long merchantId) {
        Map<String, Object> summary = settlementService.getMerchantFinancialSummary(merchantId);
        return ResponseEntity.ok(summary);
    }
}
