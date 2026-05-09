package com.yuelutraffic.app.accidents

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccidentModelsTest {

    @Test
    fun contactStaysHiddenUntilMutualConfirmation() {
        val accident = createAccidentPost("麓山南路", "轻微事故")
        val pending = accident.requestContact()

        assertEquals(ContactExchangeStatus.PENDING, pending.contactExchangeStatus)
        assertTrue(pending.visibleContacts.isEmpty())

        val confirmed = pending.confirmContact("对方联系方式")
        assertEquals(ContactExchangeStatus.MUTUALLY_CONFIRMED, confirmed.contactExchangeStatus)
        assertEquals(2, confirmed.visibleContacts.size)
    }

    @Test
    fun accidentFallbackCopyIsSimplifiedChinese() {
        val accident = createAccidentPost("", "轻微事故")

        assertEquals("麓山南路", accident.locationLabel)
        assertTrue(sampleAccidentPosts().first().description.contains("联系方式"))
    }
}
