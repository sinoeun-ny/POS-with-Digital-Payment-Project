package com.foodeats.service.impl;

import com.foodeats.model.Order;
import com.foodeats.model.Settlement;
import com.foodeats.model.User;
import com.foodeats.repository.SettlementRepository;
import com.foodeats.service.SettlementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;

    public SettlementServiceImpl(SettlementRepository settlementRepository) {
        this.settlementRepository = settlementRepository;
    }

    @Override
    @Transactional
    public Settlement createSettlementForOrder(Order order) {
        if (order == null || order.getMerchant() == null) {
            return null;
        }

        // Avoid duplicate settlement
        Optional<Settlement> existing = settlementRepository.findByOrderId(order.getId());
        if (existing.isPresent()) {
            return existing.get();
        }

        double subtotal = order.getSubtotal() != null ? order.getSubtotal() :
                (order.getTotalAmount() - (order.getDeliveryFee() != null ? order.getDeliveryFee() : 1.50));
        double deliveryFee = order.getDeliveryFee() != null ? order.getDeliveryFee() : 1.50 ;

        Settlement settlement = new Settlement (

                order,
                order.getMerchant(),
                order.getDriver(),
                subtotal,
                deliveryFee,
                0.15 // 15% commission

        );

        return settlementRepository.save(settlement);
    }

    @Override
    @Transactional
    public Settlement finalizeSettlement(Long orderId) {
        Optional<Settlement> opt = settlementRepository.findByOrderId(orderId);
        if (opt.isEmpty()) {
            return null;
        }

        Settlement settlement = opt.get();
        settlement.setStatus("SETTLED");
        settlement.setSettledAt(LocalDateTime.now());
        return settlementRepository.save(settlement);
    }

    @Override
    @Transactional
    public Settlement updateSettlementDriver(Long orderId, User driver) {
        Optional<Settlement> opt = settlementRepository.findByOrderId(orderId);
        if (opt.isPresent()) {
            Settlement s = opt.get();
            s.setDriver(driver);
            return settlementRepository.save(s);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Settlement> getSettlementsByMerchant(Long merchantId){
        return settlementRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId);
    }


    @Override
    @Transactional (readOnly = true)
    public Map<String, Object> getMerchantFinancialSummary(Long merchantId) {
        List<Settlement> settlements = settlementRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId);

        double totalGrossSales = 0.0;
        double totalPlatformCommission = 0.0;
        double totalSettledPayout = 0.0;
        double totalPendingPayout = 0.0;
        int settledCount = 0;
        int pendingCount = 0;

        for (Settlement s : settlements) {
            totalGrossSales += (s.getSubtotal() != null ? s.getSubtotal() : 0.0);
            totalPlatformCommission += (s.getPlatformFee() != null ? s.getPlatformFee() : 0.0);

            if ("SETTLED".equalsIgnoreCase(s.getStatus())) {
                totalSettledPayout += (s.getMerchantPayout() != null ? s.getMerchantPayout() : 0.0);
                settledCount++;
            } else if ("PENDING".equalsIgnoreCase(s.getStatus())) {
                totalPendingPayout += (s.getMerchantPayout() != null ? s.getMerchantPayout() : 0.0);
                pendingCount++;
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("merchantId" , merchantId) ;
        summary.put("totalGrossSales", Math.round(totalGrossSales * 100.0) / 100.0);
        summary.put("commissionRate", "15%");
        summary.put("totalPlatformCommission", Math.round(totalPlatformCommission * 100.0) / 100.0);
        summary.put("availableSettledPayout", Math.round(totalSettledPayout * 100.0) / 100.0);
        summary.put("pendingInEscrowPayout", Math.round(totalPendingPayout * 100.0) / 100.0);
        summary.put("totalOrdersCount", settlements.size());
        summary.put("settledOrdersCount", settledCount);
        summary.put("pendingOrdersCount", pendingCount);
        summary.put("settlements", settlements);

        return summary;
    }

}
