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

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TaskManager.addTask("Study", 30)
        TaskManager.addTask("Workout", 20)
        setContent {
            TaskScreen()
        }
    }
    fun completeTask() {
        WalletManager.minutes += 10
    }
    @Composable
    fun TaskButton() {
        Button(onClick = {
            completeTask()
        }) {
            Text("Complete Task")
        }
    }
}

@Composable
fun TaskScreen() {

    var wallet by remember { mutableIntStateOf(0) }
    var currentApp by remember { mutableStateOf("Unknown") }
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Screen Time Wallet: $wallet minutes",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(onClick = {
            wallet += 30
        }) {
            Text("Complete Task (+30 min)")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            if (wallet > 0) {
                wallet -= 5
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


