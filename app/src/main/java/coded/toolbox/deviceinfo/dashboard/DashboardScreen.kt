package coded.toolbox.deviceinfo.dashboard

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.util.DisplayMetrics
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coded.toolbox.deviceinfo.AdBannerManager
import coded.toolbox.deviceinfo.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

// BatteryStats data class
data class BatteryStats(
    val level: Int, val status: Int, val voltage: Int, val temperature: Int
)

// BatteryStatusReceiver
class BatteryStatusReceiver(
    private val onBatteryStatusChanged: (BatteryStats) -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)

        val batteryStats = BatteryStats(
            level = level, status = status, voltage = voltage, temperature = temperature
        )

        onBatteryStatusChanged(batteryStats)
    }
}

@Composable
fun BatteryStatusBox() {
    // State to store battery info
    var batteryStats by remember { mutableStateOf<BatteryStats?>(null) }
    var isCharging by remember { mutableStateOf(false) }
    var batteryPercentage by remember { mutableFloatStateOf(0f) }

    // Save state across configuration changes
    var progress by rememberSaveable { mutableFloatStateOf(0f) }  // Progress bar state
    var currentIconIndex by rememberSaveable { mutableIntStateOf(0) }  // Icon index state

    // Register the BatteryStatusReceiver
    val context = LocalContext.current

    DisposableEffect(context) {
        val batteryReceiver = BatteryStatusReceiver { stats ->
            batteryStats = stats
            batteryPercentage = stats.level.toFloat()
            isCharging = stats.status == BatteryManager.BATTERY_STATUS_CHARGING

            // Set the progress and icon index when the battery status is received
            progress = batteryPercentage
            currentIconIndex = (batteryPercentage / 12.5f).toInt()
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)

        // Unregister the receiver when the composable is disposed
        onDispose {
            context.unregisterReceiver(batteryReceiver)
        }
    }

    // Update progress and icon while charging
    LaunchedEffect(isCharging) {
        if (isCharging) {
            // Loop progress bar from current battery percentage to 100 every 2 seconds
            launch {
                var currentProgress = batteryPercentage
                while (isCharging) {
                    currentProgress = (currentProgress + 10) % 100  // Increase progress and loop
                    progress = currentProgress // Update progress bar
                    delay(1000)  // Wait for 2 seconds before updating
                }
            }

            // Loop icon from battery0 to battery7 every second
            launch {
                var currentIcon =
                    (batteryPercentage / 12.5f).toInt()  // Starting icon based on battery percentage
                while (isCharging) {
                    currentIcon = (currentIcon + 1) % 8  // Loop through battery icons
                    currentIconIndex = currentIcon
                    delay(1000)  // Wait for 1 second before updating the icon
                }
            }
        } else {
            // Reset progress and icon when charging stops
            progress = batteryPercentage // Reset progress to current battery percentage
            currentIconIndex =
                (batteryPercentage / 12.5f).toInt()  // Reset icon based on battery percentage
        }
    }

    // Set the correct battery icon based on the current index
    val batteryIcon = when (currentIconIndex) {
        0 -> R.drawable.battery0
        1 -> R.drawable.battery1
        2 -> R.drawable.battery2
        3 -> R.drawable.battery3
        4 -> R.drawable.battery4
        5 -> R.drawable.battery5
        6 -> R.drawable.battery6
        else -> R.drawable.battery7
    }

    // Only display when battery stats are available
    batteryStats?.let {
        val chargingStatus = if (isCharging) "(Charging)" else ""
        val voltage = it.voltage / 1000f // Convert voltage to volts
        val temperature = it.temperature / 10f // Convert temperature to Celsius

        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 13.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { }
            .padding(16.dp, 10.dp, 16.dp, 10.dp)) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Battery Status Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Display battery icon
                    Icon(
                        painter = painterResource(id = batteryIcon),
                        contentDescription = null,
                        modifier = Modifier.size(34.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Battery $chargingStatus",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.secondary
                        )

                        // Battery progress bar showing the percentage
                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp)),
                            color = MaterialTheme.colorScheme.onPrimary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                        )

                        // Battery Info - Voltage and Temperature
                        Text(
                            text = "Voltage: ${
                                String.format(
                                    "%.2f", voltage
                                )
                            } V, Temp: ${String.format("%.1f", temperature)} °C",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    // Display battery percentage text
                    Text(
                        text = "${batteryPercentage.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}


@Composable
fun DashboardScreen(navController: NavController, adBannerManager: AdBannerManager) {
    val context = LocalContext.current
    val viewModel: DashboardViewModel = viewModel()
    LaunchedEffect(Unit) {
        viewModel.startRamUsageMonitoring(context)
        viewModel.startCpuUsageMonitoring() // Start CPU usage monitoring
    }
    val cpuCores = viewModel.cpuCores
    val cpuFrequencies = viewModel.cpuFrequencies

    //////new code added
    // State to hold the RAM usage percentage and values for the graph
    var ramUsagePercentage by remember { mutableFloatStateOf(Random.nextFloat() * 100) }
    val ramUsageValues = remember { mutableStateListOf<Float>() }
    var useRealValues by remember { mutableStateOf(false) }

    // State to hold real-time RAM info (using Int for values)
    var totalRam by remember { mutableIntStateOf(0) } // Total RAM as Int
    var usedRam by remember { mutableIntStateOf(0) } // Used RAM as Int
    var unusedRam by remember { mutableIntStateOf(0) } // Unused RAM as Int

    LaunchedEffect(Unit) {
        totalRam = getTotalRam(context).toInt()
        val randomValues = List(10) { Random.nextFloat() * 40f + 30f }
        ramUsageValues.addAll(randomValues)
        repeat(10) {
            ramUsagePercentage = Random.nextFloat() * 100
            delay(100)
        }
        useRealValues = true
        observeRamUsage(context).collect { updatedPercentage ->
            ramUsagePercentage = updatedPercentage
            usedRam = getUsedRam(context).toInt()
            unusedRam = totalRam - usedRam
            if (useRealValues) {
                val usedRamMB = usedRam
                val lastTwoDigits = usedRamMB % 100
                if (ramUsageValues.size >= 10) {
                    ramUsageValues.removeAt(0)
                }
                ramUsageValues.add(lastTwoDigits.toFloat())
            }
        }
    }

    Scaffold { paddingValues ->
        val padding = paddingValues
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // RAM usage card
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 13.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.onPrimary,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(10.dp)
                            .fillMaxWidth() // Ensure the box takes up full width
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            // Top Row with RAM Total and Used information
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween // Distribute space between text
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = "RAM - $totalRam",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.01.sp
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = " MB Total",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = "$usedRam",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.01.sp
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = " MB Used",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            // Row for CircularProgress and Graph
                            Row {
                                CircularProgressWithDoubleGap(
                                    progressPercentage = ramUsagePercentage,
                                    modifier = Modifier.padding(10.dp)
                                )
                                Column {
                                    RamGraph(ramUsageValues = ramUsageValues)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Text(
                                            text = "$unusedRam",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                letterSpacing = 0.01.sp
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Text(
                                            text = " MB Free",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // CPU information grid
            val screenWidth = LocalConfiguration.current.screenWidthDp.dp
            val columns = if (screenWidth < 600.dp) 4 else 6
            cpuCores.chunked(columns).forEach { chunkedCores ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    chunkedCores.forEachIndexed { _, core ->
                        val coreIndex = cpuCores.indexOf(core)
                        val frequency = cpuFrequencies.getOrNull(coreIndex) ?: 0f
                        Box(modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 5.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { }
                            .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center) {
                            Column(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.SpaceEvenly,
                                horizontalAlignment = Alignment.CenterHorizontally // Center text horizontally
                            ) {
                                Text(
                                    text = "CPU${coreIndex + 1}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    text = "${frequency.toInt()} MHz",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .padding(horizontal = 13.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp), // Space between items
                verticalAlignment = Alignment.CenterVertically // Align items vertically
            ) {
                Row(modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { }
                    .background(MaterialTheme.colorScheme.primary),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround) {
                    // CircularProgressBar with centered text
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(60.dp) // Adjust size as needed
                    ) {
                        // CircularProgressBar
                        CircularProgressIndicator(
                            progress = {
                                0f // Set to a non-zero value for testing
                            },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.onPrimary, // Progress color
                            strokeWidth = 4.dp,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), // Unprogressed color
                        )
                        // Text in the center of CircularProgressIndicator
                        Text(
                            text = "0", // Replace with actual value
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ), color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    // Text for "Tests 0/15 Completed"
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Tests", style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ), color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "0/15", // Replace with actual progress data
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ), color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "Completed", style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ), color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Box(modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { }
                    .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Icon from drawable resource
                        Icon(
                            painter = painterResource(id = R.drawable.cellphone), // Replace with your drawable resource ID
                            contentDescription = "Display Icon",
                            tint = MaterialTheme.colorScheme.onPrimary, // Optional tint color
                            modifier = Modifier
                                .size(60.dp)
                                .padding(vertical = 6.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp)) // Add spacing between icon and text
                        // Column with two texts
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Display", style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                ), color = MaterialTheme.colorScheme.onPrimary
                            )
                            val configuration = LocalConfiguration.current
                            val screenWidthPx =
                                configuration.screenWidthDp * configuration.densityDpi / 160
                            val screenHeightPx =
                                configuration.screenHeightDp * configuration.densityDpi / 160
                            Text(
                                text = "$screenWidthPx x $screenHeightPx",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            // Get device screen size in inches and refresh rate
                            val deviceInfo = remember {
                                val metrics = DisplayMetrics()
                                context.display.getRealMetrics(metrics)
                                val widthPx = metrics.widthPixels
                                val heightPx = metrics.heightPixels
                                val densityDpi = metrics.densityDpi
                                // Calculate screen size in inches
                                val widthInches = widthPx.toDouble() / densityDpi
                                val heightInches = heightPx.toDouble() / densityDpi
                                val screenSize = sqrt(widthInches.pow(2) + heightInches.pow(2))
                                // Get refresh rate
                                val refreshRate = context.display.refreshRate
                                Pair(screenSize, refreshRate)
                            }
                            val (screenSize, refreshRate) = deviceInfo
                            Text(
                                text = "${"%.1f".format(screenSize)}\" | ${refreshRate.toInt()} Hz", // Real device values
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

            }
            SystemStorageBox()
            InternalStorageBox()
            BatteryStatusBox()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 13.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoBox(
                    iconPainter = painterResource(id = R.drawable.sensor),
                    title = "55",
                    subtitle = "Sensors",
                    modifier = Modifier.weight(0.5f)
                )
                InfoBox(
                    iconPainter = painterResource(id = R.drawable.android),
                    title = getNumberOfInstalledApps(context).toString(),
                    subtitle = "All Apps",
                    modifier = Modifier.weight(0.5f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

fun getNumberOfInstalledApps(context: Context): Int {
    val packageManager = context.packageManager
    val installedPackages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
    return installedPackages.size
}

@Composable
fun SystemStorageBox() {
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 13.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(MaterialTheme.colorScheme.surface)
        .clickable { }
        .padding(16.dp, 10.dp, 16.dp, 10.dp)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.memory),
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Column(
                    modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "System Storage",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    val systemStorageStats = getSystemStorageStats()
                    val usedStoragePercentage = systemStorageStats.usedPercentage

                    LinearProgressIndicator(
                        progress = { usedStoragePercentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        color = MaterialTheme.colorScheme.onPrimary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    )
                    Text(
                        text = "Free: ${systemStorageStats.freeSpace} GB, Total: ${systemStorageStats.totalSpace} GB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                val storageStats = getSystemStorageStats()
                val usedStoragePercentage = storageStats.usedPercentage
                Text(
                    text = "${usedStoragePercentage}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

fun getSystemStorageStats(): SystemStorageStats {
    val path = File("/system")
    val stat = StatFs(path.absolutePath)
    val blockSize = stat.blockSizeLong
    val totalBlocks = stat.blockCountLong
    val availableBlocks = stat.availableBlocksLong
    val usedBlocks = totalBlocks - availableBlocks

    // Convert bytes to megabytes for easier reading (1024 * 1024 for MB)
    val totalSpaceInMB = totalBlocks * blockSize / (1024 * 1024)
    val usedSpaceInMB = usedBlocks * blockSize / (1024 * 1024)
    val freeSpaceInMB = availableBlocks * blockSize / (1024 * 1024)

    // Return the actual system storage stats
    return SystemStorageStats(
        totalSpaceInMB.toFloat(), usedSpaceInMB.toFloat(), freeSpaceInMB.toFloat()
    )
}

data class SystemStorageStats(val totalSpace: Float, val usedSpace: Float, val freeSpace: Float) {
    val usedPercentage: Int
        get() = ((usedSpace / totalSpace) * 100).toInt()
}

@Composable
fun InternalStorageBox() {
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 13.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(MaterialTheme.colorScheme.surface)
        .clickable { }
        .padding(16.dp, 10.dp, 16.dp, 10.dp)

    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.card),
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Column(
                    modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Internal Storage",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    val storageStats = getStorageStats()
                    val usedStoragePercentage = storageStats.usedPercentage

                    LinearProgressIndicator(
                        progress = { usedStoragePercentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        color = MaterialTheme.colorScheme.onPrimary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    )
                    Text(
                        text = "Free: ${storageStats.freeSpace} GB, Total: ${storageStats.totalSpace} GB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                val storageStats = getStorageStats()
                val usedStoragePercentage = storageStats.usedPercentage
                Text(
                    text = "${usedStoragePercentage}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

fun getStorageStats(): StorageStats {
    val statFs = StatFs(Environment.getDataDirectory().absolutePath)
    val totalBytes = statFs.blockSizeLong * statFs.blockCountLong
    val availableBytes = statFs.blockSizeLong * statFs.availableBlocksLong
    val usedBytes = totalBytes - availableBytes

    // Convert bytes to GB
    val totalSpace = totalBytes / (1024f * 1024f * 1024f)
    val freeSpace = availableBytes / (1024f * 1024f * 1024f)
    val usedSpacePercentage = (usedBytes.toFloat() / totalBytes.toFloat()) * 100

    return StorageStats(
        totalSpace = "%.2f".format(totalSpace),
        freeSpace = "%.2f".format(freeSpace),
        usedPercentage = usedSpacePercentage.toInt()
    )
}

// Data class to hold storage stats
data class StorageStats(
    val totalSpace: String, val freeSpace: String, val usedPercentage: Int
)

@Composable
fun InfoBox(
    iconPainter: Painter, title: String, subtitle: String, modifier: Modifier = Modifier
) {
    Box(modifier = modifier
        .clip(RoundedCornerShape(8.dp))
        .background(MaterialTheme.colorScheme.primary)
        .clickable { }
        .padding(10.dp),
        contentAlignment = Alignment.Center) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            // Icon
            Image(
                painter = iconPainter,
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                modifier = Modifier.size(60.dp)  // Adjust size for the icon
            )

            // Column for Title and Subtitle
            Column(
                verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.Start
            ) {
                // Title Text (Very Large)
                Text(
                    text = title, style = TextStyle(
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.ExtraBold,
                    ).merge(MaterialTheme.typography.headlineSmall)
                )

                // Subtitle Text (Medium)
                Text(
                    text = subtitle, style = TextStyle(
                        color = MaterialTheme.colorScheme.onPrimary,
                    ).merge(MaterialTheme.typography.bodyMedium)
                )
            }
        }
    }
}


////////////////////////new code added
@Composable
fun RamGraph(ramUsageValues: List<Float>) {
    val lineProgress = remember { Animatable(0f) }
    val strokecolour = MaterialTheme.colorScheme.primary
    val onprimary = MaterialTheme.colorScheme.onPrimary

    // Animate the graph when new values are added
    LaunchedEffect(ramUsageValues) {
        lineProgress.animateTo(
            targetValue = 1f, animationSpec = tween(
                durationMillis = 1000, easing = LinearEasing
            )
        )
    }

    // Draw the graph with smoothness and animation
    if (ramUsageValues.isNotEmpty()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
        ) {
            val width = size.width
            val height = size.height
            val numberOfPoints = ramUsageValues.size
            val path = Path().apply {
                // Start drawing the path from the first point
                moveTo(0f, height - (ramUsageValues[0] / 100f * height))

                // Use a smoother control point calculation for Bézier curves
                for (i in 1 until (lineProgress.value * numberOfPoints).toInt()) {
                    val x = i * (width / (numberOfPoints - 1))
                    val y = height - (ramUsageValues[i] / 100f * height)

                    val prevX = (i - 1) * (width / (numberOfPoints - 1))
                    val prevY = height - (ramUsageValues[i - 1] / 100f * height)

                    // Adjust control points for smooth cubic Bézier curves
                    val controlX1 = prevX + (x - prevX) / 3
                    val controlX2 = x - (x - prevX) / 3

                    cubicTo(
                        controlX1, prevY, controlX2, y, x, y
                    ) // Smooth cubic Bézier curve
                }
            }

            // Draw the shadow path
            val shadowPath = Path().apply {
                moveTo(0f, height)
                for (i in 0 until (lineProgress.value * numberOfPoints).toInt()) {
                    val x = i * (width / (numberOfPoints - 1))
                    val y = height - (ramUsageValues[i] / 100f * height)
                    lineTo(x, y)
                }
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            drawPath(
                path = shadowPath, brush = Brush.verticalGradient(
                    colors = listOf(strokecolour.copy(alpha = 0.7f), onprimary.copy(alpha = 0.1f))
                )
            )

            drawPath(
                path = path, color = strokecolour, style = Stroke(width = 4f)
            )
        }
    }
}

// Helper function to get total RAM
private fun getTotalRam(context: Context): Float {
    val memoryInfo = ActivityManager.MemoryInfo()
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    activityManager.getMemoryInfo(memoryInfo)
    return memoryInfo.totalMem / (1024 * 1024).toFloat() // Convert to MB
}

// Helper function to get used RAM
private fun getUsedRam(context: Context): Float {
    val memoryInfo = ActivityManager.MemoryInfo()
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    activityManager.getMemoryInfo(memoryInfo)
    val availableRam = memoryInfo.availMem / (1024 * 1024).toFloat() // Convert to MB
    val totalRam = memoryInfo.totalMem / (1024 * 1024).toFloat() // Convert to MB
    return totalRam - availableRam // Used RAM in MB
}

@Composable
fun CircularProgressWithDoubleGap(progressPercentage: Float, modifier: Modifier) {
    val onPrimary = MaterialTheme.colorScheme.primary
    val backgroundTrackColor = MaterialTheme.colorScheme.outlineVariant

    // Animatable progress for smooth transitions
    val animatedProgress = remember { Animatable(0f) }

    // Animate the progressPercentage value smoothly on recomposition
    LaunchedEffect(progressPercentage) {
        animatedProgress.animateTo(
            targetValue = progressPercentage, animationSpec = tween(
                durationMillis = 500, // Duration for the smooth transition
                easing = LinearEasing // Easing function for smoothness
            )
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(90.dp) // Size of the circular progress
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 6.dp.toPx() // Stroke width
            val gap = 1.5.dp.toPx() // Gap between arcs
            val radius = (size.minDimension / 2) - (strokeWidth / 2) // Radius of the circle
            val sweepAngle = 360 * (animatedProgress.value / 100) // Angle to draw the progress
            val gapAngle = gap / radius * 180 // Gap angle to create the gap effect

            // Draw Track with gap at the end
            drawArc(
                color = backgroundTrackColor,
                startAngle = -230f + sweepAngle + gapAngle,
                sweepAngle = 360f - sweepAngle - gapAngle * 2,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Draw Progress with gap at the start and end
            drawArc(
                color = onPrimary,
                startAngle = -230f + gapAngle,
                sweepAngle = sweepAngle - gapAngle * 2,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Display the percentage in the center (smoothly animating text)
        Row {
            Text(
                text = "${animatedProgress.value.toInt()}",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = onPrimary
                )
            )
            Text(
                text = "%", style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold, color = onPrimary
                )
            )
        }
    }
}


fun observeRamUsage(context: Context): Flow<Float> = flow {
    while (true) {
        val totalRam = getTotalRam(context)
        val usedRam = getUsedRam(context)
        val ramUsagePercentage = ((usedRam / totalRam) * 100)

        emit(ramUsagePercentage) // Emit the updated RAM usage percentage
        delay(1000) // Update every second
    }
}
