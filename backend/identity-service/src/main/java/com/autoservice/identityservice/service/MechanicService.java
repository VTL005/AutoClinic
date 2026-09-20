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
import com.autoservice.identityservice.dto.response.PageResponse;
import com.autoservice.identityservice.exception.BusinessException;
import com.autoservice.identityservice.exception.DuplicateResourceException;
import com.autoservice.identityservice.exception.ErrorCode;
import com.autoservice.identityservice.exception.ResourceNotFoundException;
import com.autoservice.identityservice.repository.MechanicProfileRepository;
import com.autoservice.identityservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MechanicService {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;
    private final MechanicProfileRepository
            mechanicProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MechanicResponse createMechanic(
            CreateMechanicRequest request
    ) {
        String username =
                normalizeUsername(request.username());

        String phone = request.phone().trim();

        String email =
                normalizeEmail(request.email());

        validateUniqueInformation(
                username,
                phone,
                email
        );

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        user.setFullName(request.fullName().trim());
        user.setPhone(phone);
        user.setEmail(email);
        user.setRole(Role.MECHANIC);
        user.setAccountStatus(AccountStatus.ACTIVE);

        User savedUser =
                userRepository.saveAndFlush(user);

        MechanicProfile profile =
                new MechanicProfile();

        profile.setUser(savedUser);
        profile.setSkillLevel(request.skillLevel());
        profile.setSpecialization(
                normalizeOptionalText(
                        request.specialization()
                )
        );
        profile.setHourlyRate(request.hourlyRate());
        profile.setEmploymentStatus(
                EmploymentStatus.ACTIVE
        );

        MechanicProfile savedProfile =
                mechanicProfileRepository
                        .saveAndFlush(profile);

        return toResponse(savedProfile);
    }

    @Transactional(readOnly = true)
    public PageResponse<MechanicResponse> getMechanics(
            String keyword,
            SkillLevel skillLevel,
            EmploymentStatus employmentStatus,
            Pageable pageable
    ) {
        validatePageSize(pageable);

        Page<MechanicResponse> result =
                mechanicProfileRepository
                        .searchMechanics(
                                normalizeOptionalText(keyword),
                                skillLevel,
                                employmentStatus,
                                pageable
                        )
                        .map(this::toResponse);

        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public MechanicResponse getMechanic(
            Long profileId
    ) {
        MechanicProfile profile =
                requireProfile(profileId);

        return toResponse(profile);
    }

    @Transactional
    public MechanicResponse updateMechanic(
            Long profileId,
            UpdateMechanicProfileRequest request
    ) {
        MechanicProfile profile =
                requireProfile(profileId);

        profile.setSkillLevel(request.skillLevel());
        profile.setSpecialization(
                normalizeOptionalText(
                        request.specialization()
                )
        );
        profile.setHourlyRate(request.hourlyRate());
        profile.setEmploymentStatus(
                request.employmentStatus()
        );

        MechanicProfile savedProfile =
                mechanicProfileRepository
                        .saveAndFlush(profile);

        return toResponse(savedProfile);
    }

    private MechanicProfile requireProfile(
            Long profileId
    ) {
        return mechanicProfileRepository
                .findDetailedById(profileId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ErrorCode.MECHANIC_PROFILE_NOT_FOUND,
                                "Không tìm thấy hồ sơ kỹ thuật viên."
                        )
                );
    }

    private void validateUniqueInformation(
            String username,
            String phone,
            String email
    ) {
        if (userRepository
                .existsByUsernameIgnoreCase(username)) {
            throw new DuplicateResourceException(
                    ErrorCode.USERNAME_ALREADY_EXISTS,
                    "Tên đăng nhập đã tồn tại."
            );
        }

        if (userRepository.existsByPhone(phone)) {
            throw new DuplicateResourceException(
                    ErrorCode.PHONE_ALREADY_EXISTS,
                    "Số điện thoại đã được sử dụng."
            );
        }

        if (email != null
                && userRepository
                .existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException(
                    ErrorCode.EMAIL_ALREADY_EXISTS,
                    "Email đã được sử dụng."
            );
        }
    }

    private void validatePageSize(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "Số phần tử mỗi trang không được vượt quá 100."
            );
        }
    }

    private String normalizeUsername(String username) {
        return username
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private MechanicResponse toResponse(
            MechanicProfile profile
    ) {
        User user = profile.getUser();

        return new MechanicResponse(
                profile.getId(),
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getPhone(),
                user.getEmail(),
                user.getAccountStatus(),
                profile.getSkillLevel(),
                profile.getSpecialization(),
                profile.getHourlyRate(),
                profile.getEmploymentStatus(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}