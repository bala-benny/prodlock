package com.example.testapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize with some tasks if empty
        if (TaskManager.tasks.isEmpty()) {
            TaskManager.addTask("Study", 30)
            TaskManager.addTask("Workout", 20)
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TaskScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen() {
    // trigger variable to force Compose to refresh when the WalletManager object changes
    var refreshTrigger by remember { mutableIntStateOf(0) }
    
    // Create a copy of the list that updates when refreshTrigger changes
    val tasks = remember(refreshTrigger) { TaskManager.tasks.toList() }
    
    var taskName by remember { mutableStateOf("") }
    var reward by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val analyzer = remember { UsageAnalyzer() }

    // State for the timer
    var timerDisplay by remember { mutableStateOf("") }
    var isTimerActive by remember { mutableStateOf(WalletManager.isTimerRunning) }

    // Usage prediction logic
    val usageList = analyzer.getDailyUsage(context)
    val predictedUsage = if (usageList.isNotEmpty()) {
        val minutes = (usageList[0].totalTimeInForeground / 60000).toInt()
        analyzer.predictMonthlyUsage(minutes)
    } else 0
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prodlock Dashboard", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SECTION 1: WALLET STATS & TIMER ---
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (isTimerActive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isTimerActive) "SESSION ACTIVE" else "Screen Time Wallet",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isTimerActive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        Text(
                            text = if (isTimerActive) timerDisplay else "${WalletManager.minutes} min",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isTimerActive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            thickness = 1.dp,
                            color = (if (isTimerActive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer).copy(alpha = 0.2f)
                        )
                        
                        Text(
                            text = "Predicted monthly usage: $predictedUsage minutes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isTimerActive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // --- SECTION 2: SESSION CONTROL ---
            item {
                if (!isTimerActive) {
                    Button(
                        onClick = {
                            if (WalletManager.minutes > 0) {
                                WalletManager.startTimer(
                                    onTick = { seconds ->
                                        val m = seconds / 60
                                        val s = seconds % 60
                                        timerDisplay = String.format(Locale.getDefault(), "%02d:%02d", m, s)
                                        isTimerActive = true
                                        refreshTrigger++
                                    },
                                    onFinish = {
                                        isTimerActive = false
                                        refreshTrigger++
                                    }
                                )
                                isTimerActive = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = WalletManager.minutes > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Use all time (${WalletManager.minutes} min)")
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            WalletManager.stopTimer()
                            isTimerActive = false
                            refreshTrigger++
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Stop Session")
                    }
                }
            }

            // --- SECTION 3: ADD NEW TASK ---
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Add New Task",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        OutlinedTextField(
                            value = taskName,
                            onValueChange = { taskName = it },
                            label = { Text("Task Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = reward,
                            onValueChange = { reward = it },
                            label = { Text("Reward Minutes") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (taskName.isNotEmpty() && reward.isNotEmpty()) {
                                    TaskManager.addTask(taskName, reward.toIntOrNull() ?: 0)
                                    taskName = ""
                                    reward = ""
                                    refreshTrigger++
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add Task")
                        }
                    }
                }
            }

            // --- SECTION 4: TASK LIST ---
            item {
                Text(
                    text = "Your Tasks (Long press to remove)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }

            items(tasks, key = { it.id }) { task ->
                TaskItem(
                    task = task,
                    onComplete = { rewardAmount ->
                        WalletManager.minutes += rewardAmount
                        refreshTrigger++
                    },
                    onDelete = {
                        TaskManager.removeTask(task.id)
                        refreshTrigger++
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItem(task: Task, onComplete: (Int) -> Unit, onDelete: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onComplete(task.reward) },
                onLongClick = { onDelete() }
            )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "+${task.reward} minutes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "Complete",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
