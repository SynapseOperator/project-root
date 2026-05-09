package com.yuelutraffic.accidents;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccidentPostRepository extends JpaRepository<AccidentPost, UUID> {

    List<AccidentPost> findByStatusAndLatitudeBetweenAndLongitudeBetweenOrderByOccurredAtDesc(
            AccidentStatus status,
            double minLatitude,
            double maxLatitude,
            double minLongitude,
            double maxLongitude);
}
