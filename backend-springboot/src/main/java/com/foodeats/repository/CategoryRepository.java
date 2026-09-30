package com.foodeats.repository;

import com.foodeats.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT c FROM Category c WHERE c.merchant.id = :merchantId")
    List<Category> findByMerchantId(@Param("merchantId") Long merchantId);

    @Query("SELECT c FROM Category c WHERE c.merchant.id = :merchantId ORDER BY c.displayOrder ASC")
    List<Category> findByMerchantIdOrderByDisplayOrderAsc(@Param("merchantId") Long merchantId);
}
