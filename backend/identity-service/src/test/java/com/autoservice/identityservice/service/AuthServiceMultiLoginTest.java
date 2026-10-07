package com.autoservice.identityservice.service;

import com.autoservice.identityservice.domain.entity.User;
import com.autoservice.identityservice.domain.enums.AccountStatus;
import com.autoservice.identityservice.dto.request.LoginRequest;
import com.autoservice.identityservice.exception.BusinessException;
import com.autoservice.identityservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceMultiLoginTest {
    private final UserRepository users = mock(UserRepository.class);
    private final PasswordEncoder passwords = mock(PasswordEncoder.class);
    private final JwtService jwt = mock(JwtService.class);
    private final RefreshTokenService refresh = mock(RefreshTokenService.class);
    private final AuthService service = new AuthService(users, passwords, jwt, refresh, mock(AccountActivationService.class));

    private User user(Long id, AccountStatus status) {
        User user = new User();
        user.setId(id); user.setPasswordHash("hash-" + id);
        user.setAccountStatus(status); user.setFailedLoginCount(0);
        return user;
    }

    private void invalidPassword(String identifier, User expected) {
        assertThatThrownBy(() -> service.login(new LoginRequest(identifier, "wrong")))
                .isInstanceOf(BusinessException.class);
        verify(passwords).matches("wrong", expected.getPasswordHash());
        verifyNoInteractions(jwt, refresh);
    }

    @Test
    void emailAliasUsesTheSameUserAndFailureCounter() {
        User user = user(5L, AccountStatus.ACTIVE);
        when(users.findByEmailIgnoreCaseAndDeletedFalse("customer@example.com"))
                .thenReturn(Optional.of(user));
        invalidPassword(" Customer@Example.COM ", user);
        assertThat(user.getFailedLoginCount()).isEqualTo(1);
        verify(users).save(user);
    }

    @Test
    void internationalInputFindsLocalStoredPhone() {
        User user = user(5L, AccountStatus.ACTIVE);
        when(users.findByPhoneAndDeletedFalse("0912345678")).thenReturn(Optional.of(user));
        invalidPassword("+84 912-345-678", user);
    }

    @Test
    void ambiguousIdentifierNeverAuthenticatesAnArbitraryAccount() {
        when(users.findByUsernameIgnoreCaseAndDeletedFalse("0912345678"))
                .thenReturn(Optional.of(user(3L, AccountStatus.ACTIVE)));
        when(users.findByPhoneAndDeletedFalse("0912345678"))
                .thenReturn(Optional.of(user(5L, AccountStatus.ACTIVE)));
        assertThatThrownBy(() -> service.login(new LoginRequest("0912345678", "secret")))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(passwords, jwt, refresh);
    }

    @Test
    void wrongPasswordsCannotPromotePendingAccountThroughTemporaryLock() {
        User user = user(5L, AccountStatus.PENDING_ACTIVATION);
        user.setFailedLoginCount(4);
        when(users.findByUsernameIgnoreCaseAndDeletedFalse("customer"))
                .thenReturn(Optional.of(user));
        invalidPassword("customer", user);
        assertThat(user.getAccountStatus()).isEqualTo(AccountStatus.PENDING_ACTIVATION);
        assertThat(user.getLockedUntil()).isNull();
    }

    @Test
    void activeAccountStillLocksAfterFiveFailures() {
        User user = user(5L, AccountStatus.ACTIVE);
        user.setFailedLoginCount(4);
        when(users.findByUsernameIgnoreCaseAndDeletedFalse("customer"))
                .thenReturn(Optional.of(user));
        invalidPassword("customer", user);
        assertThat(user.getAccountStatus()).isEqualTo(AccountStatus.LOCKED);
        assertThat(user.getLockedUntil()).isNotNull();
    }
}
