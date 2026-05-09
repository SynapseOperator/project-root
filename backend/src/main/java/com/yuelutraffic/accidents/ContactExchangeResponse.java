package com.yuelutraffic.accidents;

import java.util.List;
import java.util.UUID;

public record ContactExchangeResponse(
        UUID id,
        UUID accidentId,
        ContactExchangeStatus status,
        String requestingUserPublicCode,
        String targetUserPublicCode,
        List<VisibleContact> visibleContacts) {

    public record VisibleContact(String publicCode, ContactType contactType, String contactValue) {
    }
}
