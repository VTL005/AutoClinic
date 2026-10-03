package com.autoservice.identityservice.repository;

import com.autoservice.identityservice.domain.entity.User;
import com.autoservice.identityservice.domain.enums.AccountStatus;
import com.autoservice.identityservice.domain.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByIdAndDeletedFalse(
            Long id
    );

    Optional<User> findByUsernameIgnoreCaseAndDeletedFalse(
            String username
    );

    Optional<User> findByPhoneAndDeletedFalse(
            String phone
    );

    Optional<User> findByEmailIgnoreCaseAndDeletedFalse(
            String email
    );

    boolean existsByUsernameIgnoreCase(
            String username
    );

    boolean existsByPhone(
            String phone
    );

    boolean existsByEmailIgnoreCase(
            String email
    );

    boolean existsByPhoneAndIdNot(
            String phone,
            Long userId
    );

    boolean existsByEmailIgnoreCaseAndIdNot(
            String email,
            Long userId
    );

    @Query("""
            SELECT user
            FROM User user
            WHERE user.deleted = false
              AND (
                    :keyword IS NULL
                    OR LOWER(user.username)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(user.fullName)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(user.email, ''))
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR user.phone
                        LIKE CONCAT('%', :keyword, '%')
              )
              AND (:role IS NULL OR user.role = :role)
              AND (
                    :accountStatus IS NULL
                    OR user.accountStatus = :accountStatus
              )
            """)
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("role") Role role,
            @Param("accountStatus")
            AccountStatus accountStatus,
            Pageable pageable
    );
}