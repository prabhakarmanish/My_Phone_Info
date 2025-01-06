package coded.toolbox.deviceinfo.camera

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coded.toolbox.deviceinfo.markaziTextFont

@Composable
fun CameraScreen(navController: NavController, context: Context) {
    val cameraDetails = remember { mutableStateOf(emptyMap<String, CameraDetail>()) }
    val selectedCard = remember { mutableStateOf("Front") }

    // Dialog state
    val showDialog = remember { mutableStateOf(false) }
    val dialogTitle = remember { mutableStateOf("") }
    val dialogContent = remember { mutableStateOf("") }


    LaunchedEffect(Unit) {
        cameraDetails.value = getCameraDetails(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            CameraCard(
                title = "Front Camera",
                megapixels = cameraDetails.value["Front"]?.megapixels ?: "N/A",
                isSelected = selectedCard.value == "Front",
                onClick = { selectedCard.value = "Front" },
                modifier = Modifier.weight(1f)
            )
            CameraCard(
                title = "Back Camera",
                megapixels = cameraDetails.value["Back"]?.megapixels ?: "N/A",
                isSelected = selectedCard.value == "Back",
                onClick = { selectedCard.value = "Back" },
                modifier = Modifier.weight(1f)
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Make the LazyColumn take up the remaining space
                .padding(12.dp, 0.dp, 12.dp, 0.dp)
        ) {
            val selectedDetails = cameraDetails.value[selectedCard.value]
            if (selectedDetails != null) {

                item {
                    OutlinedCard(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary // A primary background color for attention
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp) // Add more spacing to separate it from other elements
                            .shadow(
                                1.dp,
                                RoundedCornerShape(16.dp)
                            ), // Add shadow for a "floating" effect
                        shape = RoundedCornerShape(16.dp), // Slightly larger rounded corners
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp), // Increase internal padding
                            verticalAlignment = Alignment.CenterVertically // Center align content vertically
                        ) {
                            Text(
                                text = "If the MegaPixel count shown is wrong, that means your device manufacturer has limited the access to the camera for third-party apps. Also, even if your device has more than one camera on one side, Android OS identifies them as a single camera for that side.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface, // Contrast text color
                                ),
                            )
                        }
                    }
                }
                item {
                    CameraDetailRow(
                        label = "Megapixel",
                        value = selectedDetails.megapixels,
                        isFirstItem = true,
                        onClick = {
                            dialogTitle.value = "Megapixel"
                            dialogContent.value = """
                Megapixels (MP) refer to the resolution of the camera. It is the number of pixels that can be captured in a single image.
                A higher megapixel count means more detail, but it doesn't necessarily mean better photo quality.
                Your camera's megapixels are currently ${selectedDetails.megapixels}.
            """.trimIndent()
                            showDialog.value = true
                        }
                    )
                }
                item {
                    CameraDetailRow(
                        label = "Pixel Dimensions",
                        value = selectedDetails.pixelDimensions,
                        onClick = {
                            dialogTitle.value = "Pixel Dimensions"
                            dialogContent.value = """
                Pixel dimensions refer to the width and height of an image captured by the camera.
                It determines the image resolution by specifying the number of pixels along each axis.
                Larger pixel dimensions mean higher resolution images with more detail.
                Your camera's pixel dimensions are currently ${selectedDetails.pixelDimensions}.
            """.trimIndent()
                            showDialog.value = true
                        }
                    )
                }
                item {
                    CameraDetailRow(
                        label = "Focal Length",
                        value = selectedDetails.focalLength,
                        onClick = {
                            dialogTitle.value = "Focal Length"
                            dialogContent.value = """
                Focal length is the distance between the camera's lens and the image sensor when the subject is in focus.
                It affects the field of view and magnification. A longer focal length results in a narrower field of view and higher magnification.
                Your camera's focal length is currently ${selectedDetails.focalLength}.
            """.trimIndent()
                            showDialog.value = true
                        }
                    )
                }
                item {
                    CameraDetailRow(
                        label = "Aperture",
                        value = selectedDetails.aperture,
                        onClick = {
                            dialogTitle.value = "Aperture"
                            dialogContent.value = """
                Aperture refers to the opening in the camera lens that allows light to pass through to the sensor.
                It affects the exposure and depth of field in an image. A lower f-number indicates a larger aperture, allowing more light.
                Your camera's aperture is currently ${selectedDetails.aperture}.
            """.trimIndent()
                            showDialog.value = true
                        }
                    )
                }
                item {
                    CameraDetailRow(
                        label = "ISO Sensitivity",
                        value = selectedDetails.isoSensitivity,
                        onClick = {
                            dialogTitle.value = "ISO Sensitivity"
                            dialogContent.value = """
                ISO sensitivity determines the camera sensor's sensitivity to light. A higher ISO allows the camera to capture images in low-light conditions,
                but it may introduce noise. Lower ISO values produce cleaner images but require more light.
                Your camera's ISO sensitivity is currently ${selectedDetails.isoSensitivity}.
            """.trimIndent()
                            showDialog.value = true
                        }
                    )
                }

                item {
                    CameraDetailRow(
                        label = "Exposure Time",
                        value = selectedDetails.exposureTime,
                        onClick = {
                            dialogTitle.value = "Exposure Time"
                            dialogContent.value = """
                Exposure time refers to how long the camera's sensor is exposed to light when taking a picture. 
                A longer exposure time allows more light, which is useful in low-light conditions, but it can cause motion blur.
                Your camera's exposure time is currently ${selectedDetails.exposureTime}.
            """.trimIndent()
                            showDialog.value = true
                        }
                    )
                }
                item {
                    CameraDetailRow(
                        label = "Digital Zoom",
                        value = selectedDetails.digitalZoom,
                        onClick = {
                            dialogTitle.value = "Digital Zoom"
                            dialogContent.value = """
                Digital zoom refers to the process of cropping the image and enlarging it to simulate zooming in, 
                which can lead to a loss of image quality. Unlike optical zoom, it doesn't physically change the lens' focal length.
                Your camera's digital zoom is currently ${selectedDetails.digitalZoom}.
            """.trimIndent()
                            showDialog.value = true
                        }
                    )
                }
                item {
                    CameraDetailRow(
                        label = "Flash",
                        value = selectedDetails.flash,
                        onClick = {
                            dialogTitle.value = "Flash"
                            dialogContent.value = """
                Flash is a feature that provides additional light when the surrounding environment is too dark.
                It helps illuminate the scene, but excessive use of flash can cause harsh lighting or unwanted reflections.
                Your camera's flash mode is currently ${selectedDetails.flash}.
            """.trimIndent()
                            showDialog.value = true
                        }
                    )
                }
                item {
                    CameraDetailRow(
                        label = "Supported Resolutions",
                        value = selectedDetails.supportedResolutions,
                        onClick = {
                            dialogTitle.value = "Supported Resolutions"
                            dialogContent.value = """
                Supported resolutions refer to the various image and video resolutions that the camera can capture.
                Higher resolutions result in more detailed images or videos, but also require more storage space.
                Your camera supports the following resolutions: ${selectedDetails.supportedResolutions}.
            """.trimIndent()
                            showDialog.value = true
                        }
                    )
                }

                item {
                    CameraDetailRow(
                        label = "Thumbnail Sizes",
                        value = selectedDetails.thumbnailSize,
                        onClick = {
                            dialogTitle.value = "Thumbnail Sizes"
                            dialogContent.value = """
                Thumbnail sizes refer to the small versions of images used for quick previews in galleries or apps.
                These smaller images are optimized for performance and loading speed. They usually have lower resolution than the full-size images.
                Your camera's thumbnail sizes are currently ${selectedDetails.thumbnailSize}.
            """.trimIndent()
                            showDialog.value = true
                        }
                    )
                }
                item {
                    CameraDetailRow(
                        label = "Target FPS Ranges",
                        value = selectedDetails.targetfpsrange,
                        onClick = {
                            dialogTitle.value = "Target FPS Ranges"
                            dialogContent.value = """
                Target FPS (Frames Per Second) ranges define the frame rate at which the camera captures video. 
                Higher FPS results in smoother video, especially for fast-moving scenes. Lower FPS can be used for a more cinematic effect.
                Your camera's target FPS ranges are currently ${selectedDetails.targetfpsrange}.
            """.trimIndent()
                            showDialog.value = true
                        }
                    )
                }
                item {
                    CameraDetailRow(
                        label = "Auto Exposure Modes",
                        value = selectedDetails.autoExposureModes,
                        onClick = {
                            dialogTitle.value = "Auto Exposure Modes"
                            dialogContent.value = """
                Auto Exposure Modes help the camera automatically adjust exposure settings to achieve the best brightness in varying lighting conditions.
                Different modes can be used for scenes with bright or dark lighting, or for capturing fast-moving objects.
                Your camera supports the following auto exposure modes: ${selectedDetails.autoExposureModes}.
            """.trimIndent()
                            showDialog.value = true
                        }
                    )
                }
                item {
                    CameraDetailRow(
                        label = "Compensation Step",
                        value = selectedDetails.compensationStep,
                        onClick = {
                            dialogTitle.value = "Compensation Step"
                            dialogContent.value = """
                Compensation step refers to the increment or decrement in exposure compensation when adjusting for brighter or darker images.
                This allows fine-tuning of exposure settings to optimize image quality in various lighting situations.
                Your camera's compensation step is currently ${selectedDetails.compensationStep}.
            """.trimIndent()
                            showDialog.value = true
                        }
                    )
                }

                item {
                    CameraDetailRow(
                        label = "Compensation Range",
                        value = selectedDetails.compensationRange,
                        onClick = {
                            dialogTitle.value = "Compensation Range"
                            dialogContent.value = """
                Compensation range refers to the range of exposure compensation values that the camera allows.
                Exposure compensation helps adjust the brightness of an image by increasing or decreasing exposure. A wider range gives more flexibility to adjust the image.
                Your camera's compensation range is currently ${selectedDetails.compensationRange}.
            """.trimIndent()
                            showDialog.value = true
                        }
                    )
                }
                item {
                    CameraDetailRow(
                        label = "AutoFocus Modes",
                        value = selectedDetails.autofocusModes,
                        onClick = {
                            dialogTitle.value = "AutoFocus Modes"
                            dialogContent.value = """
                AutoFocus modes determine how the camera automatically focuses on subjects. 
                Some modes may focus on faces, objects, or specific areas of the scene. Different modes work best for different scenarios, like portraits or landscapes.
                Your camera supports the following autofocus modes: ${selectedDetails.autofocusModes}.
            """.trimIndent()
                            showDialog.value = true
                        }
                    )
                }
                item {
                    CameraDetailRow(
                        label = "Effect Modes",
                        value = selectedDetails.effectModes,
                        onClick = {
                            dialogTitle.value = "Effect Modes"
                            dialogContent.value = """
                Effect modes allow you to apply various visual effects to your photos or videos, such as black and white, sepia, or other artistic filters.
                These effects modify the image to give it a distinct look or feel.
                Your camera supports the following effect modes: ${selectedDetails.effectModes}.
            """.trimIndent()
                            showDialog.value = true
                        }
                    )
                }
                item {
                    CameraDetailRow(
                        label = "Scene Modes",
                        value = selectedDetails.sceneModes,
                        onClick = {
                            dialogTitle.value = "Scene Modes"
                            dialogContent.value = """
                Scene modes are predefined settings that optimize camera parameters for specific types of scenes, such as portraits, landscapes, or night photography.
                These modes adjust things like exposure, focus, and color balance to help you get the best shot.
                Your camera supports the following scene modes: ${selectedDetails.sceneModes}.
            """.trimIndent()
                            showDialog.value = true
                        }
                    )
                }
                item {
                    CameraDetailRow(
                        label = "Video Stabilization Modes",
                        value = selectedDetails.videoStabilizationModes,
                        onClick = {
                            dialogTitle.value = "Video Stabilization Modes"
                            dialogContent.value = """
                Video stabilization modes are used to reduce or eliminate the effects of camera shake during video recording.
                This is especially useful when recording videos while moving or in low light conditions. 
                Your camera supports the following video stabilization modes: ${selectedDetails.videoStabilizationModes}.
            """.trimIndent()
                            showDialog.value = true
                        }
                    )
                }
                item {
                    CameraDetailRow(
                        label = "Auto White Balance Modes",
                        value = selectedDetails.autoWhiteBalanceModes,
                        onClick = {
                            dialogTitle.value = "Auto White Balance Modes"
                            dialogContent.value = """
                Auto White Balance (AWB) modes adjust the color temperature of your photos to ensure that whites appear white and other colors are balanced.
                Different AWB modes can be used to achieve the best color balance depending on lighting conditions.
                Your camera supports the following auto white balance modes: ${selectedDetails.autoWhiteBalanceModes}.
            """.trimIndent()
                            showDialog.value = true
                        }
                    )
                }
                item {
                    CameraDetailRow(
                        label = "Face Detection",
                        value = selectedDetails.faceDetection,
                        onClick = {
                            dialogTitle.value = "Face Detection"
                            dialogContent.value = """
                Face detection technology helps the camera automatically identify and focus on faces within the frame.
                This is useful for ensuring that faces are well-lit and in focus, especially in portrait photography.
                Your camera's face detection feature is ${selectedDetails.faceDetection}.
            """.trimIndent()
                            showDialog.value = true
                        }
                    )
                }
                item {
                    CameraDetailRow(
                        label = "Max Face Count",
                        value = selectedDetails.maxFaceCount,
                        isLastItem = true,
                        onClick = {
                            dialogTitle.value = "Max Face Count"
                            dialogContent.value = """
                Max face count refers to the maximum number of faces the camera's face detection system can identify and focus on in a scene.
                This is important for group shots or situations where multiple people are in the frame.
                Your camera's maximum face count is ${selectedDetails.maxFaceCount}.
            """.trimIndent()
                            showDialog.value = true
                        }
                    )
                }


            } else {
                item {
                    Text(
                        text = "Fetching details...",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        // Dialog to show details on click
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = { Text(dialogTitle.value) },
                text = { Text(dialogContent.value) },
                confirmButton = {
                    TextButton(onClick = { showDialog.value = false }) {
                        Text("OK")
                    }
                }
            )
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

fun getCameraDetails(context: Context): Map<String, CameraDetail> {
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val cameraIds = cameraManager.cameraIdList
    val details = mutableMapOf<String, CameraDetail>()

    for (cameraId in cameraIds) {
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
        val sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
        val focalLengths =
            characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
        val aeModes = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES)
        val afModes = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)
        val effectModes = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS)
        val compensationStep =
            characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP)
        val aperture = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
            ?.joinToString(", ") ?: "N/A"
        val digitalZoom =
            characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)?.toString()
                ?: "N/A"
        val flash =
            characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)?.let { "Supported" }
                ?: "Not Supported"
        val exposureTime =
            characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
                ?.let { "${it.lower} - ${it.upper} ns" } ?: "N/A"
        val isoSensitivity =
            characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
                ?.let { "${it.lower} - ${it.upper}" } ?: "N/A"
        val faceDetection =
            characteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES)
                ?.joinToString(", ") { faceDetectionModeToString(it) } ?: "N/A"
        val maxFaceCount =
            characteristics.get(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT)?.toString()
                ?: "N/A"
        val compensationRange =
            compensationRangeToString(characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE))
        val sceneModes = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES)
        val opticalStabilization =
            characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
        val whiteBalanceModes =
            characteristics.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES)
        val thumbnailSizes = getThumbnailSizes(characteristics)
        val supportedResolutions = getSupportedResolutions(characteristics)
        val targetfpsrange = getTargetFpsRange(characteristics)

        if (sensorSize != null) {
            val megapixels =
                "%.2f MP".format(calculateMegapixels(sensorSize.width, sensorSize.height))
            val pixelDimensions = "${sensorSize.width} x ${sensorSize.height}"
            val focalLength =
                focalLengths?.joinToString(", ") { "%.2f mm".format(it) } ?: "Data unavailable"

            val aeModesText = aeModes?.joinToString(", ") { aeModeToString(it) } ?: "N/A"
            val afModesText = afModes?.joinToString(", ") { afModeToString(it) } ?: "N/A"
            val effectModesText =
                effectModes?.joinToString(", ") { effectModeToString(it) } ?: "N/A"
            val compensationStepText = compensationStep?.toString() ?: "N/A"
            val sceneModesText = sceneModes?.joinToString(", ") { sceneModeToString(it) }
                ?: "N/A"
            val videoStabilizationText =
                if (opticalStabilization != null && opticalStabilization.isNotEmpty()) "Supported" else "Not Supported"
            val whiteBalanceModesText =
                whiteBalanceModes?.joinToString(", ") { awbModeToString(it) }
                    ?: "N/A"

            when (lensFacing) {
                CameraCharacteristics.LENS_FACING_FRONT -> {
                    details["Front"] = CameraDetail(
                        megapixels,
                        pixelDimensions,
                        focalLength,
                        aeModesText,
                        compensationStepText,
                        afModesText,
                        effectModesText,
                        sceneModesText,
                        videoStabilizationText,
                        whiteBalanceModesText,
                        aperture,
                        digitalZoom,
                        flash,
                        exposureTime,
                        isoSensitivity,
                        faceDetection,
                        maxFaceCount,
                        compensationRange,
                        thumbnailSizes,
                        supportedResolutions,
                        targetfpsrange,
                    )
                }

                CameraCharacteristics.LENS_FACING_BACK -> {
                    details["Back"] = CameraDetail(
                        megapixels,
                        pixelDimensions,
                        focalLength,
                        aeModesText,
                        compensationStepText,
                        afModesText,
                        effectModesText,
                        sceneModesText,
                        videoStabilizationText,
                        whiteBalanceModesText,
                        aperture,
                        digitalZoom,
                        flash,
                        exposureTime,
                        isoSensitivity,
                        faceDetection,
                        maxFaceCount,
                        compensationRange,
                        thumbnailSizes,
                        supportedResolutions,
                        targetfpsrange,
                    )
                }
            }
        }
    }

    return details
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

fun calculateMegapixels(width: Int, height: Int): Float {
    val totalPixels = width * height
    return totalPixels / 1_000_000f // Convert to megapixels
}


fun compensationRangeToString(compensationRange: Range<Int>?): String {
    return compensationRange?.let { "${it.lower} to ${it.upper}" } ?: "N/A"
}

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
@Preview(showBackground = true)
fun CameraScreenPreview() {
    CameraScreen(
        navController = rememberNavController(),
        context = LocalContext.current
    )
}

@Composable
@Preview
fun CameraDetailRowPreview() {
    Column {
        // Preview the first item (top corners rounded)
        CameraDetailRow(
            label = "Megapixels",
            value = "12.0 MP, 16.0 MP",
            isFirstItem = true
        )
        // Preview the middle item (no rounded corners)
        CameraDetailRow(
            label = "Pixel Dimensions",
            value = "4000 x 3000, 6000 x 4000"
        )
        // Preview the last item (bottom corners rounded)
        CameraDetailRow(
            label = "ISO Sensitivity",
            value = "100 - 3200",
            isLastItem = true
        )
    }
}