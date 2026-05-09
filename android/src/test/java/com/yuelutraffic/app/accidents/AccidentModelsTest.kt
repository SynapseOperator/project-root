package com.yuelutraffic.app.accidents

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccidentModelsTest {

    @Test
    fun contactStaysHiddenUntilMutualConfirmation() {
        val accident = createAccidentPost("Lushan South Road", "Minor incident")
        val pending = accident.requestContact()

        assertEquals(ContactExchangeStatus.PENDING, pending.contactExchangeStatus)
        assertTrue(pending.visibleContacts.isEmpty())

        val confirmed = pending.confirmContact("other-user-wechat")
        assertEquals(ContactExchangeStatus.MUTUALLY_CONFIRMED, confirmed.contactExchangeStatus)
        assertEquals(2, confirmed.visibleContacts.size)
    }
}
