package com.autoservice.identityservice.repository;

import com.autoservice.identityservice.domain.entity.AccountActivationCode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface AccountActivationCodeRepository extends JpaRepository<AccountActivationCode, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from AccountActivationCode c where c.userId = :id")
    Optional<AccountActivationCode> lockCode(@Param("id") Long id);
}
