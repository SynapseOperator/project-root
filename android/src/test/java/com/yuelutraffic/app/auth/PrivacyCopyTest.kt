package com.yuelutraffic.app.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyCopyTest {

    @Test
    fun privacyNoticeStatesBoundaries() {
        assertTrue(PRIVACY_NOTICE.contains("only to distinguish users"))
        assertTrue(PRIVACY_NOTICE.contains("not shown publicly"))
        assertTrue(PRIVACY_NOTICE.contains("not formal school identity verification"))
    }

    @Test
    fun publicCodeDoesNotExposeStudentNumber() {
        val publicCode = publicCodeForStudentNumber("20260001")

        assertTrue(publicCode.startsWith("User-"))
        assertFalse(publicCode.contains("20260001"))
    }
}
