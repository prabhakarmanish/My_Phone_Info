package coded.toolbox.deviceinfo.thermal

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException

@Composable
fun ThermalScreen(navController: NavController, context: Context) {
    val batteryTemperature = getBatteryTemperature(context)
    val cpuTemperature = getCpuTemperature()
    val gpuTemperature = getGpuTemperature()
    val cpuUsrTemperature = getCpuUsrTemperature()  // Added function to fetch cpu-1-0-usr
    val thermalZones = getThermalZoneTemperatures()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Thermal Information", modifier = Modifier.padding(bottom = 16.dp))

        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxWidth()) {
            item { ThermalItem("Battery Temperature", "$batteryTemperature °C") }
            item { ThermalItem("CPU Temperature", cpuTemperature) }
            item { ThermalItem("GPU Temperature", gpuTemperature) }
            item { ThermalItem("CPU-1-0-usr Temperature", cpuUsrTemperature) }  // Display cpu-1-0-usr temp
            thermalZones.forEach { zone ->
                item { ThermalItem("Thermal Zone", zone) }
            }
        }
    }
}

@Composable
fun ThermalItem(title: String, value: String) {
    Card(modifier = Modifier.padding(8.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = title)
            Text(text = value)
        }
    }
}

fun getBatteryTemperature(context: Context): Double {
    val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val temperature = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
    return temperature / 10.0 // Convert from deci-Celsius to Celsius
}

fun getCpuTemperature(): String {
    try {
        val reader = BufferedReader(FileReader("/sys/class/thermal/thermal_zone0/temp"))
        val tempString = reader.readLine()
        reader.close()
        val temperature = tempString.toFloat() / 1000.0 // Convert to Celsius
        return "$temperature °C"
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return "N/A"
}

fun getGpuTemperature(): String {
    try {
        val reader = BufferedReader(FileReader("/sys/class/thermal/thermal_zone1/temp"))
        val tempString = reader.readLine()
        reader.close()
        val temperature = tempString.toFloat() / 1000.0 // Convert to Celsius
        return "$temperature °C"
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return "N/A"
}

// Function to get temperature from cpu-1-0-usr or similar thermal zone
fun getCpuUsrTemperature(): String {
    try {
        // Adjust the path if necessary for your device
        val reader = BufferedReader(FileReader("/sys/class/thermal/thermal_zone1/temp")) // Could be another thermal zone path for cpu-1-0-usr
        val tempString = reader.readLine()
        reader.close()
        val temperature = tempString.toFloat() / 1000.0 // Convert to Celsius
        return "$temperature °C"
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return "N/A"
}

fun getThermalZoneTemperatures(): List<String> {
    val temperatures = mutableListOf<String>()
    val zones = listOf(
        "/sys/class/thermal/thermal_zone0/temp",
        "/sys/class/thermal/thermal_zone1/temp",
        "/sys/class/thermal/thermal_zone2/temp"
    )
    for (zone in zones) {
        try {
            val reader = BufferedReader(FileReader(zone))
            val tempString = reader.readLine()
            reader.close()
            val temperature = tempString.toFloat() / 1000.0 // Convert to Celsius
            temperatures.add("Zone $zone: $temperature°C")
        } catch (e: IOException) {
            temperatures.add("Zone $zone: N/A")
        }
    }
    return temperatures
}
