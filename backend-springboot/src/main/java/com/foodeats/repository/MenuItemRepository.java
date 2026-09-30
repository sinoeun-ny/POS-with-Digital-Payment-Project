package com.foodeats.repository;

import com.foodeats.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    @Query("SELECT m FROM MenuItem m WHERE m.category.id = :categoryId")
    List<MenuItem> findByCategoryId(@Param("categoryId") Long categoryId);

    List<MenuItem> findByNameContainingIgnoreCase(String query);

    @Query("SELECT m FROM MenuItem m WHERE (m.merchant IS NOT NULL AND m.merchant.id = :merchantId) OR (m.category IS NOT NULL AND m.category.merchant.id = :merchantId)")
    List<MenuItem> findByMerchantId(@Param("merchantId") Long merchantId);
}
