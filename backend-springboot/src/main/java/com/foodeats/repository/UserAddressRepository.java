package com.foodeats.repository;

import com.foodeats.model.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    //fetch/deisplay address on the website
    @Query("SELECT ua FROM UserAddress ua WHERE ua.user.id = :userId")
    List<UserAddress> findByUserId(@Param("userId") Long userId);

    //insert address to proc
        @Procedure(procedureName = "sp_add_user_address")
        Long spAddUserAddress(
                @Param("p_user_id") Long userId,
                @Param("p_label") String label ,
                @Param("p_street_address") String streetAddress ,
                @Param("p_city") String city ,
                @Param("p_is_default") Boolean isDefault
        );

}
