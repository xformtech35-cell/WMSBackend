package com.warehouse.wms.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.warehouse.wms.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    boolean existsByRoleId(Long roleId);
    
    
    @Query("""
            SELECT u FROM User u
            WHERE (:search IS NULL OR
                   LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(u.email)    LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:role   IS NULL OR u.role = :role)
              AND (:active IS NULL OR u.active = :active)
        """)
        Page<User> searchUsers(
                @Param("search") String search,
                @Param("role")   String role,
                @Param("active") Boolean active,
                Pageable pageable
        );
}
