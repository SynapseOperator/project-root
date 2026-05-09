package com.yuelutraffic.app.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyCopyTest {

    @Test
    fun privacyNoticeStatesBoundaries() {
        assertTrue(PRIVACY_NOTICE.contains("仅用于区分应用内用户"))
        assertTrue(PRIVACY_NOTICE.contains("不会公开展示"))
        assertTrue(PRIVACY_NOTICE.contains("不代表正式学校身份认证"))
    }

    @Test
    fun publicCodeDoesNotExposeStudentNumber() {
        val publicCode = publicCodeForStudentNumber("20260001")

        assertTrue(publicCode.startsWith("同学-"))
        assertFalse(publicCode.contains("20260001"))
    }
}
