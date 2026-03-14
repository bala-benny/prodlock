package com.example.testapp

import com.example.testapp.getForegroundApp
import com.example.testapp.TaskManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.testapp.WalletManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TaskManager.addTask("Study", 30)
        TaskManager.addTask("Workout", 20)
        setContent {
            TaskScreen()
        }
    }
}

@Composable
fun TaskScreen() {

    // We use a trigger variable to force Compose to refresh when the WalletManager object changes
    var refreshTrigger by remember { mutableIntStateOf(0) }
    
    var currentApp by remember { mutableStateOf("Unknown") }
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Screen Time Wallet: ${WalletManager.minutes + refreshTrigger * 0}",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(onClick = {
            WalletManager.minutes += 30
            refreshTrigger++ // Increment to trigger UI update
        }) {
            Text("Complete Task (+30 min)")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            if (WalletManager.minutes > 0) {

                WalletManager.minutes = maxOf(0, WalletManager.minutes - 5)

                refreshTrigger++
            }
        }) {
            Text("Use App (-5 min)")
        }
        Spacer(modifier = Modifier.height(30.dp))

        Button(onClick = {
            currentApp = getForegroundApp(context) ?: "None"
        }) {
            Text("Detect Current App")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Current App: $currentApp")
    }
}
