package coded.toolbox.deviceinfo.dashboard

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coded.toolbox.deviceinfo.R
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun DashboardScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    // Retrieve real device information
    val brand = Build.BRAND // The brand of the phone (e.g., Motorola)
    val model = Build.MODEL // The specific model of the phone (e.g., Moto G45 5G)
    val androidVersion = Build.VERSION.RELEASE // Android version (e.g., Android 14)
    val androidCodename =
        Build.VERSION.CODENAME // Codename for Android version (e.g., Upside Down Cake)

    // Logic to handle pre-release or development codename
    val androidVersionDisplay = if (androidCodename != "REL") {
        "$androidVersion ($androidCodename)"
    } else {
        "$androidVersion (Final Release)"
    }

    // State for live CPU core frequencies
    val coreFrequencies = remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    // Fetch core frequencies live
    LaunchedEffect(Unit) {
        while (true) {
            coreFrequencies.value = fetchCpuFrequencies()
            delay(1000) // Update every second
        }
    }

    // State for internal storage info
    val internalStorageInfo = remember { mutableStateOf(Pair(0L, 0L)) }

    // Fetch internal storage info
    LaunchedEffect(Unit) {
        val stats = getInternalStorageStats()
        internalStorageInfo.value = stats
    }


    // Calculate percentUsed dynamically
    val (used, total) = internalStorageInfo.value
    val percentUsed = if (total > 0) (used * 100 / total).toInt() else 0

    Scaffold(
        content = { paddingValues ->
            val padding = paddingValues
            Column(
                modifier = Modifier
                    .fillMaxSize() // Ensures the Column takes up the available screen space
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),  // Allows the content inside the Column to scroll
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Row with Two Cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // First Card with Motorola and Moto G45 info
                    OutlinedCard(
                        modifier = Modifier.weight(0.5f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = brand,
                                style = TextStyle(
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 16.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = model,
                                style = TextStyle(
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 16.sp
                                )
                            )
                        }
                    }

                    // Second Card with Android version info
                    OutlinedCard(
                        modifier = Modifier.weight(0.5f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = androidVersionDisplay,
                                style = TextStyle(
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 16.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Codename: $androidCodename",
                                style = TextStyle(
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                }

                // OutlinedCard with RAM info
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary),
                    colors = androidx.compose.material3.CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Circular RAM Usage Card
                        Box(
                            modifier = Modifier
                                .weight(0.4f)  // Reduced space here
                                .fillMaxHeight()
                        ) {
                            CircularRamUsageCard()
                        }

                        // Right: Live RAM Graph
                        Box(
                            modifier = Modifier
                                .weight(0.6f)  // Increased space here
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LiveRamGraph()
                        }
                    }
                }

                // CPU Frequencies in Cards
                coreFrequencies.value.chunked(4).forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { (core, frequency) ->
                            OutlinedCard(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary),
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(core, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(frequency, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }

                        // Fill empty space in the row if less than 4 cores
                        repeat(4 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }


                // Internal Storage Card
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.calendar),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Internal Storage", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Progress indicator
                            LinearProgressIndicator(
                                progress = { percentUsed / 100f },
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.onPrimary

                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Storage details
                            Text(
                                text = "Used: %.2f GB, Total: %.2f GB".format(
                                    used / (1024.0 * 1024 * 1024),
                                    total / (1024.0 * 1024 * 1024)
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))

                        // Display percentUsed inside the same Row
                        Text(
                            text = "$percentUsed%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }





            }
        }
    )
}



fun getInternalStorageStats(): Pair<Long, Long> {
    val stat = StatFs(Environment.getDataDirectory().path)
    val total = stat.blockSizeLong * stat.blockCountLong
    val available = stat.blockSizeLong * stat.availableBlocksLong
    val used = total - available
    return Pair(used, total)
}

fun fetchCpuFrequencies(): List<Pair<String, String>> {
    val frequencies = mutableListOf<Pair<String, String>>()
    val cpuDir = File("/sys/devices/system/cpu/")
    cpuDir.listFiles { _, name -> name.startsWith("cpu") && name.matches(Regex("cpu[0-9]+")) }
        ?.forEach { cpu ->
            val freqFile = File(cpu, "cpufreq/scaling_cur_freq")
            val freq = if (freqFile.exists()) {
                freqFile.readText().trim().toLongOrNull()?.let { "${it / 1000} MHz" }
                    ?: "Unavailable"
            } else {
                "Unavailable"
            }
            frequencies.add(cpu.name to freq)
        }
    return frequencies.reversed() // Reverse the order of the list
}

@Composable
fun LiveRamGraph() {
    val context = LocalContext.current
    val ramUsageValues = remember { mutableStateListOf<Float>() }
    var totalRam by remember { mutableFloatStateOf(1f) }
    var usedRam by remember { mutableFloatStateOf(0f) }
    var freeRam by remember { mutableFloatStateOf(0f) }
    val maxEntries = 20

    LaunchedEffect(Unit) {
        while (true) {
            val (used, total) = getRamUsageInMb(context)
            usedRam = used
            totalRam = total
            freeRam = total - used
            ramUsageValues.add(used)
            if (ramUsageValues.size > maxEntries) ramUsageValues.removeAt(0)
            delay(1000)
        }
    }

    val usedPercentage = (usedRam / totalRam) * 100

    Box(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Used RAM: ${usedRam.toInt()} MB",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Graph(ramUsageValues, totalRam, MaterialTheme.colorScheme.onPrimary)
            Text(
                text = "Free RAM: ${freeRam.toInt()} MB",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun CircularRamUsageCard() {
    val context = LocalContext.current
    var totalRam by remember { mutableFloatStateOf(1f) }
    var usedRam by remember { mutableFloatStateOf(0f) }
    val usedPercentage = (usedRam / totalRam) * 100

    LaunchedEffect(Unit) {
        while (true) {
            val (used, total) = getRamUsageInMb(context)
            usedRam = used
            totalRam = total
            delay(1000)
        }
    }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Total RAM: ${totalRam.toInt()} MB",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            CircularRAMIndicator(
                percentage = usedPercentage,
                totalRam = totalRam.toLong(),
                usedRam = usedRam.toLong(),
                primaryColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun Graph(ramUsageValues: List<Float>, totalRam: Float, colour: Color) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(0.dp, 20.dp, 30.dp, 20.dp)
    ) {
        val width = size.width
        val height = size.height
        val verticalLineX = width - 50f

        val dynamicMin = ramUsageValues.minOrNull() ?: 0f
        val dynamicMax = ramUsageValues.maxOrNull() ?: totalRam
        val range = dynamicMax - dynamicMin
        val intervals = 5

        for (i in 0..intervals) {
            val y = height - (i * height / intervals)
            val labelValue = dynamicMin + (i * range / intervals)
            val label = "${labelValue.toInt()} MB"

            drawContext.canvas.nativeCanvas.drawText(
                label,
                verticalLineX + 20f,
                y,
                android.graphics.Paint().apply {
                    textSize = 20f
                    color = color
                    isAntiAlias = true
                }
            )

            drawLine(
                color = Color.Gray,
                start = Offset(x = 0f, y = y),
                end = Offset(x = verticalLineX, y = y),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        drawLine(
            color = Color.Black,
            start = Offset(x = verticalLineX, y = 0f),
            end = Offset(x = verticalLineX, y = height),
            strokeWidth = 4f
        )

        if (ramUsageValues.isNotEmpty()) {
            val path = androidx.compose.ui.graphics.Path()

            for (i in 0 until ramUsageValues.size - 1) {
                val startX = i * verticalLineX / (ramUsageValues.size - 1)
                val startY = (ramUsageValues[i] - dynamicMin) / range * height

                val endX = (i + 1) * verticalLineX / (ramUsageValues.size - 1)
                val endY = (ramUsageValues[i + 1] - dynamicMin) / range * height

                val controlX1 = (startX + endX) / 2
                val controlX2 = (startX + endX) / 2

                if (i == 0) {
                    path.moveTo(startX, height - startY)
                }
                path.cubicTo(
                    controlX1,
                    height - startY,
                    controlX2,
                    height - endY,
                    endX,
                    height - endY
                )
            }

            drawPath(
                path = path,
                color = colour,
                style = Stroke(width = 3f)
            )
        }
    }
}

fun getRamUsageInMb(context: Context): Pair<Float, Float> {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    val totalMemory = memoryInfo.totalMem.toFloat() / (1024 * 1024)
    val usedMemory = totalMemory - memoryInfo.availMem.toFloat() / (1024 * 1024)
    return usedMemory to totalMemory
}

@Composable
fun CircularRAMIndicator(
    percentage: Float,
    totalRam: Long,
    usedRam: Long,
    primaryColor: Color
) {
    // Animate the percentage to make it grow smoothly
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 1000),
        label = "" // You can adjust the duration as per preference
    )

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(100.dp)) {
            // Draw the background circle
            drawCircle(
                color = Color.LightGray,
                radius = size.minDimension / 2,
                style = Stroke(width = 16.dp.toPx())
            )

            // Draw the animated arc representing used RAM
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = (360 * animatedPercentage / 100),
                useCenter = false,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Display the percentage in the center of the circle
        Text(
            text = "${animatedPercentage.toInt()}%",
            color = primaryColor, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge.copy(color = primaryColor),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}