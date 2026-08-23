package com.foodeats.repository;

import com.foodeats.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByMerchantId(Long merchantId);
}
