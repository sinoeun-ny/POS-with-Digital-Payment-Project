package com.foodeats.repository;

import com.foodeats.model.Merchant;
import org.hibernate.cache.spi.entry.StructuredCacheEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    List<Merchant> findByNameContainingIgnoreCase(String query);

    @Query(value = "SELECT * FROM merchants WHERE owner_user_id = :ownerId", nativeQuery = true)
    List<Merchant> findByOwnerId(@Param("ownerId") Long ownerId);

    @Query(
            value = "CALL sp_filter_merchants(:searchKeyword, :cuisineType, :minRating, :maxFee )",
            nativeQuery = true
    )

    List<Merchant> filterMerchants(
            @Param("searchKeyword") String searchKeyword ,
            @Param("cuisineType") String cuisineType ,
            @Param("minRating") Double minRating ,
            @Param("maxFee") Double maxFee
    );

}
