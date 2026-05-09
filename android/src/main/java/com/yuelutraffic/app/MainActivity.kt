package com.yuelutraffic.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.yuelutraffic.app.ui.YueluTrafficApp
import com.yuelutraffic.app.ui.YueluTrafficTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YueluTrafficTheme {
                YueluTrafficApp()
            }
        }
    }
}
