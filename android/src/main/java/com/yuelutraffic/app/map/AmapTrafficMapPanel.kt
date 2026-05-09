package com.yuelutraffic.app.map

import android.content.Context
import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.yuelutraffic.app.traffic.TrafficReportStatus
import com.yuelutraffic.app.traffic.TrafficReportUi
import com.yuelutraffic.app.ui.YueluColors

private val YueluCampusCenter = LatLng(28.1703, 112.9388)

@Composable
fun AmapTrafficMapPanel(
    reports: List<TrafficReportUi>,
    apiKey: String,
    statusMessage: String,
    onSelectReport: (TrafficReportUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    remember(context, apiKey) {
        configureAmapSdk(context.applicationContext, apiKey)
        true
    }
    val mapView = remember {
        MapView(context).apply {
            onCreate(Bundle())
        }
    }
    var amap by remember { mutableStateOf<AMap?>(null) }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    LaunchedEffect(amap, reports.map { "${it.id}:${it.status}:${it.confidenceScore}" }) {
        amap?.let { renderTrafficMarkers(it, reports) }
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = YueluColors.Surface),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("中南大学 - 麓山南路", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(8.dp)),
            ) {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize(),
                    update = { view ->
                        val map = view.map
                        if (amap == null) {
                            amap = map
                            configureMap(map, reports, onSelectReport)
                        }
                    },
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    color = YueluColors.Surface.copy(alpha = 0.94f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = statusMessage,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = YueluColors.InkMuted,
                    )
                }
            }
        }
    }
}

private fun configureAmapSdk(context: Context, apiKey: String) {
    MapsInitializer.updatePrivacyShow(context, true, true)
    MapsInitializer.updatePrivacyAgree(context, true)
    MapsInitializer.setApiKey(apiKey)
    MapsInitializer.initialize(context)
}

private fun configureMap(
    map: AMap,
    reports: List<TrafficReportUi>,
    onSelectReport: (TrafficReportUi) -> Unit,
) {
    map.uiSettings.isZoomControlsEnabled = false
    map.uiSettings.isScaleControlsEnabled = true
    map.isTrafficEnabled = true
    map.moveCamera(CameraUpdateFactory.newLatLngZoom(YueluCampusCenter, 15f))
    map.setOnMarkerClickListener { marker ->
        val reportId = marker.getObject() as? String
        val report = reports.firstOrNull { it.id == reportId }
        if (report != null) onSelectReport(report)
        marker.showInfoWindow()
        true
    }
    renderTrafficMarkers(map, reports)
}

private fun renderTrafficMarkers(map: AMap, reports: List<TrafficReportUi>) {
    map.clear()
    val activeReports = reports.filter { it.status == TrafficReportStatus.ACTIVE }
    activeReports.take(50).forEach { report ->
        val marker = map.addMarker(
            MarkerOptions()
                .position(LatLng(report.latitude, report.longitude))
                .title(report.locationLabel)
                .snippet("${report.type.label} · 可信度 ${report.confidenceScore}"),
        )
        marker?.setObject(report.id)
    }
}
