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
import com.yuelutraffic.app.network.ApiResult
import com.yuelutraffic.app.network.BackendAuthSession
import com.yuelutraffic.app.network.BackendUserProfile
import com.yuelutraffic.app.network.YueluApiClient
import com.yuelutraffic.app.traffic.FeedbackChoice
import com.yuelutraffic.app.traffic.TrafficReportStatus
import com.yuelutraffic.app.traffic.TrafficReportType
import com.yuelutraffic.app.traffic.TrafficReportUi
import com.yuelutraffic.app.traffic.applyFeedback
import com.yuelutraffic.app.traffic.createTrafficReport
import com.yuelutraffic.app.traffic.sampleTrafficReports

@Composable
fun YueluTrafficApp() {
    val apiClient = remember { YueluApiClient() }
    var session by remember { mutableStateOf<StudentSessionUi?>(null) }
    var isLoggingIn by rememberSaveable { mutableStateOf(false) }
    var loginError by rememberSaveable { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        val signedInSession = session
        if (signedInSession == null) {
            LoginScreen(
                isLoading = isLoggingIn,
                errorMessage = loginError,
                onLogin = { studentNumber ->
                    isLoggingIn = true
                    loginError = null
                    apiClient.studentLogin(studentNumber) { result ->
                        isLoggingIn = false
                        when (result) {
                            is ApiResult.Success -> {
                                val backendSession = result.value
                                session = backendSession.toUiSession("已连接后端登录服务，正在刷新当前用户。")
                                apiClient.fetchMe(backendSession.accessToken) { meResult ->
                                    session = when (meResult) {
                                        is ApiResult.Success -> meResult.value.toUiSession(
                                            accessToken = backendSession.accessToken,
                                            connectionMessage = "已通过后端 /api/v1/me 获取当前用户会话。",
                                        )
                                        is ApiResult.Failure -> backendSession.toUiSession(
                                            "登录成功，但刷新当前用户失败：${meResult.message}",
                                        )
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
                    loginError = null
                    session = StudentSessionUi(
                        publicCode = displayCode,
                        connectionMessage = "当前为本地演示数据，未连接后端服务。",
                    )
                },
            )
        } else {
            MainShell(session = signedInSession, apiClient = apiClient)
        }
    }
}

@Composable
private fun LoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onLogin: (String) -> Unit,
    onUseDemo: (String) -> Unit,
) {
    var studentNumber by rememberSaveable { mutableStateOf("") }
    var acknowledged by rememberSaveable { mutableStateOf(false) }
    val canContinue = studentNumber.trim().length >= 4 && acknowledged

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YueluColors.Page)
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = AppCopy.appName,
            style = MaterialTheme.typography.headlineLarge,
            color = YueluColors.Ink,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = AppCopy.appSubtitle,
            style = MaterialTheme.typography.titleMedium,
            color = YueluColors.CampusGreen,
        )
        Spacer(modifier = Modifier.height(28.dp))
        OutlinedTextField(
            value = studentNumber,
            onValueChange = { studentNumber = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("学号") },
            supportingText = { Text("仅用于生成应用内展示代码") },
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Checkbox(
                checked = acknowledged,
                onCheckedChange = { acknowledged = it },
            )
            Text(
                text = PRIVACY_NOTICE,
                style = MaterialTheme.typography.bodyMedium,
                color = YueluColors.InkMuted,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
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
            StatusNotice(
                title = "后端连接失败",
                body = errorMessage,
                isWarning = true,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = AppCopy.lawfulUse,
            style = MaterialTheme.typography.bodySmall,
            color = YueluColors.InkMuted,
        )
    }
}

@Composable
private fun MainShell(session: StudentSessionUi, apiClient: YueluApiClient) {
    val reports = remember { mutableStateListOf<TrafficReportUi>().also { it.addAll(sampleTrafficReports()) } }
    val accidents = remember { mutableStateListOf<AccidentPostUi>().also { it.addAll(sampleAccidentPosts()) } }
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Map) }
    var postingRestricted by rememberSaveable { mutableStateOf(false) }
    var selectedReport by remember { mutableStateOf<TrafficReportUi?>(null) }
    var isReportLoading by rememberSaveable { mutableStateOf(false) }
    var isReportSubmitting by rememberSaveable { mutableStateOf(false) }
    var reportStatusMessage by rememberSaveable {
        mutableStateOf(
            if (session.isDemoMode) {
                "当前显示本地演示路况，未连接后端列表。"
            } else {
                "正在从后端加载麓山南路附近路况。"
            },
        )
    }

    fun replaceReport(updated: TrafficReportUi) {
        val index = reports.indexOfFirst { it.id == updated.id }
        if (index >= 0) {
            reports[index] = updated
        } else {
            reports.add(0, updated)
        }
        if (selectedReport?.id == updated.id) {
            selectedReport = updated
        }
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
                    if (reports.isEmpty()) {
                        reports.addAll(sampleTrafficReports())
                    }
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
                    is ApiResult.Failure -> {
                        reportStatusMessage = "详情刷新失败：${result.message}"
                    }
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
                is ApiResult.Failure -> {
                    reportStatusMessage = "路况提交失败：${result.message}"
                }
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
                is ApiResult.Failure -> {
                    reportStatusMessage = "反馈提交失败：${result.message}"
                }
            }
        }
    }

    LaunchedEffect(session.accessToken, session.isDemoMode) {
        if (!session.isDemoMode) {
            loadBackendReports()
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
                        icon = {
                            Text(
                                text = tab.iconText,
                                fontWeight = FontWeight.Bold,
                            )
                        },
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
                onRefreshReports = { loadBackendReports() },
                onSelectReport = { selectReport(it) },
                onFeedback = { reportId, choice -> applyReportFeedback(reportId, choice) },
                contentPadding = innerPadding,
            )
            MainTab.Report -> SubmitReportScreen(
                postingRestricted = postingRestricted,
                isSubmitting = isReportSubmitting,
                statusMessage = reportStatusMessage,
                isBackendMode = !session.isDemoMode,
                onSubmit = { type, locationLabel, description ->
                    submitReport(type, locationLabel, description)
                },
                contentPadding = innerPadding,
            )
            MainTab.Accidents -> AccidentBoardScreen(
                accidents = accidents,
                onAdd = { accidents.add(0, it) },
                onUpdate = { updated ->
                    val index = accidents.indexOfFirst { it.id == updated.id }
                    if (index >= 0) accidents[index] = updated
                },
                contentPadding = innerPadding,
            )
            MainTab.Profile -> ProfileScreen(
                session = session,
                activeReports = reports.count { it.status == TrafficReportStatus.ACTIVE },
                accidents = accidents,
                postingRestricted = postingRestricted,
                onHideReport = {
                    val index = reports.indexOfFirst {
                        it.status == TrafficReportStatus.ACTIVE || it.status == TrafficReportStatus.UNDER_REVIEW
                    }
                    if (index >= 0) reports[index] = reports[index].copy(status = TrafficReportStatus.HIDDEN)
                },
                onHideAccident = {
                    val index = accidents.indexOfFirst { it.status == AccidentPostStatus.OPEN }
                    if (index >= 0) accidents[index] = accidents[index].copy(status = AccidentPostStatus.HIDDEN)
                },
                onToggleRestriction = { postingRestricted = !postingRestricted },
                contentPadding = innerPadding,
            )
        }
    }

    selectedReport?.let { report ->
        ReportDetailDialog(
            report = report,
            onDismiss = { selectedReport = null },
            onFeedback = { choice ->
                applyReportFeedback(report.id, choice)
            },
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
                title = "麓山南路实时路况",
                subtitle = "你好，${session.publicCode}",
                badge = "地图优先",
            )
        }
        item {
            StatusNotice(
                title = if (session.isDemoMode) "演示模式" else if (isReportLoading) "正在同步" else "后端路况",
                body = "${session.connectionMessage} $reportStatusMessage",
            )
        }
        item {
            OutlinedButton(
                enabled = !isReportLoading,
                onClick = onRefreshReports,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isReportLoading) "正在刷新路况" else "刷新路况")
            }
        }
        item {
            MockMapPanel(reports = reports, onSelectReport = onSelectReport)
        }
        item {
            ReportFeed(
                reports = reports,
                onSelectReport = onSelectReport,
                onFeedback = onFeedback,
            )
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
        item {
            AppHeader(
                title = "上报路况",
                subtitle = "补充你看到的道路安全与通行信息",
                badge = "30 秒完成",
            )
        }
        if (postingRestricted) {
            item {
                StatusNotice(
                    title = "暂不可上报",
                    body = "当前账号处于演示限制状态，请稍后再试。",
                    isWarning = true,
                )
            }
        }
        item {
            StatusNotice(
                title = if (isBackendMode) "后端提交" else "演示提交",
                body = statusMessage,
            )
        }
        item {
            ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = YueluColors.Surface)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("选择类型", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    TrafficReportType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.label) },
                        )
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
                    Text(
                        text = AppCopy.lawfulUse,
                        style = MaterialTheme.typography.bodySmall,
                        color = YueluColors.InkMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun AccidentBoardScreen(
    accidents: List<AccidentPostUi>,
    onAdd: (AccidentPostUi) -> Unit,
    onUpdate: (AccidentPostUi) -> Unit,
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
        item {
            AppHeader(
                title = "事故互助栏",
                subtitle = "联系方式默认隐藏，双方确认后才显示",
                badge = "隐私优先",
            )
        }
        item {
            StatusNotice(
                title = "本地演示",
                body = AppCopy.accidentDemoNotice,
            )
        }
        item {
            ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = YueluColors.Surface)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
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
                            onAdd(createAccidentPost(locationLabel, description.ifBlank { "轻微事故，等待互助确认。" }))
                            description = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("发布互助信息")
                    }
                }
            }
        }
        items(accidents, key = { it.id }) { accident ->
            AccidentCard(
                accident = accident,
                contactValue = contactValue,
                onContactChange = { contactValue = it },
                onUpdate = onUpdate,
            )
        }
    }
}

@Composable
private fun ProfileScreen(
    session: StudentSessionUi,
    activeReports: Int,
    accidents: List<AccidentPostUi>,
    postingRestricted: Boolean,
    onHideReport: () -> Unit,
    onHideAccident: () -> Unit,
    onToggleRestriction: () -> Unit,
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
                    FilterChip(
                        selected = panel == item,
                        onClick = { panel = item },
                        label = { Text(item.label) },
                    )
                }
            }
        }
        when (panel) {
            ProfilePanel.Overview -> item {
                ProfileOverview(session = session, activeReports = activeReports)
            }
            ProfilePanel.Leaderboard -> item {
                LeaderboardPanel(
                    entries = leaderboardEntries(session, activeReports),
                )
            }
            ProfilePanel.Admin -> item {
                AdminPanel(
                    accidents = accidents,
                    postingRestricted = postingRestricted,
                    onHideReport = onHideReport,
                    onHideAccident = onHideAccident,
                    onToggleRestriction = onToggleRestriction,
                )
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
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = YueluColors.Ink,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = YueluColors.InkMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        AssistChip(
            onClick = {},
            label = { Text(badge) },
        )
    }
}

@Composable
private fun StatusNotice(title: String, body: String, isWarning: Boolean = false) {
    val container = if (isWarning) Color(0xFFFFEFE8) else YueluColors.CampusGreenSoft
    val content = if (isWarning) YueluColors.AlertRed else YueluColors.CampusGreen
    Card(
        colors = CardDefaults.cardColors(containerColor = container),
        shape = RoundedCornerShape(8.dp),
    ) {
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
            Text(
                text = "中南大学 - 麓山南路",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(YueluColors.SurfaceMuted),
            ) {
                MockMapCanvas(modifier = Modifier.fillMaxSize())
                reports.filter { it.status == TrafficReportStatus.ACTIVE }.take(4).forEachIndexed { index, report ->
                    MapMarker(
                        report = report,
                        index = index,
                        onClick = { onSelectReport(report) },
                    )
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
            color = Color(0xFFFFFFFF),
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
    val positions = listOf(
        44.dp to 168.dp,
        138.dp to 118.dp,
        232.dp to 92.dp,
        276.dp to 194.dp,
    )
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
        Text(
            text = report.type.label.take(1),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ReportFeed(
    reports: List<TrafficReportUi>,
    onSelectReport: (TrafficReportUi) -> Unit,
    onFeedback: (String, FeedbackChoice) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("附近路况", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${reports.count { it.status == TrafficReportStatus.ACTIVE }} 条生效", style = MaterialTheme.typography.bodySmall, color = YueluColors.InkMuted)
        }
        reports.forEach { report ->
            ReportCard(report = report, onSelect = { onSelectReport(report) }, onFeedback = onFeedback)
        }
    }
}

@Composable
private fun ReportCard(
    report: TrafficReportUi,
    onSelect: () -> Unit,
    onFeedback: (String, FeedbackChoice) -> Unit,
) {
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
private fun ReportDetailDialog(
    report: TrafficReportUi,
    onDismiss: () -> Unit,
    onFeedback: (FeedbackChoice) -> Unit,
) {
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
    onUpdate: (AccidentPostUi) -> Unit,
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = YueluColors.Surface),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(accident.locationLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(accident.description, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${accidentStatusLabel(accident.status)} · ${contactStatusLabel(accident.contactExchangeStatus)}",
                style = MaterialTheme.typography.bodySmall,
                color = YueluColors.InkMuted,
            )
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
                    onClick = { onUpdate(accident.requestContact()) },
                ) {
                    Text("申请交换")
                }
                OutlinedButton(
                    enabled = accident.contactExchangeStatus == ContactExchangeStatus.PENDING,
                    onClick = { onUpdate(accident.confirmContact(contactValue)) },
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
private fun ProfileOverview(session: StudentSessionUi, activeReports: Int) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = YueluColors.Surface),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("账号概览", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("展示代码：${session.publicCode}")
            Text("角色：${roleLabel(session.role)}")
            Text("称号：${titleLabel(session.titleCode)}")
            Text("信誉：${session.reputationScore} · 积分：${session.points}")
            Text("当前生效上报：$activeReports 条")
            Text("模式：${if (session.isDemoMode) "本地演示" else "后端在线"}")
            Text(
                text = if (session.isDemoMode) {
                    "当前页面全部使用本地演示数据。"
                } else {
                    "登录和路况已连接后端；事故栏、排行榜、管理员仍按本阶段演示边界处理。"
                },
                style = MaterialTheme.typography.bodySmall,
                color = YueluColors.InkMuted,
            )
            Text("学号不会公开展示，排行榜仅展示应用内代码。", style = MaterialTheme.typography.bodySmall, color = YueluColors.InkMuted)
        }
    }
}

@Composable
private fun LeaderboardPanel(entries: List<LeaderboardEntryUi>) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = YueluColors.Surface),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("校园互助排行榜", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(AppCopy.leaderboardDemoNotice, style = MaterialTheme.typography.bodySmall, color = YueluColors.InkMuted)
            entries.forEach { entry ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("${entry.rank}. ${entry.publicCode}", fontWeight = if (entry.rank == 1) FontWeight.Bold else FontWeight.Normal)
                    Text("${entry.points} 分 · ${entry.title}", color = YueluColors.InkMuted)
                }
            }
        }
    }
}

@Composable
private fun AdminPanel(
    accidents: List<AccidentPostUi>,
    postingRestricted: Boolean,
    onHideReport: () -> Unit,
    onHideAccident: () -> Unit,
    onToggleRestriction: () -> Unit,
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = YueluColors.Surface),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("演示管理员面板", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(AppCopy.adminDemoNotice, style = MaterialTheme.typography.bodySmall, color = YueluColors.InkMuted)
            Text("开放互助信息：${accidents.count { it.status == AccidentPostStatus.OPEN }} 条")
            Text("上报限制：${if (postingRestricted) "已开启" else "未开启"}")
            Button(onClick = onHideReport, modifier = Modifier.fillMaxWidth()) {
                Text("隐藏一条路况")
            }
            OutlinedButton(onClick = onHideAccident, modifier = Modifier.fillMaxWidth()) {
                Text("隐藏一条事故互助")
            }
            OutlinedButton(onClick = onToggleRestriction, modifier = Modifier.fillMaxWidth()) {
                Text(if (postingRestricted) "解除上报限制" else "开启上报限制")
            }
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
