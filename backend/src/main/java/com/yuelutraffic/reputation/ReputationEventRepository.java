package com.yuelutraffic.reputation;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReputationEventRepository extends JpaRepository<ReputationEvent, UUID> {
}
