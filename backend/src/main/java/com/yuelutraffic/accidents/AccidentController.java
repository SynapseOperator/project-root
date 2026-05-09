package com.yuelutraffic.accidents;

import com.yuelutraffic.auth.AuthService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AccidentController {

    private final AuthService authService;
    private final AccidentService accidentService;

    public AccidentController(AuthService authService, AccidentService accidentService) {
        this.authService = authService;
        this.accidentService = accidentService;
    }

    @GetMapping("/accidents")
    public List<AccidentResponse> list(
            @RequestParam double minLat,
            @RequestParam double minLng,
            @RequestParam double maxLat,
            @RequestParam double maxLng,
            @RequestParam(required = false) AccidentStatus status) {
        return accidentService.list(minLat, minLng, maxLat, maxLng, status);
    }

    @PostMapping("/accidents")
    public AccidentResponse create(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody CreateAccidentRequest request) {
        return accidentService.create(authService.requireUser(authorizationHeader), request);
    }

    @GetMapping("/accidents/{accidentId}")
    public AccidentResponse detail(@PathVariable UUID accidentId) {
        return accidentService.detail(accidentId);
    }

    @PostMapping("/accidents/{accidentId}/contact-requests")
    public ContactExchangeResponse createExchange(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID accidentId,
            @Valid @RequestBody ContactOfferRequest request) {
        return accidentService.createExchange(authService.requireUser(authorizationHeader), accidentId, request);
    }

    @PostMapping("/contact-requests/{requestId}/confirm")
    public ContactExchangeResponse confirmExchange(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID requestId,
            @Valid @RequestBody ContactOfferRequest request) {
        return accidentService.confirmExchange(authService.requireUser(authorizationHeader), requestId, request);
    }

    @GetMapping("/contact-requests/{requestId}")
    public ContactExchangeResponse getExchange(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID requestId) {
        return accidentService.getExchange(authService.requireUser(authorizationHeader), requestId);
    }
}
