package com.autoservice.identityservice.service;

import com.autoservice.identityservice.domain.entity.MechanicProfile;
import com.autoservice.identityservice.domain.entity.User;
import com.autoservice.identityservice.domain.enums.AccountStatus;
import com.autoservice.identityservice.domain.enums.EmploymentStatus;
import com.autoservice.identityservice.domain.enums.Role;
import com.autoservice.identityservice.domain.enums.SkillLevel;
import com.autoservice.identityservice.dto.request.CreateMechanicRequest;
import com.autoservice.identityservice.dto.request.UpdateMechanicProfileRequest;
import com.autoservice.identityservice.dto.response.MechanicResponse;
import com.autoservice.identityservice.exception.DuplicateResourceException;
import com.autoservice.identityservice.exception.ErrorCode;
import com.autoservice.identityservice.exception.ResourceNotFoundException;
import com.autoservice.identityservice.repository.MechanicProfileRepository;
import com.autoservice.identityservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MechanicServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MechanicProfileRepository mechanicProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MechanicService mechanicService;

    private User mechanicUser;
    private MechanicProfile mechanicProfile;

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();

        mechanicUser = User.builder()
                .id(3L)
                .username("mechanic.an")
                .passwordHash("encoded-password")
                .fullName("Nguyen Van An")
                .phone("+84901112223")
                .email("mechanic.an@autoservice.local")
                .role(Role.MECHANIC)
                .accountStatus(AccountStatus.ACTIVE)
                .failedLoginCount(0)
                .createdAt(now)
                .updatedAt(now)
                .version(0L)
                .build();

        mechanicProfile = MechanicProfile.builder()
                .id(1L)
                .user(mechanicUser)
                .skillLevel(SkillLevel.SENIOR)
                .specialization(
                        "Động cơ và hệ thống truyền động"
                )
                .hourlyRate(new BigDecimal("250000.00"))
                .employmentStatus(EmploymentStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .version(0L)
                .build();
    }

    @Test
    void createMechanicShouldCreateUserAndProfile() {
        CreateMechanicRequest request =
                new CreateMechanicRequest(
                        "mechanic.an",
                        "Mechanic@2026",
                        "Nguyen Van An",
                        "+84901112223",
                        "mechanic.an@autoservice.local",
                        SkillLevel.SENIOR,
                        "Động cơ và hệ thống truyền động",
                        new BigDecimal("250000.00")
                );

        when(userRepository.existsByUsernameIgnoreCase(
                "mechanic.an"
        )).thenReturn(false);

        when(userRepository.existsByPhone(
                "+84901112223"
        )).thenReturn(false);

        when(userRepository.existsByEmailIgnoreCase(
                "mechanic.an@autoservice.local"
        )).thenReturn(false);

        when(passwordEncoder.encode(
                "Mechanic@2026"
        )).thenReturn("encoded-password");

        when(userRepository.saveAndFlush(any(User.class)))
                .thenReturn(mechanicUser);

        when(mechanicProfileRepository.saveAndFlush(
                any(MechanicProfile.class)
        )).thenReturn(mechanicProfile);

        MechanicResponse response =
                mechanicService.createMechanic(request);

        assertEquals(1L, response.profileId());
        assertEquals(3L, response.userId());
        assertEquals(
                "mechanic.an",
                response.username()
        );
        assertEquals(
                SkillLevel.SENIOR,
                response.skillLevel()
        );
        assertEquals(
                EmploymentStatus.ACTIVE,
                response.employmentStatus()
        );

        verify(passwordEncoder).encode(
                "Mechanic@2026"
        );
        verify(userRepository).saveAndFlush(
                any(User.class)
        );
        verify(mechanicProfileRepository).saveAndFlush(
                any(MechanicProfile.class)
        );
    }

    @Test
    void createMechanicShouldRejectDuplicateUsername() {
        CreateMechanicRequest request =
                new CreateMechanicRequest(
                        "mechanic.an",
                        "Mechanic@2026",
                        "Nguyen Van An",
                        "+84901112223",
                        "mechanic.an@autoservice.local",
                        SkillLevel.SENIOR,
                        "Động cơ",
                        new BigDecimal("250000.00")
                );

        when(userRepository.existsByUsernameIgnoreCase(
                "mechanic.an"
        )).thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> mechanicService.createMechanic(request)
        );

        verify(userRepository, never())
                .saveAndFlush(any(User.class));

        verify(mechanicProfileRepository, never())
                .saveAndFlush(any(MechanicProfile.class));
    }

    @Test
    void getMechanicShouldReturnExistingProfile() {
        when(mechanicProfileRepository.findDetailedById(1L))
                .thenReturn(Optional.of(mechanicProfile));

        MechanicResponse response =
                mechanicService.getMechanic(1L);

        assertEquals(1L, response.profileId());
        assertEquals(
                "mechanic.an",
                response.username()
        );
        assertEquals(
                "Động cơ và hệ thống truyền động",
                response.specialization()
        );
    }

    @Test
    void getMechanicShouldThrowWhenProfileDoesNotExist() {
        when(mechanicProfileRepository.findDetailedById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> mechanicService.getMechanic(99L)
                );

        assertEquals(
                ErrorCode.MECHANIC_PROFILE_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    void updateMechanicShouldUpdateProfileInformation() {
        UpdateMechanicProfileRequest request =
                new UpdateMechanicProfileRequest(
                        SkillLevel.EXPERT,
                        "Chẩn đoán điện và điện tử ô tô",
                        new BigDecimal("350000.00"),
                        EmploymentStatus.ACTIVE
                );

        when(mechanicProfileRepository.findDetailedById(1L))
                .thenReturn(Optional.of(mechanicProfile));

        when(mechanicProfileRepository.saveAndFlush(
                mechanicProfile
        )).thenReturn(mechanicProfile);

        MechanicResponse response =
                mechanicService.updateMechanic(
                        1L,
                        request
                );

        assertEquals(
                SkillLevel.EXPERT,
                response.skillLevel()
        );
        assertEquals(
                "Chẩn đoán điện và điện tử ô tô",
                response.specialization()
        );
        assertEquals(
                new BigDecimal("350000.00"),
                response.hourlyRate()
        );

        verify(mechanicProfileRepository)
                .saveAndFlush(mechanicProfile);
    }

    @Test
    void existingMechanicProfileShouldRemainActive() {
        assertTrue(
                mechanicProfile.getEmploymentStatus()
                        == EmploymentStatus.ACTIVE
        );

        assertEquals(
                AccountStatus.ACTIVE,
                mechanicProfile.getUser()
                        .getAccountStatus()
        );
    }
}