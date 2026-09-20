package com.autoservice.identityservice.service;

import com.autoservice.identityservice.domain.entity.RefreshToken;
import com.autoservice.identityservice.domain.entity.User;
import com.autoservice.identityservice.domain.enums.AccountStatus;
import com.autoservice.identityservice.domain.enums.Role;
import com.autoservice.identityservice.dto.request.LoginRequest;
import com.autoservice.identityservice.dto.request.RefreshTokenRequest;
import com.autoservice.identityservice.dto.response.AuthTokenResponse;
import com.autoservice.identityservice.exception.BusinessException;
import com.autoservice.identityservice.exception.ErrorCode;
import com.autoservice.identityservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthLoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_WithActiveAccount_ShouldReturnTokenPair() {
        User user = createActiveUser();

        LoginRequest request = new LoginRequest(
                "THUY.MY",
                "MyPassword123"
        );

        when(userRepository
                .findByUsernameIgnoreCaseAndDeletedFalse(
                        "thuy.my"
                ))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "MyPassword123",
                user.getPasswordHash()
        )).thenReturn(true);

        when(jwtService.generateAccessToken(user))
                .thenReturn("access-token");

        when(jwtService.getAccessTokenExpiresInSeconds())
                .thenReturn(900L);

        when(refreshTokenService.issue(user))
                .thenReturn(
                        new RefreshTokenService.IssuedRefreshToken(
                                "refresh-token",
                                Instant.now().plusSeconds(604800)
                        )
                );

        AuthTokenResponse response =
                authService.login(request);

        assertEquals(
                "access-token",
                response.accessToken()
        );

        assertEquals(
                "refresh-token",
                response.refreshToken()
        );

        assertEquals("Bearer", response.tokenType());
        assertEquals(900L, response.expiresInSeconds());
        assertEquals("thuy.my", response.user().username());
        assertEquals(Role.CUSTOMER, response.user().role());

        verify(userRepository).save(user);
        verify(refreshTokenService).issue(user);
    }

    @Test
    void login_WithWrongPassword_ShouldIncreaseFailedCount() {
        User user = createActiveUser();

        LoginRequest request = new LoginRequest(
                "thuy.my",
                "WrongPassword123"
        );

        when(userRepository
                .findByUsernameIgnoreCaseAndDeletedFalse(
                        "thuy.my"
                ))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "WrongPassword123",
                user.getPasswordHash()
        )).thenReturn(false);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                ErrorCode.INVALID_CREDENTIALS,
                exception.getErrorCode()
        );

        assertEquals(1, user.getFailedLoginCount());

        verify(userRepository).save(user);
        verifyNoInteractions(
                jwtService,
                refreshTokenService
        );
    }

    @Test
    void login_OnFifthFailure_ShouldLockAccount() {
        User user = createActiveUser();
        user.setFailedLoginCount(4);

        LoginRequest request = new LoginRequest(
                "thuy.my",
                "WrongPassword123"
        );

        when(userRepository
                .findByUsernameIgnoreCaseAndDeletedFalse(
                        "thuy.my"
                ))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "WrongPassword123",
                user.getPasswordHash()
        )).thenReturn(false);

        assertThrows(
                BusinessException.class,
                () -> authService.login(request)
        );

        assertEquals(5, user.getFailedLoginCount());

        assertEquals(
                AccountStatus.LOCKED,
                user.getAccountStatus()
        );

        assertNotNull(user.getLockedUntil());

        verify(userRepository).save(user);
        verifyNoInteractions(
                jwtService,
                refreshTokenService
        );
    }

    @Test
    void login_WithPendingAccount_ShouldRejectLogin() {
        User user = createActiveUser();
        user.setAccountStatus(
                AccountStatus.PENDING_ACTIVATION
        );

        LoginRequest request = new LoginRequest(
                "thuy.my",
                "MyPassword123"
        );

        when(userRepository
                .findByUsernameIgnoreCaseAndDeletedFalse(
                        "thuy.my"
                ))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "MyPassword123",
                user.getPasswordHash()
        )).thenReturn(true);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                ErrorCode.ACCOUNT_PENDING_ACTIVATION,
                exception.getErrorCode()
        );

        verify(userRepository, never()).save(user);

        verifyNoInteractions(
                jwtService,
                refreshTokenService
        );
    }

    @Test
    void refresh_WithUsableToken_ShouldRotateTokenPair() {
        User user = createActiveUser();

        RefreshToken currentRefreshToken =
                RefreshToken.builder()
                        .id(10L)
                        .user(user)
                        .tokenHash("old-token-hash")
                        .expiresAt(
                                Instant.now().plusSeconds(3600)
                        )
                        .build();

        when(refreshTokenService.requireUsable(
                "old-refresh-token"
        )).thenReturn(currentRefreshToken);

        when(jwtService.generateAccessToken(user))
                .thenReturn("new-access-token");

        when(jwtService.getAccessTokenExpiresInSeconds())
                .thenReturn(900L);

        when(refreshTokenService.issue(user))
                .thenReturn(
                        new RefreshTokenService.IssuedRefreshToken(
                                "new-refresh-token",
                                Instant.now().plusSeconds(604800)
                        )
                );

        AuthTokenResponse response =
                authService.refresh(
                        new RefreshTokenRequest(
                                "old-refresh-token"
                        )
                );

        assertEquals(
                "new-access-token",
                response.accessToken()
        );

        assertEquals(
                "new-refresh-token",
                response.refreshToken()
        );

        verify(refreshTokenService)
                .revoke(currentRefreshToken);

        verify(refreshTokenService).issue(user);
    }

    @Test
    void logout_ShouldRevokeProvidedRefreshToken() {
        RefreshTokenRequest request =
                new RefreshTokenRequest(
                        "refresh-token"
                );

        authService.logout(request);

        verify(refreshTokenService)
                .revoke("refresh-token");
    }

    private User createActiveUser() {
        return User.builder()
                .id(1L)
                .username("thuy.my")
                .passwordHash("$2a$10$encodedPassword")
                .fullName("Pham Thi Thuy My")
                .phone("+84912345678")
                .email("thuy.my.test@example.com")
                .role(Role.CUSTOMER)
                .accountStatus(AccountStatus.ACTIVE)
                .failedLoginCount(0)
                .deleted(false)
                .createdAt(Instant.now())
                .build();
    }
}