package com.yuelutraffic.app.auth

import java.util.Locale
import kotlin.math.abs

const val PRIVACY_NOTICE: String =
    "Student number is used only to distinguish users in the app. It is not shown publicly and is not formal school identity verification."

fun publicCodeForStudentNumber(studentNumber: String): String {
    val normalized = studentNumber.trim().uppercase(Locale.ROOT)
    val hash = abs(normalized.hashCode().toLong()).toString(16).uppercase(Locale.ROOT)
    return "User-" + hash.padStart(6, '0').take(6)
}
