package com.yuelutraffic.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByStudentNumberHash(String studentNumberHash);

    boolean existsByPublicCode(String publicCode);

    List<AppUser> findTop50ByOrderByPointsDescReputationScoreDescCreatedAtAsc();
}
