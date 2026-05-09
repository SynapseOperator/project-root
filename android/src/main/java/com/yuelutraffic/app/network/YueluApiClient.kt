package com.yuelutraffic.app.network

import android.os.Handler
import android.os.Looper
import com.yuelutraffic.app.BuildConfig
import com.yuelutraffic.app.accidents.AccidentPostStatus
import com.yuelutraffic.app.accidents.AccidentPostUi
import com.yuelutraffic.app.accidents.ContactExchangeStatus
import com.yuelutraffic.app.config.normalizeBackendBaseUrl
import com.yuelutraffic.app.traffic.FeedbackChoice
import com.yuelutraffic.app.traffic.TrafficReportStatus
import com.yuelutraffic.app.traffic.TrafficReportType
import com.yuelutraffic.app.traffic.TrafficReportUi
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import org.json.JSONArray
import org.json.JSONObject

sealed class ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>()
    data class Failure(
        val message: String,
        val statusCode: Int? = null,
        val throwable: Throwable? = null,
    ) : ApiResult<Nothing>()
}

data class BackendUserProfile(
    val id: String,
    val publicCode: String,
    val role: String,
    val reputationScore: Int,
    val points: Int,
    val titleCode: String,
    val postingBanUntil: String?,
)

data class BackendAuthSession(
    val accessToken: String,
    val user: BackendUserProfile,
    val newUser: Boolean,
    val privacyNotice: String,
)

data class BackendContactExchange(
    val id: String,
    val accidentId: String,
    val status: ContactExchangeStatus,
    val visibleContacts: List<String>,
)

class YueluApiClient(
    private val baseUrl: String = BuildConfig.API_BASE_URL,
    private val ioExecutor: Executor = Executors.newSingleThreadExecutor(),
    private val mainHandler: Handler = Handler(Looper.getMainLooper()),
) {
    fun studentLogin(studentNumber: String, callback: (ApiResult<BackendAuthSession>) -> Unit) {
        execute(callback) {
            val body = studentLoginRequestBody(studentNumber = studentNumber, privacyAcknowledged = true)
            parseAuthSession(postJson(path = "/api/v1/auth/student", body = body, accessToken = null))
        }
    }

    fun fetchMe(accessToken: String, callback: (ApiResult<BackendUserProfile>) -> Unit) {
        execute(callback) {
            parseUserProfile(getJson(path = "/api/v1/me", accessToken = accessToken))
        }
    }

    fun listTrafficReports(callback: (ApiResult<List<TrafficReportUi>>) -> Unit) {
        execute(callback) {
            parseTrafficReports(
                getJson(
                    path = "/api/v1/reports?minLat=28.1200&minLng=112.8800&maxLat=28.2200&maxLng=113.0000",
                    accessToken = null,
                ),
            )
        }
    }

    fun fetchReportDetail(reportId: String, callback: (ApiResult<TrafficReportUi>) -> Unit) {
        execute(callback) {
            parseTrafficReport(getJson(path = "/api/v1/reports/$reportId", accessToken = null))
        }
    }

    fun createTrafficReport(
        accessToken: String,
        type: TrafficReportType,
        locationLabel: String,
        description: String,
        callback: (ApiResult<TrafficReportUi>) -> Unit,
    ) {
        execute(callback) {
            parseTrafficReport(
                postJson(
                    path = "/api/v1/reports",
                    body = createReportRequestBody(type, locationLabel, description),
                    accessToken = accessToken,
                ),
            )
        }
    }

    fun sendReportFeedback(
        accessToken: String,
        reportId: String,
        choice: FeedbackChoice,
        callback: (ApiResult<TrafficReportUi>) -> Unit,
    ) {
        execute(callback) {
            parseTrafficReport(
                postJson(
                    path = "/api/v1/reports/$reportId/feedback",
                    body = feedbackRequestBody(choice),
                    accessToken = accessToken,
                ),
            )
        }
    }

    fun listAccidents(callback: (ApiResult<List<AccidentPostUi>>) -> Unit) {
        execute(callback) {
            parseAccidents(
                getJson(
                    path = "/api/v1/accidents?minLat=28.1200&minLng=112.8800&maxLat=28.2200&maxLng=113.0000",
                    accessToken = null,
                ),
            )
        }
    }

    fun createAccident(
        accessToken: String,
        locationLabel: String,
        description: String,
        callback: (ApiResult<AccidentPostUi>) -> Unit,
    ) {
        execute(callback) {
            parseAccident(
                postJson(
                    path = "/api/v1/accidents",
                    body = createAccidentRequestBody(locationLabel, description),
                    accessToken = accessToken,
                ),
            )
        }
    }

    fun requestAccidentContact(
        accessToken: String,
        accidentId: String,
        contactValue: String,
        callback: (ApiResult<BackendContactExchange>) -> Unit,
    ) {
        execute(callback) {
            parseContactExchange(
                postJson(
                    path = "/api/v1/accidents/$accidentId/contact-requests",
                    body = contactOfferRequestBody(contactValue),
                    accessToken = accessToken,
                ),
            )
        }
    }

    fun confirmAccidentContact(
        accessToken: String,
        requestId: String,
        contactValue: String,
        callback: (ApiResult<BackendContactExchange>) -> Unit,
    ) {
        execute(callback) {
            parseContactExchange(
                postJson(
                    path = "/api/v1/contact-requests/$requestId/confirm",
                    body = contactOfferRequestBody(contactValue),
                    accessToken = accessToken,
                ),
            )
        }
    }

    private fun <T> execute(callback: (ApiResult<T>) -> Unit, block: () -> T) {
        ioExecutor.execute {
            val result = try {
                ApiResult.Success(block())
            } catch (ex: ApiHttpException) {
                ApiResult.Failure(
                    message = "后端返回异常（${ex.statusCode}）：${ex.userMessage()}",
                    statusCode = ex.statusCode,
                    throwable = ex,
                )
            } catch (ex: IOException) {
                ApiResult.Failure(
                    message = "无法连接后端，请确认服务已启动并使用 ${normalizedBaseUrl()}。",
                    throwable = ex,
                )
            } catch (ex: Exception) {
                ApiResult.Failure(
                    message = "后端响应解析失败，请稍后重试。",
                    throwable = ex,
                )
            }
            mainHandler.post { callback(result) }
        }
    }

    private fun postJson(path: String, body: String, accessToken: String?): String {
        return request(path = path, method = "POST", body = body, accessToken = accessToken)
    }

    private fun getJson(path: String, accessToken: String?): String {
        return request(path = path, method = "GET", body = null, accessToken = accessToken)
    }

    private fun request(path: String, method: String, body: String?, accessToken: String?): String {
        val connection = URL(normalizedBaseUrl() + path).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("Accept", "application/json")
            if (accessToken != null) {
                connection.setRequestProperty("Authorization", "Bearer $accessToken")
            }
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.outputStream.use { stream ->
                    stream.write(body.toByteArray(Charsets.UTF_8))
                }
            }
            val responseText = readResponseBody(connection)
            if (connection.responseCode !in 200..299) {
                throw ApiHttpException(connection.responseCode, responseText)
            }
            return responseText
        } finally {
            connection.disconnect()
        }
    }

    private fun readResponseBody(connection: HttpURLConnection): String {
        val stream = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: connection.inputStream
        }
        return stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    private fun normalizedBaseUrl(): String = normalizeBackendBaseUrl(baseUrl)
}

internal fun studentLoginRequestBody(studentNumber: String, privacyAcknowledged: Boolean): String {
    return "{\"studentNumber\":\"${escapeJson(studentNumber.trim())}\",\"privacyAcknowledged\":$privacyAcknowledged}"
}

internal fun createReportRequestBody(
    type: TrafficReportType,
    locationLabel: String,
    description: String,
): String {
    return buildString {
        append("{")
        append("\"type\":\"").append(type.name).append("\",")
        append("\"latitude\":28.1703,")
        append("\"longitude\":112.9388,")
        append("\"locationLabel\":\"").append(escapeJson(locationLabel.trim())).append("\",")
        append("\"description\":\"").append(escapeJson(description.trim())).append("\",")
        append("\"initialCredibility\":").append(type.baseConfidence)
        append("}")
    }
}

internal fun feedbackRequestBody(choice: FeedbackChoice): String {
    return "{\"feedbackType\":\"${choice.name}\"}"
}

internal fun createAccidentRequestBody(locationLabel: String, description: String): String {
    return buildString {
        append("{")
        append("\"latitude\":28.1703,")
        append("\"longitude\":112.9388,")
        append("\"locationLabel\":\"").append(escapeJson(locationLabel.trim())).append("\",")
        append("\"description\":\"").append(escapeJson(description.trim())).append("\"")
        append("}")
    }
}

internal fun contactOfferRequestBody(contactValue: String, contactType: String = "WECHAT"): String {
    return "{\"contactType\":\"$contactType\",\"contactValue\":\"${escapeJson(contactValue.trim())}\"}"
}

internal fun parseAuthSession(json: String): BackendAuthSession {
    val root = JSONObject(json)
    return BackendAuthSession(
        accessToken = root.getString("accessToken"),
        user = parseUserProfile(root.getJSONObject("user")),
        newUser = root.optBoolean("newUser", false),
        privacyNotice = root.optString("privacyNotice"),
    )
}

internal fun parseUserProfile(json: String): BackendUserProfile {
    return parseUserProfile(JSONObject(json))
}

internal fun parseTrafficReports(json: String): List<TrafficReportUi> {
    val array = JSONArray(json)
    return List(array.length()) { index ->
        parseTrafficReport(array.getJSONObject(index))
    }
}

internal fun parseTrafficReport(json: String): TrafficReportUi {
    return parseTrafficReport(JSONObject(json))
}

internal fun parseAccidents(json: String): List<AccidentPostUi> {
    val array = JSONArray(json)
    return List(array.length()) { index ->
        parseAccident(array.getJSONObject(index))
    }
}

internal fun parseAccident(json: String): AccidentPostUi {
    return parseAccident(JSONObject(json))
}

internal fun parseContactExchange(json: String): BackendContactExchange {
    val root = JSONObject(json)
    val visibleContacts = root.optJSONArray("visibleContacts")
    return BackendContactExchange(
        id = root.optString("id"),
        accidentId = root.optString("accidentId"),
        status = contactExchangeStatusFromApi(root.optString("status")),
        visibleContacts = if (visibleContacts == null) {
            emptyList()
        } else {
            List(visibleContacts.length()) { index ->
                val contact = visibleContacts.getJSONObject(index)
                "${contact.optString("publicCode")} · ${contact.optString("contactType")} · ${contact.optString("contactValue")}"
            }
        },
    )
}

private fun parseUserProfile(json: JSONObject): BackendUserProfile {
    return BackendUserProfile(
        id = json.optString("id"),
        publicCode = json.optString("publicCode", "未知用户"),
        role = json.optString("role", "USER"),
        reputationScore = json.optInt("reputationScore", 0),
        points = json.optInt("points", 0),
        titleCode = json.optString("titleCode", "ROAD_HELPER"),
        postingBanUntil = json.optString("postingBanUntil").ifBlank { null },
    )
}

private fun parseTrafficReport(json: JSONObject): TrafficReportUi {
    return TrafficReportUi(
        id = json.optString("id"),
        type = reportTypeFromApi(json.optString("type")),
        latitude = json.optDouble("latitude", 28.1703),
        longitude = json.optDouble("longitude", 112.9388),
        locationLabel = json.optString("locationLabel", "中南大学 / 麓山南路"),
        description = json.optString("description"),
        submittedAt = parseInstantOrNow(json.optString("submittedAt")),
        defaultExpiresAt = parseInstantOrNow(json.optString("defaultExpiresAt")),
        status = reportStatusFromApi(json.optString("status")),
        confidenceScore = json.optInt("confidenceScore", json.optInt("initialCredibility", 50)),
    )
}

private fun parseAccident(json: JSONObject): AccidentPostUi {
    return AccidentPostUi(
        id = json.optString("id"),
        locationLabel = json.optString("locationLabel", "麓山南路"),
        description = json.optString("description"),
        occurredAt = parseInstantOrNow(json.optString("occurredAt")),
        status = accidentStatusFromApi(json.optString("status")),
    )
}

private fun reportTypeFromApi(value: String): TrafficReportType {
    return TrafficReportType.entries.firstOrNull { it.name == value } ?: TrafficReportType.CONGESTION
}

private fun reportStatusFromApi(value: String): TrafficReportStatus {
    return TrafficReportStatus.entries.firstOrNull { it.name == value } ?: TrafficReportStatus.UNDER_REVIEW
}

private fun accidentStatusFromApi(value: String): AccidentPostStatus {
    return AccidentPostStatus.entries.firstOrNull { it.name == value } ?: AccidentPostStatus.OPEN
}

private fun contactExchangeStatusFromApi(value: String): ContactExchangeStatus {
    return ContactExchangeStatus.entries.firstOrNull { it.name == value } ?: ContactExchangeStatus.NONE
}

private fun parseInstantOrNow(value: String): Instant {
    return runCatching { Instant.parse(value) }.getOrElse { Instant.now() }
}

private fun escapeJson(value: String): String {
    return buildString {
        value.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
    }
}

private class ApiHttpException(
    val statusCode: Int,
    private val responseBody: String,
) : IOException("HTTP $statusCode $responseBody") {
    fun userMessage(): String {
        return when (statusCode) {
            400 -> "请求信息未通过校验，请检查后重试。"
            401 -> "登录状态无效，请重新登录。"
            403 -> "当前账号无权访问该功能。"
            in 500..599 -> "后端服务暂时不可用，请稍后再试。"
            else -> "请求未成功，请稍后重试。"
        }
    }
}
