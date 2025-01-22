package coded.toolbox.deviceinfo.battery

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.sin

class BatteryViewModel(application: Application) : AndroidViewModel(application) {
    // MutableLiveData for battery data
    val batteryLevel = MutableLiveData(0)
    val batteryHealth = MutableLiveData("Unknown")
    val batteryStatus = MutableLiveData("Unknown")
    val powerSource = MutableLiveData("Unknown")
    val batteryTechnology = MutableLiveData("Unknown")
    val batteryCurrent = MutableLiveData(0)
    val batteryPower = MutableLiveData(0f)
    val batteryVoltage = MutableLiveData(0)
    val batteryCapacity = MutableLiveData(0)
    val isCharging = MutableLiveData(false)
    val currentIconIndex = MutableLiveData(0)
    val batteryTemperature = MutableLiveData(0)

    val graphData = MutableLiveData<List<Float>>(emptyList())
    private val _currentGraphData = MutableStateFlow<List<Float>>(emptyList())
    val currentGraphData: StateFlow<List<Float>> = _currentGraphData

    val showDialog = MutableLiveData(false)
    val dialogTitle = MutableLiveData("")
    val dialogDescription = MutableLiveData("")

    private val context = application.applicationContext
    private var time = 0f
    private var animationJob: Job? = null

    init {
        startGraphSimulation()
    }

    private fun startGraphSimulation() {
        viewModelScope.launch(Dispatchers.Main) {
            while (true) {
                val newPower = (sin(time) * 10).toFloat() // Simulate a sine wave
                batteryPower.value = newPower
                val newGraphData = graphData.value?.toMutableList() ?: mutableListOf()
                newGraphData.add(newPower)
                if (newGraphData.size > 100) newGraphData.removeAt(0) // Keep graph data limited
                graphData.value = newGraphData
                time += 0.1f
                delay(1000) // Update every second
            }
        }
    }

    fun updateGraphData(newDataPoint: Float) {
        _currentGraphData.value = _currentGraphData.value.toMutableList().apply {
            add(newDataPoint)
            if (size > 100) removeAt(0) // Limit graph data size
        }
    }

    fun updateBatteryStatus(intent: Intent) {
        batteryLevel.value = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
        batteryHealth.value = when (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified Failure"
            else -> "Unknown"
        }
        batteryStatus.value = when (intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown"
        }
        powerSource.value = when (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Charger"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Battery"
        }
        batteryTechnology.value = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
        batteryVoltage.value = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) // in mV
        batteryCapacity.value = getBatteryCapacityInMah(context).toInt()

        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val currentMicroAmps = try {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        } catch (e: Exception) {
            0
        }

        val fallbackCurrent = getCurrentNowFromFile()
        // Update the current value to support both positive and negative readings
        batteryCurrent.value = when {
            currentMicroAmps != 0 -> (currentMicroAmps / 1000) // µA to mA
            fallbackCurrent != 0 -> fallbackCurrent // Use fallback if available
            else -> 0
        }

        if (currentMicroAmps == 0 && fallbackCurrent == 0) {
            Log.w("BatteryViewModel", "Current reading is not supported on this device.")
        }

        batteryPower.value = (batteryVoltage.value!! / 1000.0 * batteryCurrent.value!! / 1000.0).toFloat() // V * A = W
        batteryTemperature.value = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val isCurrentlyCharging = batteryStatus.value == "Charging" || batteryStatus.value == "Full"
        isCharging.value = isCurrentlyCharging

        if (isCurrentlyCharging) {
            startChargingAnimation()
        } else {
            stopChargingAnimation()
            currentIconIndex.value = (batteryLevel.value!! / 10).coerceAtMost(7)
        }
    }

    private fun getCurrentNowFromFile(): Int {
        val currentFile = File("/sys/class/power_supply/battery/current_now")
        return if (currentFile.exists()) {
            try {
                val currentMicroAmps = currentFile.readText().trim().toInt()
                currentMicroAmps / 1000 // Convert µA to mA, including negative values
            } catch (e: Exception) {
                0 // Handle potential exceptions
            }
        } else {
            0 // Return 0 if file does not exist
        }
    }


    private fun startChargingAnimation() {
        animationJob?.cancel()
        animationJob = viewModelScope.launch {
            while (isCharging.value == true) {
                currentIconIndex.value = (currentIconIndex.value!! + 1) % 8
                delay(500) // Change icon every 500ms
            }
        }
    }

    private fun stopChargingAnimation() {
        animationJob?.cancel()
        animationJob = null
    }

    fun getBatteryStatusReceiver(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                updateBatteryStatus(intent)
            }
        }
    }

    fun getBatteryCapacityInMah(context: Context): Float {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val capacityPercent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        val designCapacityInMah = getDesignBatteryCapacityInMah(context)
        return if (designCapacityInMah > 0) {
            (capacityPercent / 100f) * designCapacityInMah
        } else {
            3000f * (capacityPercent / 100f)
        }
    }

    private fun getDesignBatteryCapacityInMah(context: Context): Float {
        val path = "/sys/class/power_supply/battery/charge_full_design"
        val designCapacityFile = File(path)
        return if (designCapacityFile.exists()) {
            try {
                val designCapacity = designCapacityFile.readText().trim().toInt()
                designCapacity.toFloat() / 1000f
            } catch (e: Exception) {
                0f
            }
        } else {
            0f
        }
    }
}
