package com.autoservice.identityservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoservice.identityservice.domain.entity.User;

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
}