package com.yuelutraffic.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yuelutraffic.app.accidents.AccidentPostStatus
import com.yuelutraffic.app.accidents.AccidentPostUi
import com.yuelutraffic.app.accidents.ContactExchangeStatus
import com.yuelutraffic.app.accidents.confirmContact
import com.yuelutraffic.app.accidents.createAccidentPost
import com.yuelutraffic.app.accidents.requestContact
import com.yuelutraffic.app.accidents.sampleAccidentPosts
import com.yuelutraffic.app.auth.PRIVACY_NOTICE
import com.yuelutraffic.app.auth.publicCodeForStudentNumber
import com.yuelutraffic.app.config.isSupportedBackendBaseUrl
import com.yuelutraffic.app.config.normalizeBackendBaseUrl
import com.yuelutraffic.app.map.AmapTrafficMapPanel
import com.yuelutraffic.app.map.MapProviderMode
import com.yuelutraffic.app.map.currentMapProviderRuntimeConfig
import com.yuelutraffic.app.network.ApiResult
import com.yuelutraffic.app.network.BackendAuthSession
import com.yuelutraffic.app.network.BackendUserProfile
import com.yuelutraffic.app.network.YueluApiClient
import com.yuelutraffic.app.storage.SessionStore
import com.yuelutraffic.app.traffic.FeedbackChoice
import com.yuelutraffic.app.traffic.TrafficReportStatus
import com.yuelutraffic.app.traffic.TrafficReportType
import com.yuelutraffic.app.traffic.TrafficReportUi
import com.yuelutraffic.app.traffic.applyFeedback
import com.yuelutraffic.app.traffic.createTrafficReport
import com.yuelutraffic.app.traffic.sampleTrafficReports
import java.time.Instant

@Composable
fun YueluTrafficApp() {
    val context = LocalContext.current
    val sessionStore = remember(context) { SessionStore(context) }
    var apiBaseUrl by rememberSaveable { mutableStateOf(sessionStore.loadBackendBaseUrl()) }
    val normalizedBaseUrl = normalizeBackendBaseUrl(apiBaseUrl)
    val apiClient = remember(normalizedBaseUrl) { YueluApiClient(baseUrl = normalizedBaseUrl) }

    var session by remember { mutableStateOf(sessionStore.loadSession()) }
    var isCheckingSession by rememberSaveable { mutableStateOf(session?.accessToken != null) }
    var isLoggingIn by rememberSaveable { mutableStateOf(false) }
    var loginError by rememberSaveable { mutableStateOf<String?>(null) }

    fun saveBaseUrl(value: String) {
        apiBaseUrl = value
        sessionStore.saveBackendBaseUrl(value)
    }

    fun clearSession(message: String? = null) {
        sessionStore.clearSession()
        session = null
        loginError = message
    }

    LaunchedEffect(Unit) {
        val storedSession = session
        val token = storedSession?.accessToken
        if (token == null) {
            isCheckingSession = false
        } else {
            apiClient.fetchMe(token) { result ->
                isCheckingSession = false
                when (result) {
                    is ApiResult.Success -> {
                        val restored = result.value.toUiSession(
                            accessToken = token,
                            connectionMessage = "已恢复本机登录，并通过后端校验当前用户。",
                        )
                        sessionStore.saveSession(restored)
                        session = restored
                    }
                    is ApiResult.Failure -> {
                        clearSession("登录状态已失效，请重新登录。${result.message}")
                    }
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        val signedInSession = session
        when {
            isCheckingSession -> RestoringSessionScreen(apiBaseUrl = normalizedBaseUrl)
            signedInSession == null -> LoginScreen(
                apiBaseUrl = apiBaseUrl,
                isLoading = isLoggingIn,
                errorMessage = loginError,
                onApiBaseUrlChange = ::saveBaseUrl,
                onLogin = { studentNumber ->
                    isLoggingIn = true
                    loginError = null
                    apiClient.studentLogin(studentNumber) { result ->
                        isLoggingIn = false
                        when (result) {
                            is ApiResult.Success -> {
                                val backendSession = result.value
                                val loginSession = backendSession.toUiSession("已连接后端登录服务，正在刷新当前用户。")
                                sessionStore.saveSession(loginSession)
                                session = loginSession
                                apiClient.fetchMe(backendSession.accessToken) { meResult ->
                                    session = when (meResult) {
                                        is ApiResult.Success -> {
                                            val refreshed = meResult.value.toUiSession(
                                                accessToken = backendSession.accessToken,
                                                connectionMessage = "已通过后端 /api/v1/me 获取当前用户会话。",
                                            )
                                            sessionStore.saveSession(refreshed)
                                            refreshed
                                        }
                                        is ApiResult.Failure -> {
                                            val fallback = backendSession.toUiSession(
                                                "登录成功，但刷新当前用户失败：${meResult.message}",
                                            )
                                            sessionStore.saveSession(fallback)
                                            fallback
                                        }
                                    }
                                }
                            }
                            is ApiResult.Failure -> {
                                loginError = result.message
                            }
                        }
                    }
                },
                onUseDemo = { studentNumber ->
                    val displayCode = publicCodeForStudentNumber(studentNumber.ifBlank { "DEMO-STUDENT" })
                    sessionStore.clearSession()
                    loginError = null
                    session = StudentSessionUi(
                        publicCode = displayCode,
                        connectionMessage = "当前为本地演示数据，未连接后端服务。",
                    )
                },
            )
            else -> MainShell(
                session = signedInSession,
                apiClient = apiClient,
                apiBaseUrl = apiBaseUrl,
                onApiBaseUrlChange = ::saveBaseUrl,
                onSessionUpdated = { updated ->
                    sessionStore.saveSession(updated)
                    session = updated
                },
                onLogout = { clearSession("已退出登录。") },
            )
        }
    }
}

@Composable
private fun RestoringSessionScreen(apiBaseUrl: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YueluColors.Page)
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(AppCopy.appName, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        StatusNotice(
            title = "正在恢复登录",
            body = "正在使用 $apiBaseUrl 校验本机保存的后端会话。",
        )
    }
}

@Composable
private fun LoginScreen(
    apiBaseUrl: String,
    isLoading: Boolean,
    errorMessage: String?,
    onApiBaseUrlChange: (String) -> Unit,
    onLogin: (String) -> Unit,
    onUseDemo: (String) -> Unit,
) {
    var studentNumber by rememberSaveable { mutableStateOf("") }
    var acknowledged by rememberSaveable { mutableStateOf(false) }
    val baseUrlValid = isSupportedBackendBaseUrl(normalizeBackendBaseUrl(apiBaseUrl))
    val canContinue = studentNumber.trim().length >= 4 && acknowledged && baseUrlValid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YueluColors.Page)
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(AppCopy.appName, style = MaterialTheme.typography.headlineLarge, color = YueluColors.Ink, fontWeight = FontWeight.Bold)
        Text(AppCopy.appSubtitle, style = MaterialTheme.typography.titleMedium, color = YueluColors.CampusGreen)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = apiBaseUrl,
            onValueChange = onApiBaseUrlChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("后端地址") },
            supportingText = { Text("模拟器默认使用 http://10.0.2.2:8080；真机可填写电脑局域网地址。") },
            singleLine = true,
        )
        if (!baseUrlValid) {
            StatusNotice(
                title = "后端地址格式不正确",
                body = "请使用 http:// 或 https:// 开头的地址。",
                isWarning = true,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = studentNumber,
            onValueChange = { studentNumber = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("学号") },
            supportingText = { Text("仅用于登录和生成应用内公开代码。") },
            singleLine = true,
        )
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Checkbox(checked = acknowledged, onCheckedChange = { acknowledged = it })
            Text(
                text = PRIVACY_NOTICE,
                style = MaterialTheme.typography.bodyMedium,
                color = YueluColors.InkMuted,
                modifier = Modifier.weight(1f),
            )
        }
        Button(
            enabled = canContinue && !isLoading,
            onClick = { onLogin(studentNumber) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isLoading) "正在连接后端" else "登录并进入地图")
        }
        OutlinedButton(
            enabled = !isLoading && (studentNumber.isBlank() || acknowledged),
            onClick = { onUseDemo(studentNumber) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("使用本地演示模式")
        }
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            StatusNotice(title = "后端连接失败", body = errorMessage, isWarning = true)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(AppCopy.lawfulUse, style = MaterialTheme.typography.bodySmall, color = YueluColors.InkMuted)
    }
}

@Composable
private fun MainShell(
    session: StudentSessionUi,
    apiClient: YueluApiClient,
    apiBaseUrl: String,
    onApiBaseUrlChange: (String) -> Unit,
    onSessionUpdated: (StudentSessionUi) -> Unit,
    onLogout: () -> Unit,
) {
    val reports = remember { mutableStateListOf<TrafficReportUi>().also { it.addAll(sampleTrafficReports()) } }
    val accidents = remember { mutableStateListOf<AccidentPostUi>().also { it.addAll(sampleAccidentPosts()) } }
    val reviewQueue = remember { mutableStateListOf<TrafficReportUi>() }
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Map) }
    var postingRestricted by rememberSaveable(session.publicCode, session.postingBanUntil) {
        mutableStateOf(session.postingBanUntil != null)
    }
    var selectedReport by remember { mutableStateOf<TrafficReportUi?>(null) }
    var isReportLoading by rememberSaveable { mutableStateOf(false) }
    var isReportSubmitting by rememberSaveable { mutableStateOf(false) }
    var isAccidentLoading by rememberSaveable { mutableStateOf(false) }
    var isAccidentSubmitting by rememberSaveable { mutableStateOf(false) }
    var isLeaderboardLoading by rememberSaveable { mutableStateOf(false) }
    var isAdminLoading by rememberSaveable { mutableStateOf(false) }
    var isAdminActionRunning by rememberSaveable { mutableStateOf(false) }
    var reportStatusMessage by rememberSaveable {
        mutableStateOf(if (session.isDemoMode) "当前显示本地演示路况，未连接后端列表。" else "正在从后端加载麓山南路附近路况。")
    }
    var accidentStatusMessage by rememberSaveable {
        mutableStateOf(if (session.isDemoMode) "当前显示本地演示互助信息，未连接后端事故栏。" else "正在从后端加载事故互助信息。")
    }
    var profileStatusMessage by rememberSaveable {
        mutableStateOf(if (session.isDemoMode) "当前为本地演示资料。" else "资料来自后端当前用户接口。")
    }
    var adminStatusMessage by rememberSaveable {
        mutableStateOf(if (session.isDemoMode) "当前为本地管理员演示。" else "管理员操作将直接写入后端。")
    }
    val leaderboardRows = remember {
        mutableStateListOf<LeaderboardEntryUi>().also {
            it.addAll(leaderboardEntries(session, 0))
        }
    }

    fun replaceReport(updated: TrafficReportUi) {
        val index = reports.indexOfFirst { it.id == updated.id }
        if (index >= 0) reports[index] = updated else reports.add(0, updated)
        if (selectedReport?.id == updated.id) selectedReport = updated
    }

    fun loadBackendReports() {
        if (session.isDemoMode) {
            reportStatusMessage = "当前显示本地演示路况，未连接后端列表。"
            return
        }
        isReportLoading = true
        reportStatusMessage = "正在同步后端路况。"
        apiClient.listTrafficReports { result ->
            isReportLoading = false
            when (result) {
                is ApiResult.Success -> {
                    reports.clear()
                    reports.addAll(result.value)
                    reportStatusMessage = "已从后端加载 ${result.value.size} 条附近路况。"
                }
                is ApiResult.Failure -> {
                    if (reports.isEmpty()) reports.addAll(sampleTrafficReports())
                    reportStatusMessage = "后端路况加载失败：${result.message} 当前显示本地演示路况。"
                }
            }
        }
    }

    fun selectReport(report: TrafficReportUi) {
        selectedReport = report
        if (!session.isDemoMode) {
            apiClient.fetchReportDetail(report.id) { result ->
                when (result) {
                    is ApiResult.Success -> replaceReport(result.value)
                    is ApiResult.Failure -> reportStatusMessage = "详情刷新失败：${result.message}"
                }
            }
        }
    }

    fun submitReport(type: TrafficReportType, locationLabel: String, description: String) {
        if (session.isDemoMode || session.accessToken == null) {
            val report = createTrafficReport(type, locationLabel, description)
            reports.add(0, report)
            selectedReport = report
            selectedTab = MainTab.Map
            reportStatusMessage = "本地演示上报已添加，未同步到后端。"
            return
        }
        isReportSubmitting = true
        reportStatusMessage = "正在提交到后端。"
        apiClient.createTrafficReport(session.accessToken, type, locationLabel, description) { result ->
            isReportSubmitting = false
            when (result) {
                is ApiResult.Success -> {
                    replaceReport(result.value)
                    selectedReport = result.value
                    selectedTab = MainTab.Map
                    reportStatusMessage = "路况已提交到后端。"
                }
                is ApiResult.Failure -> reportStatusMessage = "路况提交失败：${result.message}"
            }
        }
    }

    fun applyReportFeedback(reportId: String, choice: FeedbackChoice) {
        if (session.isDemoMode || session.accessToken == null) {
            val index = reports.indexOfFirst { it.id == reportId }
            if (index >= 0) {
                reports[index] = reports[index].applyFeedback(choice)
                if (selectedReport?.id == reportId) selectedReport = reports[index]
            }
            reportStatusMessage = "本地演示反馈已记录，未同步到后端。"
            return
        }
        apiClient.sendReportFeedback(session.accessToken, reportId, choice) { result ->
            when (result) {
                is ApiResult.Success -> {
                    replaceReport(result.value)
                    reportStatusMessage = "反馈已同步到后端。"
                }
                is ApiResult.Failure -> reportStatusMessage = "反馈提交失败：${result.message}"
            }
        }
    }

    fun replaceAccident(updated: AccidentPostUi) {
        val index = accidents.indexOfFirst { it.id == updated.id }
        if (index >= 0) accidents[index] = updated else accidents.add(0, updated)
    }

    fun updateAccidentExchange(exchange: com.yuelutraffic.app.network.BackendContactExchange) {
        val index = accidents.indexOfFirst { it.id == exchange.accidentId }
        if (index >= 0) {
            accidents[index] = accidents[index].copy(
                status = if (exchange.status == ContactExchangeStatus.MUTUALLY_CONFIRMED) {
                    AccidentPostStatus.MATCHED
                } else {
                    accidents[index].status
                },
                contactExchangeStatus = exchange.status,
                contactRequestId = exchange.id,
                visibleContacts = exchange.visibleContacts,
            )
        }
    }

    fun loadBackendAccidents() {
        if (session.isDemoMode) {
            accidentStatusMessage = "当前显示本地演示互助信息，未连接后端事故栏。"
            return
        }
        isAccidentLoading = true
        accidentStatusMessage = "正在同步后端事故栏。"
        apiClient.listAccidents { result ->
            isAccidentLoading = false
            when (result) {
                is ApiResult.Success -> {
                    accidents.clear()
                    accidents.addAll(result.value)
                    accidentStatusMessage = "已从后端加载 ${result.value.size} 条事故互助信息。"
                }
                is ApiResult.Failure -> {
                    if (accidents.isEmpty()) accidents.addAll(sampleAccidentPosts())
                    accidentStatusMessage = "事故栏加载失败：${result.message} 当前显示本地演示信息。"
                }
            }
        }
    }

    fun submitAccident(locationLabel: String, description: String) {
        if (session.isDemoMode || session.accessToken == null) {
            accidents.add(0, createAccidentPost(locationLabel, description))
            accidentStatusMessage = "本地演示事故互助已添加，未同步到后端。"
            return
        }
        isAccidentSubmitting = true
        accidentStatusMessage = "正在提交事故互助到后端。"
        apiClient.createAccident(session.accessToken, locationLabel, description) { result ->
            isAccidentSubmitting = false
            when (result) {
                is ApiResult.Success -> {
                    replaceAccident(result.value)
                    accidentStatusMessage = "事故互助已提交到后端。"
                }
                is ApiResult.Failure -> accidentStatusMessage = "事故互助提交失败：${result.message}"
            }
        }
    }

    fun requestAccidentContact(accidentId: String, contactValue: String) {
        if (session.isDemoMode || session.accessToken == null) {
            val index = accidents.indexOfFirst { it.id == accidentId }
            if (index >= 0) accidents[index] = accidents[index].requestContact()
            accidentStatusMessage = "本地演示联系方式申请已记录，未同步到后端。"
            return
        }
        apiClient.requestAccidentContact(session.accessToken, accidentId, contactValue) { result ->
            when (result) {
                is ApiResult.Success -> {
                    updateAccidentExchange(result.value)
                    accidentStatusMessage = "联系方式申请已提交后端；请等待另一方确认。请求 ID：${result.value.id}"
                }
                is ApiResult.Failure -> accidentStatusMessage = "联系方式申请失败：${result.message}"
            }
        }
    }

    fun confirmAccidentContact(requestId: String?, contactValue: String) {
        if (requestId == null) {
            accidentStatusMessage = "请先申请联系方式，或输入已有请求后再确认。"
            return
        }
        if (session.isDemoMode || session.accessToken == null) {
            val index = accidents.indexOfFirst { it.contactRequestId == requestId }
            if (index >= 0) accidents[index] = accidents[index].confirmContact(contactValue)
            accidentStatusMessage = "本地演示双方确认已完成，未同步到后端。"
            return
        }
        apiClient.confirmAccidentContact(session.accessToken, requestId, contactValue) { result ->
            when (result) {
                is ApiResult.Success -> {
                    updateAccidentExchange(result.value)
                    accidentStatusMessage = "双方联系方式确认已同步到后端。"
                }
                is ApiResult.Failure -> accidentStatusMessage = "联系方式确认失败：${result.message}"
            }
        }
    }

    fun refreshProfile() {
        val token = session.accessToken
        if (session.isDemoMode || token == null) {
            profileStatusMessage = "演示模式不刷新后端资料。"
            return
        }
        profileStatusMessage = "正在刷新后端资料。"
        apiClient.fetchMe(token) { result ->
            when (result) {
                is ApiResult.Success -> {
                    val updated = result.value.toUiSession(
                        accessToken = token,
                        connectionMessage = "已刷新后端当前用户资料。",
                    )
                    onSessionUpdated(updated)
                    profileStatusMessage = "后端资料已刷新。"
                }
                is ApiResult.Failure -> {
                    profileStatusMessage = "资料刷新失败：${result.message}"
                }
            }
        }
    }

    fun loadLeaderboard() {
        if (session.isDemoMode) {
            leaderboardRows.clear()
            leaderboardRows.addAll(leaderboardEntries(session, reports.count { it.status == TrafficReportStatus.ACTIVE }))
            profileStatusMessage = "当前显示本地演示排行榜。"
            return
        }
        isLeaderboardLoading = true
        profileStatusMessage = "正在加载后端排行榜。"
        apiClient.listLeaderboard { result ->
            isLeaderboardLoading = false
            when (result) {
                is ApiResult.Success -> {
                    leaderboardRows.clear()
                    leaderboardRows.addAll(result.value.mapIndexed { index, user ->
                        LeaderboardEntryUi(
                            rank = index + 1,
                            publicCode = user.publicCode,
                            points = user.points,
                            title = titleLabel(user.titleCode),
                        )
                    })
                    profileStatusMessage = "已从后端加载 ${result.value.size} 条排行榜数据。"
                }
                is ApiResult.Failure -> {
                    profileStatusMessage = "排行榜加载失败：${result.message}"
                }
            }
        }
    }

    fun refreshAdminQueue() {
        if (!session.isDemoAdmin) {
            adminStatusMessage = "当前账号不是管理员，无法读取后台审核队列。"
            return
        }
        if (session.isDemoMode || session.accessToken == null) {
            reviewQueue.clear()
            reviewQueue.addAll(reports.filter { it.status == TrafficReportStatus.UNDER_REVIEW })
            adminStatusMessage = if (reviewQueue.isEmpty()) {
                "演示模式暂无待复核路况，可查看事故互助管理示例。"
            } else {
                "已加载 ${reviewQueue.size} 条本地演示待复核路况。"
            }
            return
        }
        isAdminLoading = true
        adminStatusMessage = "正在加载后端审核队列。"
        apiClient.listAdminReviewQueue(session.accessToken) { result ->
            isAdminLoading = false
            when (result) {
                is ApiResult.Success -> {
                    reviewQueue.clear()
                    reviewQueue.addAll(result.value)
                    adminStatusMessage = "已加载 ${result.value.size} 条后端待复核路况。"
                }
                is ApiResult.Failure -> {
                    adminStatusMessage = "审核队列加载失败：${result.message}"
                }
            }
        }
    }

    fun moderateReportFromAdmin(reportId: String, status: TrafficReportStatus, reason: String) {
        if (session.isDemoMode || session.accessToken == null) {
            val index = reports.indexOfFirst { it.id == reportId }
            if (index >= 0) {
                val updated = reports[index].copy(status = status)
                replaceReport(updated)
                reviewQueue.removeAll { it.id == reportId }
                adminStatusMessage = "演示模式已将路况标记为${trafficStatusLabel(status)}。"
            } else {
                adminStatusMessage = "未找到要处理的演示路况。"
            }
            return
        }
        isAdminActionRunning = true
        adminStatusMessage = "正在提交路况审核操作。"
        apiClient.moderateReport(session.accessToken, reportId, status, reason) { result ->
            isAdminActionRunning = false
            when (result) {
                is ApiResult.Success -> {
                    replaceReport(result.value)
                    reviewQueue.removeAll { it.id == result.value.id }
                    adminStatusMessage = "路况已更新为${trafficStatusLabel(result.value.status)}。"
                }
                is ApiResult.Failure -> {
                    adminStatusMessage = "路况审核操作失败：${result.message}"
                }
            }
        }
    }

    fun moderateAccidentFromAdmin(accidentId: String, status: AccidentPostStatus, reason: String) {
        if (session.isDemoMode || session.accessToken == null) {
            val index = accidents.indexOfFirst { it.id == accidentId }
            if (index >= 0) {
                accidents[index] = accidents[index].copy(status = status)
                adminStatusMessage = "演示模式已将事故互助标记为${accidentStatusLabel(status)}。"
            } else {
                adminStatusMessage = "未找到要处理的演示事故互助。"
            }
            return
        }
        isAdminActionRunning = true
        adminStatusMessage = "正在提交事故互助管理操作。"
        apiClient.moderateAccident(session.accessToken, accidentId, status, reason) { result ->
            isAdminActionRunning = false
            when (result) {
                is ApiResult.Success -> {
                    replaceAccident(result.value)
                    adminStatusMessage = "事故互助已更新为${accidentStatusLabel(result.value.status)}。"
                }
                is ApiResult.Failure -> {
                    adminStatusMessage = "事故互助管理失败：${result.message}"
                }
            }
        }
    }

    fun restrictUserFromAdmin(userId: String?, reason: String) {
        if (userId.isNullOrBlank()) {
            adminStatusMessage = "该条目缺少后端用户 ID，无法限制上报。"
            return
        }
        val banUntil = Instant.now().plusSeconds(24 * 60 * 60).toString()
        if (session.isDemoMode || session.accessToken == null) {
            postingRestricted = true
            adminStatusMessage = "演示模式已开启 24 小时上报限制。"
            return
        }
        isAdminActionRunning = true
        adminStatusMessage = "正在提交用户上报限制。"
        apiClient.restrictUser(session.accessToken, userId, banUntil, reason) { result ->
            isAdminActionRunning = false
            when (result) {
                is ApiResult.Success -> {
                    adminStatusMessage = "已限制 ${result.value.publicCode} 上报至 ${result.value.postingBanUntil ?: banUntil}。"
                }
                is ApiResult.Failure -> {
                    adminStatusMessage = "用户限制操作失败：${result.message}"
                }
            }
        }
    }

    LaunchedEffect(session.accessToken, session.isDemoMode) {
        postingRestricted = session.postingBanUntil != null
        if (!session.isDemoMode) {
            loadBackendReports()
            loadBackendAccidents()
            loadLeaderboard()
        }
        if (session.isDemoAdmin) {
            refreshAdminQueue()
        }
    }

    Scaffold(
        containerColor = YueluColors.Page,
        bottomBar = {
            NavigationBar(containerColor = YueluColors.Surface) {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Text(tab.iconText, fontWeight = FontWeight.Bold) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        when (selectedTab) {
            MainTab.Map -> MapHomeScreen(
                session = session,
                reports = reports,
                reportStatusMessage = reportStatusMessage,
                isReportLoading = isReportLoading,
                onRefreshReports = ::loadBackendReports,
                onSelectReport = ::selectReport,
                onFeedback = ::applyReportFeedback,
                contentPadding = innerPadding,
            )
            MainTab.Report -> SubmitReportScreen(
                postingRestricted = postingRestricted,
                isSubmitting = isReportSubmitting,
                statusMessage = reportStatusMessage,
                isBackendMode = !session.isDemoMode,
                onSubmit = ::submitReport,
                contentPadding = innerPadding,
            )
            MainTab.Accidents -> AccidentBoardScreen(
                statusMessage = accidentStatusMessage,
                isLoading = isAccidentLoading,
                isSubmitting = isAccidentSubmitting,
                isBackendMode = !session.isDemoMode,
                accidents = accidents,
                onRefresh = ::loadBackendAccidents,
                onSubmit = ::submitAccident,
                onRequestContact = ::requestAccidentContact,
                onConfirmContact = ::confirmAccidentContact,
                contentPadding = innerPadding,
            )
            MainTab.Profile -> ProfileScreen(
                session = session,
                apiBaseUrl = apiBaseUrl,
                onApiBaseUrlChange = onApiBaseUrlChange,
                activeReports = reports.count { it.status == TrafficReportStatus.ACTIVE },
                leaderboardEntries = leaderboardRows,
                profileStatusMessage = profileStatusMessage,
                isLeaderboardLoading = isLeaderboardLoading,
                reportsUnderReview = reviewQueue,
                accidents = accidents,
                postingRestricted = postingRestricted,
                adminStatusMessage = adminStatusMessage,
                isAdminLoading = isAdminLoading,
                isAdminActionRunning = isAdminActionRunning,
                onRefreshProfile = ::refreshProfile,
                onRefreshLeaderboard = ::loadLeaderboard,
                onRefreshAdmin = ::refreshAdminQueue,
                onApproveReport = { report ->
                    moderateReportFromAdmin(report.id, TrafficReportStatus.ACTIVE, "管理员确认路况信息可保留。")
                },
                onHideReport = { report ->
                    moderateReportFromAdmin(report.id, TrafficReportStatus.HIDDEN, "管理员隐藏不适合公开的路况信息。")
                },
                onHideAccident = { accident ->
                    moderateAccidentFromAdmin(accident.id, AccidentPostStatus.HIDDEN, "管理员隐藏不适合公开的事故互助信息。")
                },
                onRestrictUser = ::restrictUserFromAdmin,
                onLogout = onLogout,
                contentPadding = innerPadding,
            )
        }
    }

    selectedReport?.let { report ->
        ReportDetailDialog(
            report = report,
            onDismiss = { selectedReport = null },
            onFeedback = { choice -> applyReportFeedback(report.id, choice) },
        )
    }
}

@Composable
private fun MapHomeScreen(
    session: StudentSessionUi,
    reports: List<TrafficReportUi>,
    reportStatusMessage: String,
    isReportLoading: Boolean,
    onRefreshReports: () -> Unit,
    onSelectReport: (TrafficReportUi) -> Unit,
    onFeedback: (String, FeedbackChoice) -> Unit,
    contentPadding: PaddingValues,
) {
    val mapProviderConfig = remember { currentMapProviderRuntimeConfig() }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            AppHeader(title = "麓山南路实时路况", subtitle = "你好，${session.publicCode}", badge = "地图优先")
        }
        item {
            StatusNotice(
                title = if (session.isDemoMode) "演示模式" else if (isReportLoading) "正在同步" else "后端路况",
                body = "${session.connectionMessage} $reportStatusMessage ${mapProviderConfig.statusMessage}",
            )
        }
        item {
            OutlinedButton(enabled = !isReportLoading, onClick = onRefreshReports, modifier = Modifier.fillMaxWidth()) {
                Text(if (isReportLoading) "正在刷新路况" else "刷新路况")
            }
        }
        item {
            if (mapProviderConfig.mode == MapProviderMode.AMAP) {
                AmapTrafficMapPanel(
                    reports = reports,
                    apiKey = mapProviderConfig.amapApiKey,
                    statusMessage = mapProviderConfig.statusMessage,
                    onSelectReport = onSelectReport,
                )
            } else {
                MockMapPanel(reports = reports, onSelectReport = onSelectReport)
            }
        }
        item {
            ReportFeed(reports = reports, onSelectReport = onSelectReport, onFeedback = onFeedback)
        }
    }
}

@Composable
private fun SubmitReportScreen(
    postingRestricted: Boolean,
    isSubmitting: Boolean,
    statusMessage: String,
    isBackendMode: Boolean,
    onSubmit: (TrafficReportType, String, String) -> Unit,
    contentPadding: PaddingValues,
) {
    var selectedType by rememberSaveable { mutableStateOf(TrafficReportType.CONGESTION) }
    var locationLabel by rememberSaveable { mutableStateOf("麓山南路中南大学门口") }
    var description by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { AppHeader(title = "上报路况", subtitle = "补充你看到的道路安全与通行信息", badge = "30 秒完成") }
        if (postingRestricted) {
            item { StatusNotice(title = "暂不可上报", body = "当前账号处于限制状态，请稍后再试。", isWarning = true) }
        }
        item { StatusNotice(title = if (isBackendMode) "后端提交" else "演示提交", body = statusMessage) }
        item {
            ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = YueluColors.Surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("选择类型", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    TrafficReportType.entries.forEach { type ->
                        FilterChip(selected = selectedType == type, onClick = { selectedType = type }, label = { Text(type.label) })
                    }
                    OutlinedTextField(
                        value = locationLabel,
                        onValueChange = { locationLabel = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("地点") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("补充说明") },
                        minLines = 3,
                    )
                    Button(
                        enabled = !postingRestricted && !isSubmitting && locationLabel.isNotBlank(),
                        onClick = {
                            onSubmit(selectedType, locationLabel, description)
                            description = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (isSubmitting) "正在提交" else if (isBackendMode) "提交到后端" else "提交演示路况")
                    }
                    Text(AppCopy.lawfulUse, style = MaterialTheme.typography.bodySmall, color = YueluColors.InkMuted)
                }
            }
        }
    }
}

@Composable
private fun AccidentBoardScreen(
    statusMessage: String,
    isLoading: Boolean,
    isSubmitting: Boolean,
    isBackendMode: Boolean,
    accidents: List<AccidentPostUi>,
    onRefresh: () -> Unit,
    onSubmit: (String, String) -> Unit,
    onRequestContact: (String, String) -> Unit,
    onConfirmContact: (String?, String) -> Unit,
    contentPadding: PaddingValues,
) {
    var locationLabel by rememberSaveable { mutableStateOf("麓山南路中南大学门口") }
    var description by rememberSaveable { mutableStateOf("") }
    var contactValue by rememberSaveable { mutableStateOf("微信或手机号仅本机演示") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { AppHeader(title = "事故互助栏", subtitle = "联系方式默认隐藏，双方确认后才显示", badge = "隐私优先") }
        item {
            StatusNotice(
                title = if (isBackendMode) "后端事故栏" else "演示事故栏",
                body = "$statusMessage ${AppCopy.accidentDemoNotice}",
            )
        }
        item {
            OutlinedButton(enabled = !isLoading, onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
                Text(if (isLoading) "正在刷新事故栏" else "刷新事故栏")
            }
        }
        item {
            ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = YueluColors.Surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = locationLabel,
                        onValueChange = { locationLabel = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("事故地点") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("情况说明") },
                        minLines = 2,
                    )
                    Button(
                        onClick = {
                            onSubmit(locationLabel, description.ifBlank { "轻微事故，等待互助确认。" })
                            description = ""
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (isSubmitting) "正在发布" else if (isBackendMode) "发布到后端" else "发布演示信息")
                    }
                }
            }
        }
        items(accidents, key = { it.id }) { accident ->
            AccidentCard(
                accident = accident,
                contactValue = contactValue,
                onContactChange = { contactValue = it },
                onRequestContact = { onRequestContact(accident.id, contactValue) },
                onConfirmContact = { onConfirmContact(accident.contactRequestId, contactValue) },
            )
        }
    }
}

@Composable
private fun ProfileScreen(
    session: StudentSessionUi,
    apiBaseUrl: String,
    onApiBaseUrlChange: (String) -> Unit,
    activeReports: Int,
    leaderboardEntries: List<LeaderboardEntryUi>,
    profileStatusMessage: String,
    isLeaderboardLoading: Boolean,
    reportsUnderReview: List<TrafficReportUi>,
    accidents: List<AccidentPostUi>,
    postingRestricted: Boolean,
    adminStatusMessage: String,
    isAdminLoading: Boolean,
    isAdminActionRunning: Boolean,
    onRefreshProfile: () -> Unit,
    onRefreshLeaderboard: () -> Unit,
    onRefreshAdmin: () -> Unit,
    onApproveReport: (TrafficReportUi) -> Unit,
    onHideReport: (TrafficReportUi) -> Unit,
    onHideAccident: (AccidentPostUi) -> Unit,
    onRestrictUser: (String?, String) -> Unit,
    onLogout: () -> Unit,
    contentPadding: PaddingValues,
) {
    var panel by rememberSaveable { mutableStateOf(ProfilePanel.Overview) }
    val panels = ProfilePanel.entries.filter { it != ProfilePanel.Admin || session.isDemoAdmin }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            AppHeader(
                title = "我的",
                subtitle = "应用内代码 ${session.publicCode}",
                badge = if (session.isDemoAdmin) {
                    if (session.isDemoMode) "演示管理员" else "管理员"
                } else {
                    "普通用户"
                },
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                panels.forEach { item ->
                    FilterChip(selected = panel == item, onClick = { panel = item }, label = { Text(item.label) })
                }
            }
        }
        when (panel) {
            ProfilePanel.Overview -> item {
                ProfileOverview(
                    session = session,
                    activeReports = activeReports,
                    apiBaseUrl = apiBaseUrl,
                    onApiBaseUrlChange = onApiBaseUrlChange,
                    profileStatusMessage = profileStatusMessage,
                    onRefreshProfile = onRefreshProfile,
                    onLogout = onLogout,
                )
            }
            ProfilePanel.Leaderboard -> item {
                LeaderboardPanel(
                    entries = leaderboardEntries,
                    statusMessage = profileStatusMessage,
                    isLoading = isLeaderboardLoading,
                    onRefresh = onRefreshLeaderboard,
                )
            }
            ProfilePanel.Admin -> item {
                AdminPanel(
                    reportsUnderReview = reportsUnderReview,
                    accidents = accidents,
                    postingRestricted = postingRestricted,
                    adminStatusMessage = adminStatusMessage,
                    isLoading = isAdminLoading,
                    isActionRunning = isAdminActionRunning,
                    onRefresh = onRefreshAdmin,
                    onApproveReport = onApproveReport,
                    onHideReport = onHideReport,
                    onHideAccident = onHideAccident,
                    onRestrictUser = onRestrictUser,
                )
            }
            ProfilePanel.Privacy -> item {
                PrivacySafetyPanel()
            }
        }
    }
}

@Composable
private fun AppHeader(title: String, subtitle: String, badge: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = YueluColors.Ink)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = YueluColors.InkMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        AssistChip(onClick = {}, label = { Text(badge) })
    }
}

@Composable
private fun StatusNotice(title: String, body: String, isWarning: Boolean = false) {
    val container = if (isWarning) Color(0xFFFFEFE8) else YueluColors.CampusGreenSoft
    val content = if (isWarning) YueluColors.AlertRed else YueluColors.CampusGreen
    Card(colors = CardDefaults.cardColors(containerColor = container), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = content, fontWeight = FontWeight.Bold)
            Text(body, style = MaterialTheme.typography.bodySmall, color = YueluColors.Ink)
        }
    }
}

@Composable
private fun MockMapPanel(reports: List<TrafficReportUi>, onSelectReport: (TrafficReportUi) -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = YueluColors.Surface),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("中南大学 - 麓山南路", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(YueluColors.SurfaceMuted),
            ) {
                MockMapCanvas(modifier = Modifier.fillMaxSize())
                reports.filter { it.status == TrafficReportStatus.ACTIVE }.take(4).forEachIndexed { index, report ->
                    MapMarker(report = report, index = index, onClick = { onSelectReport(report) })
                }
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    color = YueluColors.Surface.copy(alpha = 0.94f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = AppCopy.mapSdkDeferred,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = YueluColors.InkMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun MockMapCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRect(color = Color(0xFFE8F3EC))
        drawRect(color = Color(0xFFD6E6D6), topLeft = Offset(size.width * 0.05f, size.height * 0.08f), size = Size(size.width * 0.38f, size.height * 0.28f))
        drawRect(color = Color(0xFFDBEAF5), topLeft = Offset(size.width * 0.63f, size.height * 0.07f), size = Size(size.width * 0.25f, size.height * 0.18f))
        drawLine(
            color = Color(0xFFFFF6E7),
            start = Offset(size.width * 0.02f, size.height * 0.62f),
            end = Offset(size.width * 0.98f, size.height * 0.36f),
            strokeWidth = 34f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = YueluColors.RoadOrange.copy(alpha = 0.65f),
            start = Offset(size.width * 0.02f, size.height * 0.62f),
            end = Offset(size.width * 0.98f, size.height * 0.36f),
            strokeWidth = 4f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Color.White,
            start = Offset(size.width * 0.36f, size.height * 0.05f),
            end = Offset(size.width * 0.56f, size.height * 0.95f),
            strokeWidth = 24f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = YueluColors.Divider,
            start = Offset(size.width * 0.36f, size.height * 0.05f),
            end = Offset(size.width * 0.56f, size.height * 0.95f),
            strokeWidth = 2f,
            cap = StrokeCap.Round,
        )
        val path = Path().apply {
            moveTo(size.width * 0.08f, size.height * 0.82f)
            cubicTo(size.width * 0.18f, size.height * 0.73f, size.width * 0.24f, size.height * 0.89f, size.width * 0.36f, size.height * 0.78f)
            cubicTo(size.width * 0.47f, size.height * 0.66f, size.width * 0.62f, size.height * 0.77f, size.width * 0.78f, size.height * 0.63f)
        }
        drawPath(path = path, color = Color(0xFFA7CFB5), style = Stroke(width = 8f, cap = StrokeCap.Round))
    }
}

@Composable
private fun MapMarker(report: TrafficReportUi, index: Int, onClick: () -> Unit) {
    val positions = listOf(44.dp to 168.dp, 138.dp to 118.dp, 232.dp to 92.dp, 276.dp to 194.dp)
    val position = positions[index % positions.size]
    Box(
        modifier = Modifier
            .offset(x = position.first, y = position.second)
            .size(42.dp)
            .clip(CircleShape)
            .background(markerColor(report.type, report.confidenceScore))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(report.type.label.take(1), color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ReportFeed(
    reports: List<TrafficReportUi>,
    onSelectReport: (TrafficReportUi) -> Unit,
    onFeedback: (String, FeedbackChoice) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("附近路况", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${reports.count { it.status == TrafficReportStatus.ACTIVE }} 条生效", style = MaterialTheme.typography.bodySmall, color = YueluColors.InkMuted)
        }
        reports.forEach { report ->
            ReportCard(report = report, onSelect = { onSelectReport(report) }, onFeedback = onFeedback)
        }
    }
}

@Composable
private fun ReportCard(report: TrafficReportUi, onSelect: () -> Unit, onFeedback: (String, FeedbackChoice) -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.elevatedCardColors(containerColor = YueluColors.Surface),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(report.type.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(trafficStatusLabel(report.status), style = MaterialTheme.typography.bodySmall, color = YueluColors.CampusGreen)
            }
            Text(report.locationLabel, style = MaterialTheme.typography.bodyMedium, color = YueluColors.Ink)
            if (report.description.isNotBlank()) {
                Text(report.description, style = MaterialTheme.typography.bodySmall, color = YueluColors.InkMuted)
            }
            Text("可信度 ${report.confidenceScore} / 100", style = MaterialTheme.typography.bodySmall, color = YueluColors.InkMuted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    enabled = report.status == TrafficReportStatus.ACTIVE,
                    onClick = { onFeedback(report.id, FeedbackChoice.CONFIRM_VALID) },
                ) {
                    Text(feedbackLabel(FeedbackChoice.CONFIRM_VALID))
                }
                OutlinedButton(
                    enabled = report.status == TrafficReportStatus.ACTIVE,
                    onClick = { onFeedback(report.id, FeedbackChoice.MARK_EXPIRED) },
                ) {
                    Text(feedbackLabel(FeedbackChoice.MARK_EXPIRED))
                }
            }
        }
    }
}

@Composable
private fun ReportDetailDialog(report: TrafficReportUi, onDismiss: () -> Unit, onFeedback: (FeedbackChoice) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(report.type.label) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("地点：${report.locationLabel}")
                Text("状态：${trafficStatusLabel(report.status)}")
                Text("可信度：${report.confidenceScore} / 100")
                Text(report.description.ifBlank { "暂无补充说明。" })
            }
        },
        confirmButton = {
            TextButton(onClick = { onFeedback(FeedbackChoice.CONFIRM_VALID) }) {
                Text(feedbackLabel(FeedbackChoice.CONFIRM_VALID))
            }
        },
        dismissButton = {
            TextButton(onClick = { onFeedback(FeedbackChoice.MARK_EXPIRED) }) {
                Text(feedbackLabel(FeedbackChoice.MARK_EXPIRED))
            }
        },
    )
}

@Composable
private fun AccidentCard(
    accident: AccidentPostUi,
    contactValue: String,
    onContactChange: (String) -> Unit,
    onRequestContact: () -> Unit,
    onConfirmContact: () -> Unit,
) {
    ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = YueluColors.Surface), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(accident.locationLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(accident.description, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${accidentStatusLabel(accident.status)} · ${contactStatusLabel(accident.contactExchangeStatus)}",
                style = MaterialTheme.typography.bodySmall,
                color = YueluColors.InkMuted,
            )
            if (accident.contactRequestId != null) {
                Text(
                    text = "联系方式请求 ID：${accident.contactRequestId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = YueluColors.InkMuted,
                )
            }
            OutlinedTextField(
                value = contactValue,
                onValueChange = onContactChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("私密联系方式") },
                singleLine = true,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    enabled = accident.contactExchangeStatus == ContactExchangeStatus.NONE,
                    onClick = onRequestContact,
                ) {
                    Text("申请交换")
                }
                OutlinedButton(
                    enabled = accident.contactExchangeStatus == ContactExchangeStatus.PENDING,
                    onClick = onConfirmContact,
                ) {
                    Text("双方确认")
                }
            }
            accident.visibleContacts.forEach { contact ->
                Text(contact, style = MaterialTheme.typography.bodySmall, color = YueluColors.CampusGreen)
            }
        }
    }
}

@Composable
private fun ProfileOverview(
    session: StudentSessionUi,
    activeReports: Int,
    apiBaseUrl: String,
    onApiBaseUrlChange: (String) -> Unit,
    profileStatusMessage: String,
    onRefreshProfile: () -> Unit,
    onLogout: () -> Unit,
) {
    ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = YueluColors.Surface), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("账号概览", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("展示代码：${session.publicCode}")
            Text("角色：${roleLabel(session.role)}")
            Text("称号：${titleLabel(session.titleCode)}")
            Text("信誉：${session.reputationScore} · 积分：${session.points}")
            Text("当前生效上报：$activeReports 条")
            Text("限制状态：${session.postingBanUntil ?: "未限制"}")
            Text("模式：${if (session.isDemoMode) "本地演示" else "后端在线"}")
            Text(profileStatusMessage, style = MaterialTheme.typography.bodySmall, color = YueluColors.InkMuted)
            OutlinedTextField(
                value = apiBaseUrl,
                onValueChange = onApiBaseUrlChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("后端地址") },
                supportingText = { Text("修改后会影响下一次请求；真机可填写电脑局域网地址。") },
                singleLine = true,
            )
            Text("学号不会公开展示，排行榜仅展示应用内代码。", style = MaterialTheme.typography.bodySmall, color = YueluColors.InkMuted)
            Button(onClick = onRefreshProfile, modifier = Modifier.fillMaxWidth()) {
                Text("刷新后端资料")
            }
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                Text("退出登录")
            }
        }
    }
}

@Composable
private fun LeaderboardPanel(
    entries: List<LeaderboardEntryUi>,
    statusMessage: String,
    isLoading: Boolean,
    onRefresh: () -> Unit,
) {
    ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = YueluColors.Surface), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("校园互助排行榜", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(AppCopy.leaderboardDemoNotice, style = MaterialTheme.typography.bodySmall, color = YueluColors.InkMuted)
            Text(statusMessage, style = MaterialTheme.typography.bodySmall, color = YueluColors.InkMuted)
            OutlinedButton(enabled = !isLoading, onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
                Text(if (isLoading) "正在刷新排行榜" else "刷新排行榜")
            }
            entries.forEach { entry ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${entry.rank}. ${entry.publicCode}", fontWeight = if (entry.rank == 1) FontWeight.Bold else FontWeight.Normal)
                    Text("${entry.points} 分 · ${entry.title}", color = YueluColors.InkMuted)
                }
            }
        }
    }
}

@Composable
private fun AdminPanel(
    reportsUnderReview: List<TrafficReportUi>,
    accidents: List<AccidentPostUi>,
    postingRestricted: Boolean,
    adminStatusMessage: String,
    isLoading: Boolean,
    isActionRunning: Boolean,
    onRefresh: () -> Unit,
    onApproveReport: (TrafficReportUi) -> Unit,
    onHideReport: (TrafficReportUi) -> Unit,
    onHideAccident: (AccidentPostUi) -> Unit,
    onRestrictUser: (String?, String) -> Unit,
) {
    ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = YueluColors.Surface), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("管理员面板", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(AppCopy.adminDemoNotice, style = MaterialTheme.typography.bodySmall, color = YueluColors.InkMuted)
            Text(adminStatusMessage, style = MaterialTheme.typography.bodySmall, color = YueluColors.InkMuted)
            OutlinedButton(enabled = !isLoading && !isActionRunning, onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
                Text(if (isLoading) "正在刷新审核队列" else "刷新审核队列")
            }
            Text("待复核路况：${reportsUnderReview.size} 条")
            if (reportsUnderReview.isEmpty()) {
                Text("暂无待复核路况。", style = MaterialTheme.typography.bodySmall, color = YueluColors.InkMuted)
            }
            reportsUnderReview.take(4).forEach { report ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(YueluColors.SurfaceMuted, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(report.locationLabel, fontWeight = FontWeight.SemiBold)
                    Text("${report.type.label} · ${report.submitterPublicCode ?: "未知用户"}", color = YueluColors.InkMuted)
                    Text(report.description, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Button(
                        enabled = !isActionRunning,
                        onClick = { onApproveReport(report) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("通过该路况")
                    }
                    OutlinedButton(
                        enabled = !isActionRunning,
                        onClick = { onHideReport(report) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("隐藏该路况")
                    }
                    OutlinedButton(
                        enabled = !isActionRunning && report.submitterId != null,
                        onClick = { onRestrictUser(report.submitterId, "管理员基于待复核路况限制上报 24 小时。") },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("限制提交者 24 小时")
                    }
                }
            }
            Text("开放互助信息：${accidents.count { it.status == AccidentPostStatus.OPEN }} 条")
            Text("当前账号限制状态：${if (postingRestricted) "已限制" else "未限制"}")
            accidents.filter { it.status == AccidentPostStatus.OPEN }.take(4).forEach { accident ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(YueluColors.SurfaceMuted, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(accident.locationLabel, fontWeight = FontWeight.SemiBold)
                    Text(accident.createdByPublicCode ?: "未知发布者", color = YueluColors.InkMuted)
                    Text(accident.description, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    OutlinedButton(
                        enabled = !isActionRunning,
                        onClick = { onHideAccident(accident) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("隐藏该事故互助")
                    }
                    OutlinedButton(
                        enabled = !isActionRunning && accident.createdByUserId != null,
                        onClick = { onRestrictUser(accident.createdByUserId, "管理员基于事故互助信息限制上报 24 小时。") },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("限制发布者 24 小时")
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacySafetyPanel() {
    ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = YueluColors.Surface), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(AppCopy.privacySafetyTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(AppCopy.privacySafetyBody, style = MaterialTheme.typography.bodyMedium, color = YueluColors.Ink)
            Text("地图 SDK 密钥、后端密钥和签名凭据不应提交到 Git。", style = MaterialTheme.typography.bodySmall, color = YueluColors.InkMuted)
        }
    }
}

private fun leaderboardEntries(session: StudentSessionUi, activeReports: Int): List<LeaderboardEntryUi> = listOf(
    LeaderboardEntryUi(1, session.publicCode, session.points + activeReports * 2, titleLabel(session.titleCode)),
    LeaderboardEntryUi(2, "同学-8A19C2", 22, "通勤雷达"),
    LeaderboardEntryUi(3, "同学-31F0B7", 18, "安全提醒员"),
)

private fun BackendAuthSession.toUiSession(connectionMessage: String): StudentSessionUi {
    return user.toUiSession(accessToken = accessToken, connectionMessage = connectionMessage)
}

private fun BackendUserProfile.toUiSession(accessToken: String, connectionMessage: String): StudentSessionUi {
    return StudentSessionUi(
        publicCode = publicCode,
        accessToken = accessToken,
        role = role,
        reputationScore = reputationScore,
        points = points,
        titleCode = titleCode,
        postingBanUntil = postingBanUntil,
        connectionMessage = connectionMessage,
        isDemoMode = false,
    )
}

private fun markerColor(type: TrafficReportType, confidence: Int): Color {
    val strong = confidence >= 60
    return when (type) {
        TrafficReportType.TRAFFIC_MANAGEMENT -> if (strong) YueluColors.AdminBlue else Color(0xFF6CA8D8)
        TrafficReportType.CONSTRUCTION -> if (strong) Color(0xFF9C6B1F) else YueluColors.RoadOrange
        TrafficReportType.CONGESTION -> if (strong) YueluColors.AlertRed else Color(0xFFE57B72)
        TrafficReportType.ROAD_CONTROL -> if (strong) Color(0xFF6C4E9B) else Color(0xFF9A7CCE)
        TrafficReportType.ACCIDENT_OR_HAZARD -> if (strong) YueluColors.CampusGreen else Color(0xFF58A86F)
    }
}
