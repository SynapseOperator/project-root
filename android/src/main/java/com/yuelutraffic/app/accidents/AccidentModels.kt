package com.yuelutraffic.app.accidents

import java.time.Instant
import java.util.UUID

enum class AccidentPostStatus {
    OPEN,
    MATCHED,
    HIDDEN,
}

enum class ContactExchangeStatus {
    NONE,
    PENDING,
    MUTUALLY_CONFIRMED,
}

data class AccidentPostUi(
    val id: String = UUID.randomUUID().toString(),
    val locationLabel: String,
    val description: String,
    val occurredAt: Instant,
    val status: AccidentPostStatus = AccidentPostStatus.OPEN,
    val contactExchangeStatus: ContactExchangeStatus = ContactExchangeStatus.NONE,
    val contactRequestId: String? = null,
    val visibleContacts: List<String> = emptyList(),
)

fun createAccidentPost(
    locationLabel: String,
    description: String,
    now: Instant = Instant.now(),
): AccidentPostUi {
    return AccidentPostUi(
        locationLabel = locationLabel.ifBlank { "麓山南路" },
        description = description.trim(),
        occurredAt = now,
    )
}

fun AccidentPostUi.requestContact(requestId: String? = contactRequestId): AccidentPostUi {
    return copy(
        contactExchangeStatus = ContactExchangeStatus.PENDING,
        contactRequestId = requestId,
        visibleContacts = emptyList(),
    )
}

fun AccidentPostUi.confirmContact(otherSideContact: String): AccidentPostUi {
    return copy(
        status = AccidentPostStatus.MATCHED,
        contactExchangeStatus = ContactExchangeStatus.MUTUALLY_CONFIRMED,
        visibleContacts = listOf("我方已保存联系方式", otherSideContact),
    )
}

fun sampleAccidentPosts(now: Instant = Instant.now()): List<AccidentPostUi> = listOf(
    createAccidentPost(
        locationLabel = "麓山南路中南大学门口附近",
        description = "轻微事故，联系方式需双方确认后显示。",
        now = now,
    ),
)
