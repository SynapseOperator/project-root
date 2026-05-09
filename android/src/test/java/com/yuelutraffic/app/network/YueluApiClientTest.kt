package com.yuelutraffic.app.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class YueluApiClientTest {

    @Test
    fun studentLoginBodyTrimsAndEscapesInput() {
        val body = studentLoginRequestBody("  2026\"001\\A  ", privacyAcknowledged = true)

        assertEquals(
            "{\"studentNumber\":\"2026\\\"001\\\\A\",\"privacyAcknowledged\":true}",
            body,
        )
    }

    @Test
    fun authParserReadsBackendSessionShape() {
        val session = parseAuthSession(
            """
            {
              "accessToken": "token-1",
              "newUser": true,
              "privacyNotice": "backend notice",
              "user": {
                "id": "11111111-1111-1111-1111-111111111111",
                "publicCode": "User-ABC123",
                "role": "ADMIN",
                "reputationScore": 70,
                "points": 12,
                "titleCode": "ROAD_HELPER",
                "postingBanUntil": null
              }
            }
            """.trimIndent(),
        )

        assertEquals("token-1", session.accessToken)
        assertEquals("User-ABC123", session.user.publicCode)
        assertEquals("ADMIN", session.user.role)
        assertEquals(70, session.user.reputationScore)
        assertTrue(session.newUser)
    }
}
