package com.example.testapp

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
}

@Composable
fun TaskScreen() {

    // We use a trigger variable to force Compose to refresh when the WalletManager object changes
    var refreshTrigger by remember { mutableIntStateOf(0) }
    
    var currentApp by remember { mutableStateOf("Unknown") }
    var taskName by remember { mutableStateOf("") }
    var reward by remember { mutableStateOf("") }
    
    val context = LocalContext.current

    val analyzer = UsageAnalyzer()
    val usageList = analyzer.getDailyUsage(context)

    var predictedUsage = 0

    if (usageList.isNotEmpty()) {
        val firstApp = usageList[0]
        val minutes = (firstApp.totalTimeInForeground / 60000).toInt()
        predictedUsage = analyzer.predictMonthlyUsage(minutes)
    }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Screen Time Wallet: ${WalletManager.minutes + refreshTrigger * 0}",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Predicted monthly usage: $predictedUsage minutes",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(20.dp))

        TextField(
            value = taskName,
            onValueChange = { taskName = it },
            label = { Text("Task Name") }
        )

        TextField(
            value = reward,
            onValueChange = { reward = it },
            label = { Text("Reward Minutes") }
        )

        Button(onClick = {
            if (taskName.isNotEmpty() && reward.isNotEmpty()) {
                TaskManager.addTask(taskName, reward.toIntOrNull() ?: 0)
                taskName = ""
                reward = ""
                refreshTrigger++
            }
        }) {
            Text("Add Task")
        }

        Spacer(modifier = Modifier.height(40.dp))

        TaskManager.tasks.forEach { task ->
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = {
                WalletManager.minutes += task.reward
                refreshTrigger++
            }) {
                Text("${task.name} (+${task.reward} min)")
            }
        }

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
