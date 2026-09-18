package com.autoservice.identityservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoservice.identityservice.domain.entity.MechanicProfile;
import com.autoservice.identityservice.domain.enums.EmploymentStatus;
import com.autoservice.identityservice.domain.enums.SkillLevel;

@Repository
public interface MechanicProfileRepository
        extends JpaRepository<MechanicProfile, Long> {

    Optional<MechanicProfile> findByUserId(
            Long userId
    );

    boolean existsByUserId(
            Long userId
    );

    List<MechanicProfile> findAllBySkillLevelAndEmploymentStatus(
            SkillLevel skillLevel,
            EmploymentStatus employmentStatus
    );

    List<MechanicProfile> findAllByEmploymentStatus(
            EmploymentStatus employmentStatus
    );
}