package com.yuelutraffic.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yuelutraffic.app.auth.PRIVACY_NOTICE
import com.yuelutraffic.app.auth.publicCodeForStudentNumber
import com.yuelutraffic.app.traffic.FeedbackChoice
import com.yuelutraffic.app.traffic.TrafficReportStatus
import com.yuelutraffic.app.traffic.TrafficReportType
import com.yuelutraffic.app.traffic.TrafficReportUi
import com.yuelutraffic.app.traffic.applyFeedback
import com.yuelutraffic.app.traffic.createTrafficReport
import com.yuelutraffic.app.traffic.sampleTrafficReports

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YueluTrafficApp()
        }
    }
}

@Composable
fun YueluTrafficApp() {
    var publicCode by rememberSaveable { mutableStateOf<String?>(null) }
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            if (publicCode == null) {
                StudentEntryScreen(
                    onEnter = { studentNumber ->
                        publicCode = publicCodeForStudentNumber(studentNumber)
                    },
                )
            } else {
                SignedInSkeleton(publicCode = publicCode.orEmpty())
            }
        }
    }
}

@Composable
private fun StudentEntryScreen(onEnter: (String) -> Unit) {
    var studentNumber by rememberSaveable { mutableStateOf("") }
    var acknowledged by rememberSaveable { mutableStateOf(false) }
    val canContinue = studentNumber.trim().length >= 4 && acknowledged

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Yuelu Traffic",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Campus traffic safety and road condition reporting",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = studentNumber,
            onValueChange = { studentNumber = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Student number") },
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Checkbox(
                checked = acknowledged,
                onCheckedChange = { acknowledged = it },
            )
            Text(
                text = PRIVACY_NOTICE,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            enabled = canContinue,
            onClick = { onEnter(studentNumber) },
        ) {
            Text("Enter app")
        }
    }
}

@Composable
private fun SignedInSkeleton(publicCode: String) {
    val reports = remember { mutableStateListOf<TrafficReportUi>().also { it.addAll(sampleTrafficReports()) } }
    var selectedTab by rememberSaveable { mutableStateOf("Map") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Yuelu Traffic",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Signed in as $publicCode",
            style = MaterialTheme.typography.bodyLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { selectedTab = "Map" }) { Text("Map") }
            Button(onClick = { selectedTab = "Submit" }) { Text("Submit") }
            Button(onClick = { selectedTab = "Leaderboard" }) { Text("Leaderboard") }
        }
        when (selectedTab) {
            "Submit" -> SubmitReportPanel(
                onSubmit = { report -> reports.add(0, report) },
            )
            "Leaderboard" -> LeaderboardPanel(
                publicCode = publicCode,
                reportCount = reports.count { it.status == TrafficReportStatus.ACTIVE },
            )
            else -> TrafficMapPanel(
                reports = reports,
                onFeedback = { reportId, choice ->
                    val index = reports.indexOfFirst { it.id == reportId }
                    if (index >= 0) {
                        reports[index] = reports[index].applyFeedback(choice)
                    }
                },
            )
        }
    }
}

@Composable
private fun TrafficMapPanel(
    reports: List<TrafficReportUi>,
    onFeedback: (String, FeedbackChoice) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(Color(0xFFEAF4EF))
                .border(1.dp, Color(0xFF5F766B))
                .padding(12.dp),
        ) {
            Text("Central South University / Lushan South Road")
            reports.filter { it.status == TrafficReportStatus.ACTIVE }.forEachIndexed { index, report ->
                Text(
                    text = "●",
                    color = markerColor(report.type, report.confidenceScore),
                    modifier = Modifier.offset(
                        x = (32 + index * 42).dp,
                        y = (64 + index * 34).dp,
                    ),
                )
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(reports, key = { it.id }) { report ->
                ReportCard(report = report, onFeedback = onFeedback)
            }
        }
    }
}

@Composable
private fun ReportCard(report: TrafficReportUi, onFeedback: (String, FeedbackChoice) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(report.type.label, style = MaterialTheme.typography.titleMedium)
            Text(report.locationLabel, style = MaterialTheme.typography.bodyMedium)
            if (report.description.isNotBlank()) {
                Text(report.description, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = "Confidence ${report.confidenceScore} · ${report.status}",
                style = MaterialTheme.typography.bodySmall,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    enabled = report.status == TrafficReportStatus.ACTIVE,
                    onClick = { onFeedback(report.id, FeedbackChoice.CONFIRM_VALID) },
                ) {
                    Text("Confirm")
                }
                Button(
                    enabled = report.status == TrafficReportStatus.ACTIVE,
                    onClick = { onFeedback(report.id, FeedbackChoice.MARK_EXPIRED) },
                ) {
                    Text("No longer valid")
                }
            }
        }
    }
}

@Composable
private fun SubmitReportPanel(onSubmit: (TrafficReportUi) -> Unit) {
    var selectedType by rememberSaveable { mutableStateOf(TrafficReportType.CONGESTION) }
    var locationLabel by rememberSaveable { mutableStateOf("Lushan South Road") }
    var description by rememberSaveable { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Report type", style = MaterialTheme.typography.titleMedium)
        TrafficReportType.entries.forEach { type ->
            Button(onClick = { selectedType = type }) {
                Text(if (selectedType == type) "Selected: ${type.label}" else type.label)
            }
        }
        OutlinedTextField(
            value = locationLabel,
            onValueChange = { locationLabel = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Location") },
            singleLine = true,
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Optional description") },
        )
        Button(
            onClick = {
                onSubmit(createTrafficReport(selectedType, locationLabel, description))
                description = ""
            },
        ) {
            Text("Submit report")
        }
    }
}

@Composable
private fun LeaderboardPanel(publicCode: String, reportCount: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Leaderboard", style = MaterialTheme.typography.titleMedium)
        Text("$publicCode · ${20 + reportCount * 2} pts · Road helper")
        Text("Public ranking uses app display codes, not student numbers.")
    }
}

private fun markerColor(type: TrafficReportType, confidence: Int): Color {
    val strong = confidence >= 60
    return when (type) {
        TrafficReportType.TRAFFIC_MANAGEMENT -> if (strong) Color(0xFF1565C0) else Color(0xFF64B5F6)
        TrafficReportType.CONSTRUCTION -> if (strong) Color(0xFF8A5A00) else Color(0xFFFFC857)
        TrafficReportType.CONGESTION -> if (strong) Color(0xFFC62828) else Color(0xFFE57373)
        TrafficReportType.ROAD_CONTROL -> if (strong) Color(0xFF6A1B9A) else Color(0xFFBA68C8)
        TrafficReportType.ACCIDENT_OR_HAZARD -> if (strong) Color(0xFF2E7D32) else Color(0xFF81C784)
    }
}
