package com.yuelutraffic.app.ui

import com.yuelutraffic.app.accidents.AccidentPostStatus
import com.yuelutraffic.app.accidents.ContactExchangeStatus
import com.yuelutraffic.app.traffic.FeedbackChoice
import com.yuelutraffic.app.traffic.TrafficReportStatus

object AppCopy {
    const val appName = "Yuelu Traffic"
    const val appSubtitle = "中南大学周边路况互助"
    const val demoModeNotice = "当前为本地演示数据，尚未连接实时后端。"
    const val mapSdkDeferred = "地图为组件化模拟视图；配置地图 SDK 后会优先使用真实地图。"
    const val lawfulUse = "仅用于共享道路安全与拥堵信息，请遵守交通规则。"
    const val accidentDemoNotice = "事故互助栏已支持后端接入；联系方式仍需双方确认后才显示。"
    const val leaderboardDemoNotice = "排行榜展示应用内代码，不展示学号。"
    const val adminDemoNotice = "管理员操作会影响后端数据，请谨慎确认。"
    const val privacySafetyTitle = "隐私与安全"
    const val privacySafetyBody =
        "学号只用于登录和生成应用内公开代码；路况信息用于校园周边道路安全互助；事故联系方式默认隐藏，双方确认后才显示。请不要使用本应用传播违法规避、绕行执法或泄露他人隐私的信息。"
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
    Privacy("隐私"),
}

data class StudentSessionUi(
    val publicCode: String,
    val accessToken: String? = null,
    val role: String = "DEMO",
    val reputationScore: Int = 60,
    val points: Int = 24,
    val titleCode: String = "LOCAL_HELPER",
    val postingBanUntil: String? = null,
    val connectionMessage: String = AppCopy.demoModeNotice,
    val isDemoMode: Boolean = true,
) {
    val isDemoAdmin: Boolean
        get() = isDemoMode || role == "ADMIN"
}

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

fun roleLabel(role: String): String = when (role) {
    "ADMIN" -> "管理员"
    "USER" -> "普通用户"
    else -> "演示用户"
}

fun titleLabel(titleCode: String): String = when (titleCode) {
    "ROAD_HELPER" -> "道路互助员"
    "LOCAL_HELPER" -> "麓山观察员"
    else -> "校园路况伙伴"
}
