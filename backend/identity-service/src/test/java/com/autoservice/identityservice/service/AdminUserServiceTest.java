package com.autoservice.identityservice.service;

import com.autoservice.identityservice.domain.entity.User;
import com.autoservice.identityservice.domain.enums.AccountStatus;
import com.autoservice.identityservice.domain.enums.Role;
import com.autoservice.identityservice.dto.request.UpdateAccountStatusRequest;
import com.autoservice.identityservice.dto.response.AdminUserResponse;
import com.autoservice.identityservice.dto.response.PageResponse;
import com.autoservice.identityservice.exception.BusinessException;
import com.autoservice.identityservice.exception.ErrorCode;
import com.autoservice.identityservice.exception.ResourceNotFoundException;
import com.autoservice.identityservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AdminUserService adminUserService;

    private User customer;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setId(1L);
        customer.setUsername("thuy.my");
        customer.setFullName("Pham Thi Thuy My");
        customer.setPhone("+84912345678");
        customer.setEmail("thuy.my.test@example.com");
        customer.setRole(Role.CUSTOMER);
        customer.setAccountStatus(AccountStatus.ACTIVE);
        customer.setFailedLoginCount(0);
        customer.setCreatedAt(Instant.now());
        customer.setUpdatedAt(Instant.now());
    }

    @Test
    void getUsersShouldReturnPagedUsers() {
        Pageable pageable =
                PageRequest.of(0, 20);

        when(userRepository.searchUsers(
                null,
                Role.CUSTOMER,
                AccountStatus.ACTIVE,
                pageable
        )).thenReturn(
                new PageImpl<>(
                        List.of(customer),
                        pageable,
                        1
                )
        );

        PageResponse<AdminUserResponse> response =
                adminUserService.getUsers(
                        null,
                        Role.CUSTOMER,
                        AccountStatus.ACTIVE,
                        pageable
                );

        assertEquals(1, response.content().size());
        assertEquals(
                "thuy.my",
                response.content().get(0).username()
        );

        assertEquals(1, response.totalElements());
        assertTrue(response.first());
        assertTrue(response.last());
    }

    @Test
    void disableUserShouldRevokeAllRefreshTokens() {
        when(userRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(customer));

        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        AdminUserResponse response =
                adminUserService.updateAccountStatus(
                        2L,
                        1L,
                        new UpdateAccountStatusRequest(
                                AccountStatus.DISABLED
                        )
                );

        assertEquals(
                AccountStatus.DISABLED,
                response.accountStatus()
        );

        verify(refreshTokenService)
                .revokeAllForUser(1L);
    }

    @Test
    void activateUserShouldResetLoginFailures() {
        customer.setAccountStatus(AccountStatus.LOCKED);
        customer.setFailedLoginCount(5);
        customer.setLockedUntil(
                Instant.now().plusSeconds(900)
        );

        when(userRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(customer));

        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        AdminUserResponse response =
                adminUserService.updateAccountStatus(
                        2L,
                        1L,
                        new UpdateAccountStatusRequest(
                                AccountStatus.ACTIVE
                        )
                );

        assertEquals(
                AccountStatus.ACTIVE,
                response.accountStatus()
        );

        assertEquals(
                0,
                response.failedLoginCount()
        );

        assertNull(response.lockedUntil());

        verify(refreshTokenService, never())
                .revokeAllForUser(any());
    }

    @Test
    void adminShouldNotUpdateOwnAccountStatus() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () ->
                                adminUserService
                                        .updateAccountStatus(
                                                2L,
                                                2L,
                                                new UpdateAccountStatusRequest(
                                                        AccountStatus.DISABLED
                                                )
                                        )
                );

        assertEquals(
                ErrorCode.CANNOT_UPDATE_OWN_ACCOUNT,
                exception.getErrorCode()
        );

        verify(userRepository, never())
                .findByIdAndDeletedFalse(any());
    }

    @Test
    void updateMissingUserShouldThrowNotFound() {
        when(userRepository.findByIdAndDeletedFalse(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () ->
                                adminUserService
                                        .updateAccountStatus(
                                                2L,
                                                999L,
                                                new UpdateAccountStatusRequest(
                                                        AccountStatus.ACTIVE
                                                )
                                        )
                );

        assertEquals(
                ErrorCode.USER_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(refreshTokenService, never())
                .revokeAllForUser(any());
    }

    @Test
    void unchangedStatusShouldNotSaveOrRevokeTokens() {
        when(userRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(customer));

        AdminUserResponse response =
                adminUserService.updateAccountStatus(
                        2L,
                        1L,
                        new UpdateAccountStatusRequest(
                                AccountStatus.ACTIVE
                        )
                );

        assertEquals(
                AccountStatus.ACTIVE,
                response.accountStatus()
        );

        assertFalse(response.failedLoginCount() > 0);

        verify(userRepository, never())
                .saveAndFlush(any());

        verify(refreshTokenService, never())
                .revokeAllForUser(any());
    }
}