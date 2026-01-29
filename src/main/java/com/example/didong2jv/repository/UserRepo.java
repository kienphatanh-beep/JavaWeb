package com.example.didong2jv.repository;

import com.example.didong2jv.entity.User;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface UserRepo extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u JOIN FETCH u.addresses a WHERE a.addressId = ?1")
    List<User> findByAddress(Long addressId);

    Optional<User> findByEmail(String email);
}