package com.foodeats.repository;

import com.foodeats.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    List<Merchant> findByNameContainingIgnoreCase(String query);
    Optional<Merchant> findByOwnerId(Long ownerId);
}
