package coded.toolbox.deviceinfo.dashboard

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class DashboardViewModel : ViewModel() {

    private val ramUsageState = mutableStateOf(Pair(0, 1)) // (used RAM, total RAM)
    private val ramUsageHistory = mutableStateListOf<Int>() // Store historical RAM data

    val cpuCores = mutableStateListOf<Int>() // List to store CPU cores
    private val cpuUsage = mutableStateListOf<Float>() // List to store CPU usage percentages
    val cpuFrequencies = mutableStateListOf<Float>() // List to store CPU frequencies

    init {
        getCpuInfo()
    }

    fun startRamUsageMonitoring(context: Context) {
        viewModelScope.launch {
            while (true) {
                val newRamUsage = getRamUsageValues(context)
                ramUsageState.value = newRamUsage
                ramUsageHistory.add(newRamUsage.first)
                if (ramUsageHistory.size > 100) {
                    ramUsageHistory.removeAt(0) // Keep only the last 100 data points
                }
                delay(1000) // Update every second
            }
        }
    }

    private fun getRamUsageValues(context: Context): Pair<Int, Int> {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalRam = (memoryInfo.totalMem / (1024 * 1024)).toInt() // Convert to MB
        val usedRam =
            ((memoryInfo.totalMem - memoryInfo.availMem) / (1024 * 1024)).toInt() // Convert to MB

        return Pair(usedRam, totalRam)
    }

    private fun getCpuInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            val numCores = Runtime.getRuntime().availableProcessors()
            cpuCores.clear()
            for (i in 0 until numCores) {
                cpuCores.add(i + 1) // Add core number
            }
        }
    }

    fun startCpuUsageMonitoring() {
        viewModelScope.launch {
            while (true) {
                // Retrieve CPU usage percentages
                val usage = getCpuUsage()
                cpuUsage.clear()
                cpuUsage.addAll(usage) // Update the list with usage values

                // Retrieve CPU frequencies (in MHz)
                val frequencies = getCpuFrequencies()
                cpuFrequencies.clear()
                cpuFrequencies.addAll(frequencies) // Update the list with frequency values

                delay(1000)  // Update every second
            }
        }
    }

    private fun getCpuUsage(): List<Float> {
        val usageList = mutableListOf<Float>()

        // Simulate CPU usage values as placeholders (can be replaced with actual data retrieval)
        for (i in cpuCores.indices) {
            val usage = (Math.random() * 100).toFloat()  // Example: Random usage percentage
            usageList.add(usage)
        }

        return usageList
    }

    private fun getCpuFrequencies(): List<Float> {
        val frequencyList = mutableListOf<Float>()

        for (i in cpuCores.indices) {
            val cpuFreqPath = "/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq"
            try {
                val frequencyStr = File(cpuFreqPath).readText().trim()
                if (frequencyStr.isNotEmpty()) {
                    val frequency = frequencyStr.toFloat() / 1000 // Convert to MHz
                    frequencyList.add(frequency)
                } else {
                    frequencyList.add(0f) // Handle the case when the value is empty
                }
            } catch (e: Exception) {
                Log.e("CPU_FREQUENCY", "Failed to read CPU frequency for core $i", e)
                frequencyList.add(0f) // Fallback value in case of failure
            }
        }

        return frequencyList
    }
}

