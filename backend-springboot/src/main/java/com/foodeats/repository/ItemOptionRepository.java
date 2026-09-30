package com.foodeats.repository;

import com.foodeats.model.ItemOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ItemOptionRepository extends JpaRepository<ItemOption, Long> {
    @Query("SELECT io FROM ItemOption io WHERE io.menuItem.id = :menuItemId")
    List<ItemOption> findByMenuItemId(@Param("menuItemId") Long menuItemId);

    @Modifying
    @Query("DELETE FROM ItemOption io WHERE io.menuItem.id = :menuItemId")
    void deleteByMenuItemId(@Param("menuItemId") Long menuItemId);
}
