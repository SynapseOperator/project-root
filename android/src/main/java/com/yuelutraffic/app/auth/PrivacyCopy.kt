package com.yuelutraffic.app.auth

import java.util.Locale
import kotlin.math.abs

const val PRIVACY_NOTICE: String =
    "学号仅用于区分应用内用户，不会公开展示，也不代表正式学校身份认证。"

fun publicCodeForStudentNumber(studentNumber: String): String {
    val normalized = studentNumber.trim().uppercase(Locale.ROOT)
    val hash = abs(normalized.hashCode().toLong()).toString(16).uppercase(Locale.ROOT)
    return "同学-" + hash.padStart(6, '0').take(6)
}
