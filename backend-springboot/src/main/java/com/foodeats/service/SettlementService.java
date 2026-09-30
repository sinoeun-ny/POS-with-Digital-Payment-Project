package com.foodeats.service;

import com.foodeats.model.Order;
import com.foodeats.model.Settlement;
import com.foodeats.model.User;

import java.util.List;
import java.util.Map;

public interface SettlementService {

    Settlement createSettlementForOrder(Order order);

    Settlement finalizeSettlement(Long orderId);

    Settlement updateSettlementDriver(Long orderId, User driver);

    List<Settlement> getSettlementsByMerchant(Long merchantId);

    Map<String, Object> getMerchantFinancialSummary(Long merchantId);
}
