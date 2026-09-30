package com.foodeats.repository;

import com.foodeats.model.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    Optional<Settlement> findByOrderId(Long orderId);

    List<Settlement> findByMerchantIdOrderByCreatedAtDesc(Long merchantId);

    List<Settlement> findByDriverIdOrderByCreatedAtDesc(Long driverId);

    List<Settlement> findByStatus(String status);


}
