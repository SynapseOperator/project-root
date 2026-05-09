package com.yuelutraffic.app.ui

import com.yuelutraffic.app.accidents.AccidentPostStatus
import com.yuelutraffic.app.accidents.ContactExchangeStatus
import com.yuelutraffic.app.traffic.FeedbackChoice
import com.yuelutraffic.app.traffic.TrafficReportStatus

object AppCopy {
    const val appName = "Yuelu Traffic"
    const val appSubtitle = "中南大学周边路况互助"
    const val demoModeNotice = "当前为本地演示数据，尚未连接实时后端。"
    const val mapSdkDeferred = "地图为组件化模拟视图，后续可替换为正式地图 SDK。"
    const val lawfulUse = "仅用于共享道路安全与拥堵信息，请遵守交通规则。"
    const val backendDeferred = "核心后端连接将在后续里程碑接入。"
}

enum class MainTab(
    val label: String,
    val iconText: String,
) {
    Map("地图", "图"),
    Report("上报", "报"),
    Accidents("事故栏", "事"),
    Profile("我的", "我"),
}

enum class ProfilePanel(val label: String) {
    Overview("资料"),
    Leaderboard("排行榜"),
    Admin("管理员"),
}

data class StudentSessionUi(
    val publicCode: String,
    val isDemoMode: Boolean = true,
    val isDemoAdmin: Boolean = true,
)

data class LeaderboardEntryUi(
    val rank: Int,
    val publicCode: String,
    val points: Int,
    val title: String,
)

fun trafficStatusLabel(status: TrafficReportStatus): String = when (status) {
    TrafficReportStatus.ACTIVE -> "生效中"
    TrafficReportStatus.EXPIRED -> "已过期"
    TrafficReportStatus.HIDDEN -> "已隐藏"
    TrafficReportStatus.UNDER_REVIEW -> "待复核"
}

fun feedbackLabel(choice: FeedbackChoice): String = when (choice) {
    FeedbackChoice.CONFIRM_VALID -> "仍然准确"
    FeedbackChoice.MARK_EXPIRED -> "已经变化"
}

fun accidentStatusLabel(status: AccidentPostStatus): String = when (status) {
    AccidentPostStatus.OPEN -> "等待互助"
    AccidentPostStatus.MATCHED -> "已互认"
    AccidentPostStatus.HIDDEN -> "已隐藏"
}

fun contactStatusLabel(status: ContactExchangeStatus): String = when (status) {
    ContactExchangeStatus.NONE -> "未申请"
    ContactExchangeStatus.PENDING -> "等待对方确认"
    ContactExchangeStatus.MUTUALLY_CONFIRMED -> "双方已确认"
}
