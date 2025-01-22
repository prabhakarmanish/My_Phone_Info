package coded.toolbox.deviceinfo.camera

import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.util.Range
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coded.toolbox.deviceinfo.markaziTextFont

data class CameraDetail(
    val megapixels: String,
    val pixelDimensions: String,
    val focalLength: String,
    val autoExposureModes: String,
    val compensationStep: String,
    val autofocusModes: String,
    val effectModes: String,
    val sceneModes: String,
    val videoStabilizationModes: String,
    val autoWhiteBalanceModes: String,
    val aperture: String,
    val digitalZoom: String,
    val flash: String,
    val exposureTime: String,
    val isoSensitivity: String,
    val faceDetection: String,
    val maxFaceCount: String,
    val compensationRange: String,
    val thumbnailSize: String,
    val supportedResolutions: String,
    val targetfpsrange: String
)

@Composable
fun CameraCard(
    title: String,
    megapixels: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.surface,
        label = "Background Color"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        label = "Content Color"
    )
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 16.dp else 6.dp, label = "Elevation"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f, label = "Scale"
    )
    val shadowColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFBB86FC) else Color.Transparent,
        label = "Shadow Color"
    )

    val titleFontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
    val megapixelsFontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
    val cardShape = RoundedCornerShape(16.dp)

    OutlinedCard(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .clip(cardShape)
            .clickable { onClick() }
            .scale(scale) // Apply scaling animation
            .shadow(
                elevation = elevation,
                shape = cardShape,
                clip = false
            )
            .background(shadowColor.copy(alpha = 0.3f)), // Add subtle glow effect
        colors = CardDefaults.outlinedCardColors(containerColor = backgroundColor),
        shape = cardShape,
        border = BorderStroke(
            2.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                androidx.compose.animation.Crossfade(
                    targetState = megapixels,
                    label = ""
                ) { megapixelText ->
                    Text(
                        text = megapixelText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = megapixelsFontWeight,
                            color = contentColor
                        ),
                    )
                }
                androidx.compose.animation.Crossfade(targetState = title, label = "") { titleText ->
                    Text(
                        text = titleText,
                        fontFamily = markaziTextFont,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = titleFontWeight,
                            color = contentColor
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CameraDetailRow(
    label: String,
    value: String?,
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val valuesList = value?.split(", ") ?: listOf()

    // Box for each row with 1.dp spacing
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp) // Space between boxes
            .then(
                // Apply rounded corners only to first and last items
                Modifier.clip(
                    when {
                        isFirstItem -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        isLastItem -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                        else -> RoundedCornerShape(0.dp) // No rounding for middle items
                    }
                )
            )
            .background(MaterialTheme.colorScheme.surface) // Set the background color
            .clickable { onClick?.invoke() } // Trigger onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp) // Vertical padding inside the box
        ) {
            // First half for label
            Column(
                modifier = Modifier
                    .weight(1f) // Take half of the space for label
                    .padding(end = 8.dp) // Space between label and values
            ) {
                Text(
                    text = label,
                    fontFamily = markaziTextFont,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    // fontWeight = FontWeight.SemiBold
                )
            }

            // Second half for values
            Column(
                modifier = Modifier
                    .weight(1f) // Take the other half of the space for values
            ) {
                valuesList.forEach { value ->
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    // Apply bottom padding outside the card if it's the last item
    if (isLastItem) {
        Spacer(modifier = Modifier.height(30.dp)) // Adds the bottom padding after the card
    }
}

fun getThumbnailSizes(characteristics: CameraCharacteristics): String {
    val thumbnailSizes = characteristics.get(CameraCharacteristics.JPEG_AVAILABLE_THUMBNAIL_SIZES)
    return thumbnailSizes?.joinToString(", ") { "${it.width} x ${it.height}" } ?: "N/A"
}

fun getSupportedResolutions(characteristics: CameraCharacteristics): String {
    val streamConfigurationMap =
        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
    val supportedResolutions = streamConfigurationMap?.getOutputSizes(SurfaceTexture::class.java)
        ?.joinToString(", ") { "${it.width} x ${it.height}" }
    return supportedResolutions ?: "N/A"
}

fun awbModeToString(mode: Int): String {
    return when (mode) {
        CameraCharacteristics.CONTROL_AWB_MODE_OFF -> "OFF"
        CameraCharacteristics.CONTROL_AWB_MODE_AUTO -> "AUTO"
        CameraCharacteristics.CONTROL_AWB_MODE_INCANDESCENT -> "INCANDESCENT"
        CameraCharacteristics.CONTROL_AWB_MODE_FLUORESCENT -> "FLUORESCENT"
        CameraCharacteristics.CONTROL_AWB_MODE_WARM_FLUORESCENT -> "WARM FLUORESCENT"
        CameraCharacteristics.CONTROL_AWB_MODE_DAYLIGHT -> "DAYLIGHT"
        CameraCharacteristics.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT -> "CLOUDY DAYLIGHT"
        CameraCharacteristics.CONTROL_AWB_MODE_TWILIGHT -> "TWILIGHT"
        CameraCharacteristics.CONTROL_AWB_MODE_SHADE -> "SHADE"
        else -> "Unknown"
    }
}

fun sceneModeToString(mode: Int): String {
    return when (mode) {
        CameraCharacteristics.CONTROL_SCENE_MODE_DISABLED -> "DISABLED"
        CameraCharacteristics.CONTROL_SCENE_MODE_FACE_PRIORITY -> "FACE PRIORITY"
        CameraCharacteristics.CONTROL_SCENE_MODE_ACTION -> "ACTION"
        CameraCharacteristics.CONTROL_SCENE_MODE_PORTRAIT -> "PORTRAIT"
        CameraCharacteristics.CONTROL_SCENE_MODE_LANDSCAPE -> "LANDSCAPE"
        CameraCharacteristics.CONTROL_SCENE_MODE_NIGHT -> "NIGHT"
        CameraCharacteristics.CONTROL_SCENE_MODE_THEATRE -> "THEATRE"
        CameraCharacteristics.CONTROL_SCENE_MODE_BEACH -> "BEACH"
        CameraCharacteristics.CONTROL_SCENE_MODE_SNOW -> "SNOW"
        CameraCharacteristics.CONTROL_SCENE_MODE_SUNSET -> "SUNSET"
        CameraCharacteristics.CONTROL_SCENE_MODE_STEADYPHOTO -> "STEADYPHOTO"
        CameraCharacteristics.CONTROL_SCENE_MODE_FIREWORKS -> "FIREWORKS"
        CameraCharacteristics.CONTROL_SCENE_MODE_SPORTS -> "SPORTS"
        CameraCharacteristics.CONTROL_SCENE_MODE_PARTY -> "PARTY"
        CameraCharacteristics.CONTROL_SCENE_MODE_CANDLELIGHT -> "CANDLELIGHT"
        else -> "Unknown"
    }
}

fun aeModeToString(mode: Int): String {
    return when (mode) {
        CameraCharacteristics.CONTROL_AE_MODE_OFF -> "OFF"
        CameraCharacteristics.CONTROL_AE_MODE_ON -> "ON"
        CameraCharacteristics.CONTROL_AE_MODE_ON_AUTO_FLASH -> "AUTO FLASH"
        CameraCharacteristics.CONTROL_AE_MODE_ON_ALWAYS_FLASH -> "ALWAYS FLASH"
        CameraCharacteristics.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE -> "AUTO FLASH REDEYE"
        CameraCharacteristics.CONTROL_AE_MODE_ON_EXTERNAL_FLASH -> "EXTERNAL FLASH"
        else -> "Unknown"
    }
}

fun afModeToString(mode: Int): String {
    return when (mode) {
        CameraCharacteristics.CONTROL_AF_MODE_OFF -> "OFF"
        CameraCharacteristics.CONTROL_AF_MODE_AUTO -> "AUTO"
        CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_PICTURE -> "CONTINUOUS PICTURE"
        CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_VIDEO -> "CONTINUOUS VIDEO"
        CameraCharacteristics.CONTROL_AF_MODE_MACRO -> "MACRO"
        CameraCharacteristics.CONTROL_AF_MODE_EDOF -> "EDOF"
        else -> "Unknown"
    }
}

fun effectModeToString(mode: Int): String {
    return when (mode) {
        CameraCharacteristics.CONTROL_EFFECT_MODE_OFF -> "OFF"
        CameraCharacteristics.CONTROL_EFFECT_MODE_MONO -> "MONO"
        CameraCharacteristics.CONTROL_EFFECT_MODE_NEGATIVE -> "NEGATIVE"
        CameraCharacteristics.CONTROL_EFFECT_MODE_SOLARIZE -> "SOLARIZE"
        CameraCharacteristics.CONTROL_EFFECT_MODE_SEPIA -> "SEPIA"
        CameraCharacteristics.CONTROL_EFFECT_MODE_POSTERIZE -> "POSTERIZE"
        CameraCharacteristics.CONTROL_EFFECT_MODE_WHITEBOARD -> "WHITEBOARD"
        CameraCharacteristics.CONTROL_EFFECT_MODE_BLACKBOARD -> "BLACKBOARD"
        CameraCharacteristics.CONTROL_EFFECT_MODE_AQUA -> "AQUA"
        else -> "Unknown"
    }
}

fun faceDetectionModeToString(mode: Int): String {
    return when (mode) {
        0 -> "Off"    // No face detection
        1 -> "Simple" // Simple face detection
        2 -> "Full"   // Full face detection
        else -> "Unknown"
    }
}

fun getTargetFpsRange(characteristics: CameraCharacteristics): String {
    val fpsRanges =
        characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)
    return fpsRanges?.joinToString(", ") { range ->
        "${range.lower} - ${range.upper} FPS"
    } ?: "N/A"
}

fun compensationRangeToString(compensationRange: Range<Int>?): String {
    return compensationRange?.let { "${it.lower} to ${it.upper}" } ?: "N/A"
}

