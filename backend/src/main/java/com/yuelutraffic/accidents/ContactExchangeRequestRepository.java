package com.yuelutraffic.accidents;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactExchangeRequestRepository extends JpaRepository<ContactExchangeRequest, UUID> {
}
