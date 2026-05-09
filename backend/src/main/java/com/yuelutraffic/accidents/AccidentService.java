package com.yuelutraffic.accidents;

import com.yuelutraffic.common.ApiException;
import com.yuelutraffic.location.LocationService;
import com.yuelutraffic.user.AppUser;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccidentService {

    private final AccidentPostRepository accidents;
    private final ContactExchangeRequestRepository exchanges;
    private final AccidentContactOfferRepository offers;
    private final LocationService locationService;

    public AccidentService(
            AccidentPostRepository accidents,
            ContactExchangeRequestRepository exchanges,
            AccidentContactOfferRepository offers,
            LocationService locationService) {
        this.accidents = accidents;
        this.exchanges = exchanges;
        this.offers = offers;
        this.locationService = locationService;
    }

    @Transactional
    public AccidentResponse create(AppUser user, CreateAccidentRequest request) {
        if (user.isPostingBanned(Instant.now())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Posting is temporarily restricted for this user");
        }
        locationService.requireInsidePilotArea(request.latitude(), request.longitude());
        AccidentPost post = new AccidentPost(
                request.latitude(),
                request.longitude(),
                emptyToNull(request.locationLabel()),
                request.occurredAt() == null ? Instant.now() : request.occurredAt(),
                request.description().trim(),
                user);
        return AccidentResponse.from(accidents.save(post));
    }

    @Transactional(readOnly = true)
    public List<AccidentResponse> list(double minLat, double minLng, double maxLat, double maxLng, AccidentStatus status) {
        AccidentStatus requestedStatus = status == null ? AccidentStatus.OPEN : status;
        return accidents.findByStatusAndLatitudeBetweenAndLongitudeBetweenOrderByOccurredAtDesc(
                        requestedStatus,
                        Math.min(minLat, maxLat),
                        Math.max(minLat, maxLat),
                        Math.min(minLng, maxLng),
                        Math.max(minLng, maxLng))
                .stream()
                .map(AccidentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccidentResponse detail(UUID accidentId) {
        return AccidentResponse.from(requireAccident(accidentId));
    }

    @Transactional
    public ContactExchangeResponse createExchange(AppUser user, UUID accidentId, ContactOfferRequest request) {
        AccidentPost accident = requireAccident(accidentId);
        ContactExchangeRequest exchange = exchanges.save(new ContactExchangeRequest(accident, user));
        offers.save(new AccidentContactOffer(exchange, user, request.contactType(), protect(request.contactValue())));
        return toExchangeResponse(exchange, user);
    }

    @Transactional
    public ContactExchangeResponse confirmExchange(AppUser user, UUID requestId, ContactOfferRequest request) {
        ContactExchangeRequest exchange = requireExchange(requestId);
        if (exchange.getRequestingUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Waiting for the other involved user to confirm");
        }
        if (exchange.getTargetUser() != null && !exchange.getTargetUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Contact request already targets another user");
        }
        if (!offers.existsByExchangeRequestAndUser(exchange, user)) {
            offers.save(new AccidentContactOffer(exchange, user, request.contactType(), protect(request.contactValue())));
        }
        exchange.setTargetUser(user);
        exchange.setTargetConfirmedAt(Instant.now());
        exchange.setStatus(ContactExchangeStatus.MUTUALLY_CONFIRMED);
        exchange.getAccident().setStatus(AccidentStatus.MATCHED);
        return toExchangeResponse(exchange, user);
    }

    @Transactional(readOnly = true)
    public ContactExchangeResponse getExchange(AppUser user, UUID requestId) {
        return toExchangeResponse(requireExchange(requestId), user);
    }

    private ContactExchangeResponse toExchangeResponse(ContactExchangeRequest exchange, AppUser viewer) {
        boolean participant = exchange.getRequestingUser().getId().equals(viewer.getId())
                || (exchange.getTargetUser() != null && exchange.getTargetUser().getId().equals(viewer.getId()));
        if (!participant) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only exchange participants can inspect this contact request");
        }
        List<ContactExchangeResponse.VisibleContact> visibleContacts = List.of();
        if (exchange.getStatus() == ContactExchangeStatus.MUTUALLY_CONFIRMED) {
            visibleContacts = offers.findByExchangeRequest(exchange).stream()
                    .map(offer -> new ContactExchangeResponse.VisibleContact(
                            offer.getUser().getPublicCode(),
                            offer.getContactType(),
                            reveal(offer.getEncryptedContactValue())))
                    .toList();
        }
        return new ContactExchangeResponse(
                exchange.getId(),
                exchange.getAccident().getId(),
                exchange.getStatus(),
                exchange.getRequestingUser().getPublicCode(),
                exchange.getTargetUser() == null ? null : exchange.getTargetUser().getPublicCode(),
                visibleContacts);
    }

    private AccidentPost requireAccident(UUID accidentId) {
        return accidents.findById(accidentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Accident post not found"));
    }

    private ContactExchangeRequest requireExchange(UUID requestId) {
        return exchanges.findById(requestId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Contact exchange request not found"));
    }

    private String protect(String value) {
        return Base64.getEncoder().encodeToString(value.trim().getBytes(StandardCharsets.UTF_8));
    }

    private String reveal(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
