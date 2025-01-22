package coded.toolbox.deviceinfo.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coded.toolbox.deviceinfo.AdBannerManager
import coded.toolbox.deviceinfo.R
import coded.toolbox.deviceinfo.markaziTextFont
import coded.toolbox.deviceinfo.ui.theme.themecolour
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun BatteryScreen(
    navController: NavController,
    context: Context,
    colorsurface: Color,
    viewModel: BatteryViewModel,
    adBannerManager: AdBannerManager
) {
    val batteryLevel by viewModel.batteryLevel.observeAsState(0)
    val batteryHealth by viewModel.batteryHealth.observeAsState("Unknown")
    val batteryStatus by viewModel.batteryStatus.observeAsState("Unknown")
    val powerSource by viewModel.powerSource.observeAsState("Unknown")
    val batteryTechnology by viewModel.batteryTechnology.observeAsState("Unknown")
    val batteryCurrent by viewModel.batteryCurrent.observeAsState(0)
    val batteryPower by viewModel.batteryPower.observeAsState(0f)
    val batteryVoltage by viewModel.batteryVoltage.observeAsState(0)
    val currentIconIndex by viewModel.currentIconIndex.observeAsState(0)
    val batteryTemperature by viewModel.batteryTemperature.observeAsState(0)

    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogDescription by remember { mutableStateOf("") }

    // Battery icons
    val batteryIcons = listOf(
        R.drawable.battery0, R.drawable.battery1, R.drawable.battery2, R.drawable.battery3,
        R.drawable.battery4, R.drawable.battery5, R.drawable.battery6, R.drawable.battery7
    )

    // Battery receiver registration
    DisposableEffect(Unit) {
        val batteryReceiver = viewModel.getBatteryStatusReceiver()
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)
        onDispose {
            context.unregisterReceiver(batteryReceiver)
        }
    }

    var batteryCapacity by remember { mutableIntStateOf(0) }

    batteryCapacity = getBatteryCapacity(context).toInt()

    val scrollState = rememberScrollState()

    val timeVsCurrent = remember { mutableStateListOf<Pair<Long, Int>>() }

    LaunchedEffect(Unit) {
        while (true) {
            // Add new data point with timestamp and current value
            val currentTime = System.currentTimeMillis() / 1000 // Seconds since epoch
            if (timeVsCurrent.size >= 10) {
                timeVsCurrent.removeAt(0) // Keep only the latest 10 seconds of data
            }
            timeVsCurrent.add(currentTime to batteryCurrent)
            delay(1000L) // Update every 1 second
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 5.dp)
            .verticalScroll(scrollState)
    ) {
        // Battery icon and graph section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
               // .padding(top=15.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)

        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Battery Icon with Current and Power values
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .fillMaxHeight()
                ) {
                    val batteryIcon = batteryIcons[currentIconIndex]
                    Image(
                        painter = painterResource(id = batteryIcon),
                        contentDescription = "Battery Icon",
                        modifier = Modifier.fillMaxSize(),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
                    )
                    // Current
                    Text(
                        text = "I: $batteryCurrent mA",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = markaziTextFont,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    )
                    // Power
                    Text(
                        text = "P: ${"%.2f".format(batteryPower)} W",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = markaziTextFont,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(8.dp)
                    )
                }
                val outlineColorInt = MaterialTheme.colorScheme.outline.toArgb() // Get ARGB as Int

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val padding = 16.dp.toPx()
                    val axisExtension = 10f // Extend X and Y axes by 10px
                    val graphHeight = size.height - 2 * padding
                    val graphWidth = size.width - 2 * padding

                    // Ensure max and min values are not the same to avoid flat graph issues
                    val maxCurrent =
                        (timeVsCurrent.maxOfOrNull { it.second }?.toFloat() ?: 1f).coerceAtLeast(1f)
                    val minCurrent =
                        (timeVsCurrent.minOfOrNull { it.second }?.toFloat() ?: 0f).coerceAtMost(
                            maxCurrent - 1f
                        )

                    val scaleY = graphHeight / (maxCurrent - minCurrent)
                    val scaleX = graphWidth / 10f // 10 seconds of data

                    // Define dotted line effect
                    val dottedLineEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                    // Draw background grid
                    val gridLines = 5
                    for (i in 0..gridLines) {
                        val y = padding + i * (graphHeight / gridLines)
                        drawLine(
                            color = Color(outlineColorInt),
                            start = Offset(padding, y),
                            end = Offset(size.width - padding, y),
                            strokeWidth = 1f,
                            pathEffect = dottedLineEffect // Apply dotted effect
                        )
                    }
                    for (i in 0..10) {
                        val x = padding + i * scaleX
                        drawLine(
                            color = Color(outlineColorInt),
                            start = Offset(x, padding),
                            end = Offset(x, size.height - padding),
                            strokeWidth = 1f,
                            pathEffect = dottedLineEffect // Apply dotted effect
                        )
                    }

                    // Draw smooth graph line
                    if (timeVsCurrent.size > 1) {
                        val path = Path().apply {
                            for (i in timeVsCurrent.indices) {
                                val x = padding + i * scaleX
                                val y =
                                    padding + (maxCurrent - timeVsCurrent[i].second.toFloat()) * scaleY

                                if (i == 0) {
                                    moveTo(x, y)
                                } else {
                                    // Smooth curve using cubicTo
                                    val prevX = padding + (i - 1) * scaleX
                                    val prevY =
                                        padding + (maxCurrent - timeVsCurrent[i - 1].second.toFloat()) * scaleY

                                    val controlX1 = (prevX + x) / 2
                                    val controlY1 = prevY
                                    val controlX2 = (prevX + x) / 2
                                    val controlY2 = y

                                    cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                                }
                            }
                        }
                        drawPath(
                            path = path,
                            color = themecolour,
                            style = Stroke(width = 3f)
                        )
                    }

                    // Y-axis (Current) labels
                    for (i in 0..5) {
                        val yValue = minCurrent + (maxCurrent - minCurrent) * (i / 5f)
                        val yPos = padding + (maxCurrent - yValue) * scaleY
                        drawContext.canvas.nativeCanvas.drawText(
                            "${yValue.toInt()}",
                            padding - 38f, // Restore to the original left-side position
                            yPos,
                            android.graphics.Paint().apply {
                                color = colorsurface.toArgb()
                                textSize = 14f
                            }
                        )
                    }


                    // X-axis (Time in seconds) labels (alternate seconds)
                    for (i in 0..10 step 2) { // Skip every other second
                        val xPos = padding + i * scaleX
                        drawContext.canvas.nativeCanvas.drawText(
                            "${i}s",
                            xPos,
                            size.height - padding + 16f,
                            android.graphics.Paint().apply {
                                color = colorsurface.toArgb()
                                textSize = 14f
                            }
                        )
                    }

                    // X and Y axis lines
                    drawLine(
                        color = colorsurface,
                        start = Offset(padding, padding - 10f), // Extend Y-axis at the top
                        end = Offset(padding, size.height - padding), // Keep the bottom unchanged
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = Color.Gray,
                        start = Offset(padding, size.height - padding),
                        end = Offset(
                            size.width - padding + 10f,
                            size.height - padding
                        ), // Extend X-axis to the right
                        strokeWidth = 2f
                    )

                }

            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Battery details list
        CameraDetailRow("Health", batteryHealth, isFirstItem = true) {
            dialogTitle = "Health"
            dialogDescription =
                "Battery health indicates the overall condition of the battery, including its ability to hold charge and maintain performance over time."
            showDialog = true
        }
        CameraDetailRow("Battery Level", "$batteryLevel%") {
            dialogTitle = "Battery Level"
            dialogDescription =
                "Battery level shows the current percentage of charge remaining in the battery."
            showDialog = true
        }
        CameraDetailRow("Status", batteryStatus) {
            dialogTitle = "Battery Status"
            dialogDescription =
                "Battery status provides the current state of the battery, such as 'Charging', 'Discharging', or 'Full'."
            showDialog = true
        }
        CameraDetailRow("Temperature", "${"%.1f".format(batteryTemperature / 10.0)} °C") {
            dialogTitle = "Temperature"
            dialogDescription =
                "Battery temperature reflects the current operating temperature of the battery in degrees Celsius."
            showDialog = true
        }
        CameraDetailRow("Power Source", powerSource) {
            dialogTitle = "Power Source"
            dialogDescription =
                "Power source identifies how the device is being powered, such as through an AC charger, USB connection, or wireless charging."
            showDialog = true
        }
        CameraDetailRow("Technology", batteryTechnology) {
            dialogTitle = "Battery Technology"
            dialogDescription =
                "Battery technology describes the type of battery chemistry or technology used in the device, such as Lithium-ion or Lithium-polymer."
            showDialog = true
        }
        CameraDetailRow(
            "Current",
            "${if (batteryCurrent >= 0) "+$batteryCurrent" else "$batteryCurrent"} mA"
        ) {
            dialogTitle = "Current"
            dialogDescription =
                "Current indicates the flow of electrical charge through the battery circuit, measured in milliamperes (mA)."
            showDialog = true
        }
        CameraDetailRow("Power", "${"%.2f".format(batteryPower)} W") {
            dialogTitle = "Power"
            dialogDescription =
                "Power represents the rate of energy consumption or production by the battery, expressed in watts (W)."
            showDialog = true
        }
        CameraDetailRow("Voltage", "$batteryVoltage mV") {
            dialogTitle = "Voltage"
            dialogDescription =
                "Voltage refers to the electric potential supplied by the battery, measured in millivolts (mV)."
            showDialog = true
        }
        CameraDetailRow("Capacity", "$batteryCapacity mAh", isLastItem = true) {
            dialogTitle = "Capacity"
            dialogDescription =
                "Battery capacity denotes the total amount of charge the battery can store, measured in milliampere-hours (mAh)."
            showDialog = true
        }
    }

    // Information dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(dialogTitle) },
            text = { Text(dialogDescription) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}