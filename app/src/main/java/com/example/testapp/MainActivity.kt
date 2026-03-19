package com.example.testapp

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.testapp.ui.theme.TestAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize data storage
        WalletManager.load(this)
        TaskManager.load(this)

        if (TaskManager.tasks.isEmpty()) {
            TaskManager.addTask(this, "Study", 30)
            TaskManager.addTask(this, "Workout", 20)
        }

        setContent {
            TestAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppContent()
                }
            }
        }
    }
}

@Composable
fun MainAppContent() {
    val context = LocalContext.current
    var showSplash by remember { mutableStateOf(true) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { _ -> }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    if (showSplash) {
        SplashScreen(onTimeout = { showSplash = false })
    } else {
        TaskScreen()
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1.1f,
                animationSpec = tween(durationMillis = 3000, easing = FastOutSlowInEasing)
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1500)
            )
        }
        
        delay(3000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .scale(scale.value * 0.8f)
                .alpha(alpha.value * 0.2f)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )
        
        Box(
            modifier = Modifier
                .size(250.dp)
                .scale(scale.value * 1.2f)
                .alpha(alpha.value * 0.1f)
                .border(1.dp, MaterialTheme.colorScheme.secondary, CircleShape)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale.value).alpha(alpha.value)
        ) {
            Text(
                text = "HARDCODE Productions",
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LinearProgressIndicator(
                modifier = Modifier.width(120.dp).alpha(0.5f),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen() {
    var refreshTrigger by remember { mutableIntStateOf(0) }
    val tasks = remember(refreshTrigger) { TaskManager.tasks.toList() }
    
    var taskName by remember { mutableStateOf("") }
    var reward by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val analyzer = remember { UsageAnalyzer() }

    var timerDisplay by remember { mutableStateOf("") }
    var isTimerActive by remember { mutableStateOf(WalletManager.isTimerRunning) }

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
            item {
                if (!isAccessibilityServiceEnabled(context, com.example.testapp.blocking.BlockService::class.java)) {
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Enable Accessibility Service")
                    }
                }
            }

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
                            text = "Predicted monthly: $predictedUsage min",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isTimerActive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            item {
                UsageBarGraph(usageList)
            }

            item {
                if (!isTimerActive) {
                    Button(
                        onClick = {
                            if (WalletManager.minutes > 0) {
                                WalletManager.startTimer(
                                    context = context,
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
                        Text("Use all time")
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            WalletManager.stopTimer(context)
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
                                    TaskManager.addTask(context, taskName, reward.toIntOrNull() ?: 0)
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
                        WalletManager.addMinutes(context, rewardAmount)
                        refreshTrigger++
                    },
                    onDelete = {
                        TaskManager.removeTask(context, task.id)
                        refreshTrigger++
                    }
                )
            }
        }
    }
}

@Composable
fun UsageBarGraph(usageList: List<android.app.usage.UsageStats>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Top App Usage (Today)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            
            val displayList = usageList
                .filter { it.totalTimeInForeground > 0 }
                .sortedByDescending { it.totalTimeInForeground }
                .take(5)

            if (displayList.isEmpty()) {
                Text("No usage data available", style = MaterialTheme.typography.bodySmall)
            } else {
                val maxTime = displayList.maxOf { it.totalTimeInForeground }.toFloat()
                
                displayList.forEach { stats ->
                    val appName = stats.packageName.split(".").last()
                    val minutes = stats.totalTimeInForeground / 60000
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = appName,
                            modifier = Modifier.width(80.dp),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(16.dp)
                                .background(secondaryColor.copy(alpha = 0.2f), MaterialTheme.shapes.small)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (maxTime > 0) stats.totalTimeInForeground / maxTime else 0f)
                                    .fillMaxHeight()
                                    .background(primaryColor, MaterialTheme.shapes.small)
                            )
                        }
                        
                        Text(
                            text = "${minutes}m",
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
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

fun isAccessibilityServiceEnabled(context: Context, service: Class<out AccessibilityService>): Boolean {
    val expectedComponentName = android.content.ComponentName(context, service)
    val enabledServicesSetting = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServicesSetting)

    while (colonSplitter.hasNext()) {
        val componentNameString = colonSplitter.next()
        val enabledService = android.content.ComponentName.unflattenFromString(componentNameString)
        if (enabledService != null && enabledService == expectedComponentName) {
            return true
        }
    }
    return false
}
