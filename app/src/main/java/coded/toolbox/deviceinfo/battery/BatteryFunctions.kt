package coded.toolbox.deviceinfo.battery

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coded.toolbox.deviceinfo.markaziTextFont

@Composable
fun CameraDetailRow(
    label: String,
    value: String?,
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp) // Space between boxes
            .then(
                Modifier.clip(
                    when {
                        isFirstItem -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        isLastItem -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                        else -> RoundedCornerShape(0.dp)
                    }
                )
            )
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick?.invoke() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = markaziTextFont,
                    style = MaterialTheme.typography.titleLarge,

                    )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = value ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    if (isLastItem) {
        Spacer(modifier = Modifier.height(16.dp))
    }
}


fun getBatteryCapacity(context: Context): Double {
    var batteryCapacity = 0.0
    try {
        val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
        val powerProfile = powerProfileClass.getConstructor(Context::class.java).newInstance(context)
        val method = powerProfileClass.getMethod("getBatteryCapacity")
        batteryCapacity = method.invoke(powerProfile) as Double
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return batteryCapacity
}
