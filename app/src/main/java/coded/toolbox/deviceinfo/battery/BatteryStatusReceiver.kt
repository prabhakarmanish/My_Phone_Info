package coded.toolbox.deviceinfo.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BatteryStatusReceiver(
    private val onBatteryStatusChanged: (Intent) -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Call the callback function with the received intent
        onBatteryStatusChanged(intent)
    }
}