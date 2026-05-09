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
    val visibleContacts: List<String> = emptyList(),
)

fun createAccidentPost(
    locationLabel: String,
    description: String,
    now: Instant = Instant.now(),
): AccidentPostUi {
    return AccidentPostUi(
        locationLabel = locationLabel.ifBlank { "Lushan South Road" },
        description = description.trim(),
        occurredAt = now,
    )
}

fun AccidentPostUi.requestContact(): AccidentPostUi {
    return copy(contactExchangeStatus = ContactExchangeStatus.PENDING, visibleContacts = emptyList())
}

fun AccidentPostUi.confirmContact(otherSideContact: String): AccidentPostUi {
    return copy(
        status = AccidentPostStatus.MATCHED,
        contactExchangeStatus = ContactExchangeStatus.MUTUALLY_CONFIRMED,
        visibleContacts = listOf("Your saved contact", otherSideContact),
    )
}

fun sampleAccidentPosts(now: Instant = Instant.now()): List<AccidentPostUi> = listOf(
    createAccidentPost(
        locationLabel = "Near Lushan South Road campus gate",
        description = "Minor incident, contact hidden until both sides confirm.",
        now = now,
    ),
)
