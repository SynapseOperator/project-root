package com.yuelutraffic.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yuelutraffic.app.auth.PRIVACY_NOTICE
import com.yuelutraffic.app.auth.publicCodeForStudentNumber

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
            text = "Signed in as $publicCode",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = "Your student number is not shown publicly.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
