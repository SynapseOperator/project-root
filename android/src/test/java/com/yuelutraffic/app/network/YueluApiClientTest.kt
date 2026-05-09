package com.yuelutraffic.app.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import com.yuelutraffic.app.traffic.FeedbackChoice
import com.yuelutraffic.app.traffic.TrafficReportStatus
import com.yuelutraffic.app.traffic.TrafficReportType
import com.yuelutraffic.app.accidents.AccidentPostStatus
import com.yuelutraffic.app.accidents.ContactExchangeStatus

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

    @Test
    fun reportRequestBodiesMatchBackendContract() {
        val createBody = createReportRequestBody(
            type = TrafficReportType.CONGESTION,
            locationLabel = " 麓山南路\"校门 ",
            description = " 车流缓慢\\请谨慎通行 ",
        )

        assertTrue(createBody.contains("\"type\":\"CONGESTION\""))
        assertTrue(createBody.contains("\"locationLabel\":\"麓山南路\\\"校门\""))
        assertTrue(createBody.contains("\"description\":\"车流缓慢\\\\请谨慎通行\""))
        assertEquals("{\"feedbackType\":\"MARK_EXPIRED\"}", feedbackRequestBody(FeedbackChoice.MARK_EXPIRED))
    }

    @Test
    fun reportParserReadsBackendReportShape() {
        val reports = parseTrafficReports(
            """
            [
              {
                "id": "22222222-2222-2222-2222-222222222222",
                "type": "CONSTRUCTION",
                "latitude": 28.17,
                "longitude": 112.93,
                "locationLabel": "麓山南路",
                "description": "施工占道",
                "submittedAt": "2026-05-09T08:00:00Z",
                "defaultExpiresAt": "2026-05-09T20:00:00Z",
                "status": "ACTIVE",
                "initialCredibility": 50,
                "confidenceScore": 58
              }
            ]
            """.trimIndent(),
        )

        assertEquals(1, reports.size)
        assertEquals(TrafficReportType.CONSTRUCTION, reports.first().type)
        assertEquals(TrafficReportStatus.ACTIVE, reports.first().status)
        assertEquals(58, reports.first().confidenceScore)
    }

    @Test
    fun accidentRequestBodiesMatchBackendContract() {
        val createBody = createAccidentRequestBody(
            locationLabel = " 麓山南路 ",
            description = " 轻微事故\\等待确认 ",
        )

        assertTrue(createBody.contains("\"locationLabel\":\"麓山南路\""))
        assertTrue(createBody.contains("\"description\":\"轻微事故\\\\等待确认\""))
        assertEquals(
            "{\"contactType\":\"WECHAT\",\"contactValue\":\"wx-123\"}",
            contactOfferRequestBody(" wx-123 "),
        )
    }

    @Test
    fun accidentAndContactParsersReadBackendShape() {
        val accidents = parseAccidents(
            """
            [
              {
                "id": "33333333-3333-3333-3333-333333333333",
                "latitude": 28.17,
                "longitude": 112.93,
                "locationLabel": "麓山南路",
                "occurredAt": "2026-05-09T08:00:00Z",
                "description": "轻微事故",
                "status": "OPEN"
              }
            ]
            """.trimIndent(),
        )
        val exchange = parseContactExchange(
            """
            {
              "id": "44444444-4444-4444-4444-444444444444",
              "accidentId": "33333333-3333-3333-3333-333333333333",
              "status": "MUTUALLY_CONFIRMED",
              "visibleContacts": [
                {"publicCode":"User-A","contactType":"WECHAT","contactValue":"wx-a"}
              ]
            }
            """.trimIndent(),
        )

        assertEquals(AccidentPostStatus.OPEN, accidents.first().status)
        assertEquals(ContactExchangeStatus.MUTUALLY_CONFIRMED, exchange.status)
        assertEquals(1, exchange.visibleContacts.size)
    }
}
