package com.yuelutraffic.app.network

import android.os.Handler
import android.os.Looper
import com.yuelutraffic.app.BuildConfig
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executor
import java.util.concurrent.Executors
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

    private fun getJson(path: String, accessToken: String): String {
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

    private fun normalizedBaseUrl(): String = baseUrl.trimEnd('/')
}

internal fun studentLoginRequestBody(studentNumber: String, privacyAcknowledged: Boolean): String {
    return "{\"studentNumber\":\"${escapeJson(studentNumber.trim())}\",\"privacyAcknowledged\":$privacyAcknowledged}"
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
            400 -> "登录信息未通过校验，请检查后重试。"
            401 -> "登录状态无效，请重新登录。"
            403 -> "当前账号无权访问该功能。"
            in 500..599 -> "后端服务暂时不可用，请稍后再试。"
            else -> "请求未成功，请稍后重试。"
        }
    }
}
