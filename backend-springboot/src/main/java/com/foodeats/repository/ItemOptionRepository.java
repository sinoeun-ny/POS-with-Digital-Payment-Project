package com.foodeats.repository;

import com.foodeats.model.ItemOption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItemOptionRepository extends JpaRepository<ItemOption, Long> {
    List<ItemOption> findByMenuItemId(Long menuItemId);
    void deleteByMenuItemId(Long menuItemId);
}
