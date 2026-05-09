package com.yuelutraffic.accidents;

import com.yuelutraffic.user.AppUser;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccidentContactOfferRepository extends JpaRepository<AccidentContactOffer, UUID> {

    boolean existsByExchangeRequestAndUser(ContactExchangeRequest exchangeRequest, AppUser user);

    List<AccidentContactOffer> findByExchangeRequest(ContactExchangeRequest exchangeRequest);
}
