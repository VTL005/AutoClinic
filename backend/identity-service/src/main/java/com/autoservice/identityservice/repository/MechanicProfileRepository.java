package com.autoservice.identityservice.repository;

import com.autoservice.identityservice.domain.entity.MechanicProfile;
import com.autoservice.identityservice.domain.enums.EmploymentStatus;
import com.autoservice.identityservice.domain.enums.SkillLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MechanicProfileRepository
        extends JpaRepository<MechanicProfile, Long> {

    Optional<MechanicProfile> findByUserId(
            Long userId
    );

    boolean existsByUserId(
            Long userId
    );

    List<MechanicProfile>
    findAllBySkillLevelAndEmploymentStatus(
            SkillLevel skillLevel,
            EmploymentStatus employmentStatus
    );

    List<MechanicProfile> findAllByEmploymentStatus(
            EmploymentStatus employmentStatus
    );
    @Query("""
        SELECT profile
        FROM MechanicProfile profile
        JOIN FETCH profile.user user
        WHERE user.id = :userId
          AND user.deleted = false
        """)
    Optional<MechanicProfile> findDetailedByUserId(
            @Param("userId") Long userId
    );
    @Query("""
            SELECT profile
            FROM MechanicProfile profile
            JOIN FETCH profile.user user
            WHERE profile.id = :profileId
              AND user.deleted = false
            """)
    Optional<MechanicProfile> findDetailedById(
            @Param("profileId") Long profileId
    );

    @Query(
            value = """
                    SELECT profile
                    FROM MechanicProfile profile
                    JOIN FETCH profile.user user
                    WHERE user.deleted = false
                      AND (
                            :keyword IS NULL
                            OR LOWER(user.username)
                                LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(user.fullName)
                                LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(
                                COALESCE(profile.specialization, '')
                            )
                                LIKE LOWER(CONCAT('%', :keyword, '%'))
                      )
                      AND (
                            :skillLevel IS NULL
                            OR profile.skillLevel = :skillLevel
                      )
                      AND (
                            :employmentStatus IS NULL
                            OR profile.employmentStatus =
                                :employmentStatus
                      )
                    """,
            countQuery = """
                    SELECT COUNT(profile)
                    FROM MechanicProfile profile
                    JOIN profile.user user
                    WHERE user.deleted = false
                      AND (
                            :keyword IS NULL
                            OR LOWER(user.username)
                                LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(user.fullName)
                                LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(
                                COALESCE(profile.specialization, '')
                            )
                                LIKE LOWER(CONCAT('%', :keyword, '%'))
                      )
                      AND (
                            :skillLevel IS NULL
                            OR profile.skillLevel = :skillLevel
                      )
                      AND (
                            :employmentStatus IS NULL
                            OR profile.employmentStatus =
                                :employmentStatus
                      )
                    """
    )
    Page<MechanicProfile> searchMechanics(
            @Param("keyword") String keyword,
            @Param("skillLevel") SkillLevel skillLevel,
            @Param("employmentStatus")
            EmploymentStatus employmentStatus,
            Pageable pageable
    );
}