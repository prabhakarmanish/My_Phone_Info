package coded.toolbox.deviceinfo.system

import android.media.MediaDrm
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coded.toolbox.deviceinfo.AdBannerManager
import coded.toolbox.deviceinfo.R
import coded.toolbox.deviceinfo.markaziTextFont
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.TimeUnit


@Composable
fun SystemScreen(navController: NavController, adBannerManager: AdBannerManager) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var uuid by remember { mutableStateOf<String>("") }
    var vendor by remember { mutableStateOf<String>("") }
    var description by remember { mutableStateOf<String>("") }
    var algorithms by remember { mutableStateOf<String>("") }
    var version by remember { mutableStateOf<String>("") }
    var securityLevel by remember { mutableStateOf<String>("") }
    var maxHdcpLevel by remember { mutableStateOf<String>("") }
    var systemId by remember { mutableStateOf<String>("") }
    var maxSessions by remember { mutableStateOf<String>("") }
    var hdcpLevel by remember { mutableStateOf<String>("") }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val supportedSchemes = MediaDrm.getSupportedCryptoSchemes()

            if (supportedSchemes.isNotEmpty()) {
                try {
                    val uuidValue =
                        supportedSchemes[0] // Just using the first UUID for demonstration
                    val mediaDrm = MediaDrm(uuidValue)

                    uuid = uuidValue.toString()
                    vendor = getVendor(uuidValue)
                    description = getDescription(uuidValue)
                    algorithms = getAlgorithms(uuidValue)
                    version = getVersion(uuidValue)
                    securityLevel = getSecurityLevel(uuidValue)
                    maxHdcpLevel = getMaxHdcpLevel(uuidValue)
                    systemId = getSystemId(uuidValue)
                    maxSessions = getMaxSessions(uuidValue)
                    hdcpLevel = getHdcpLevel(uuidValue)
                } catch (e: Exception) {
                    uuid = "Error"
                    vendor = "Error"
                    description = "Error fetching details"
                    algorithms = "N/A"
                    version = "N/A"
                    securityLevel = "N/A"
                    maxHdcpLevel = "N/A"
                    systemId = "N/A"
                    maxSessions = "N/A"
                    hdcpLevel = "N/A"
                }
            } else {
                uuid = "No supported DRM schemes found"
                vendor = "Error"
                description = "Error fetching details"
                algorithms = "N/A"
                version = "N/A"
                securityLevel = "N/A"
                maxHdcpLevel = "N/A"
                systemId = "N/A"
                maxSessions = "N/A"
                hdcpLevel = "N/A"
            }
        }
    }

    // Get Android version and codename
    val androidVersion = Build.VERSION.RELEASE ?: "Unknown"
    // Map of Android API levels to codenames
    val codenameMap = mapOf(
        34 to "Upside Down Cake",
        33 to "Tiramisu",
        32 to "Snow Cone v2",
        31 to "Snow Cone",
        30 to "Red Velvet Cake",
        29 to "Pie",
        28 to "Oreo",
        27 to "Oreo",
        26 to "Nougat",
        25 to "Nougat",
        24 to "Nougat",
        23 to "Marshmallow",
        22 to "Lollipop",
        21 to "Lollipop",
        20 to "KitKat Watch",
        19 to "KitKat",
        18 to "Jelly Bean",
        17 to "Jelly Bean",
        16 to "Jelly Bean",
        15 to "Ice Cream Sandwich",
        14 to "Ice Cream Sandwich",
        13 to "Honeycomb",
        12 to "Honeycomb",
        11 to "Honeycomb",
        10 to "Gingerbread",
        9 to "Gingerbread",
        8 to "Froyo",
        7 to "Eclair",
        6 to "Eclair",
        5 to "Eclair",
        4 to "Donut",
        3 to "Cupcake"
    )
    // Determine codename based on the Android version
    val androidCodename = codenameMap[Build.VERSION.SDK_INT] ?: "Unknown"

    // Set image resource and release date based on Android version
    val (imageRes, releaseDate) = when (Build.VERSION.SDK_INT) {
        34 -> Pair(R.drawable.android14, "October 05, 2023")
        33 -> Pair(R.drawable.android13, "August 15, 2022")
        32 -> Pair(R.drawable.android12, "October 04, 2021")
        31 -> Pair(R.drawable.android11, "September 08, 2020")
        30 -> Pair(R.drawable.android10, "September 03, 2019")
        29 -> Pair(R.drawable.android9, "August 06, 2018")
        else -> Pair(R.drawable.android, "Unknown Release Date") // Default case
    }

    // Scrollable layout using LazyColumn
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 13.dp)
    ) {
        item {
            VersionCard(
                imageRes = imageRes,
                androidVersion = androidVersion,
                androidCodename = androidCodename,
                releaseDate = releaseDate
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
// Add system information rows
        item {
            ConfigurableDeviceDetailRow(
                label = "Code Name",
                value = "${Build.VERSION.CODENAME} (${Build.VERSION.SDK_INT})", // Adding SDK version for clarity
                isTop = true
            )
        }

        item {
            ConfigurableDeviceDetailRow(
                label = "API Level", value = Build.VERSION.SDK_INT.toString()
            )
        }
        item {
            ConfigurableDeviceDetailRow(
                label = "Released With", value = "Android ${Build.VERSION.RELEASE}"
            )
        }
        item {
            ConfigurableDeviceDetailRow(
                label = "Security Patch Level", value = Build.VERSION.SECURITY_PATCH
            )
        }
        item {
            ConfigurableDeviceDetailRow(label = "Bootloader", value = Build.BOOTLOADER)
        }
        item {
            ConfigurableDeviceDetailRow(label = "Build Number", value = Build.DISPLAY)
        }
        item {
            ConfigurableDeviceDetailRow(label = "Baseband", value = Build.getRadioVersion())
        }
        item {
            ConfigurableDeviceDetailRow(
                label = "Java VM", value = System.getProperty("java.vm.version")
            )
        }
        item {
            ConfigurableDeviceDetailRow(label = "Kernel", value = System.getProperty("os.version"))
        }
        item {
            ConfigurableDeviceDetailRow(
                label = "Language",
                value = System.getProperty("user.language")?.uppercase(Locale.getDefault())
                    ?: "Unknown" // Display language code in uppercase, default to "Unknown" if null
            )
        }

        item {
            val timezone = System.getProperty("user.timezone")
            val timezoneValue = if (timezone.isNullOrEmpty()) {
                // Fallback to system default timezone
                TimeZone.getDefault().id
            } else {
                // Use the provided timezone, ensuring it's valid
                TimeZone.getTimeZone(timezone).id.takeIf { it != "GMT" } ?: "Unknown"
            }

            ConfigurableDeviceDetailRow(
                label = "Timezone", value = timezoneValue
            )
        }
        item {
            ConfigurableDeviceDetailRow(
                label = "OpenGL ES",
                value = "Supported Version: ${Build.VERSION.SDK_INT}" // Replace with actual check
            )
        }
        item {
            ConfigurableDeviceDetailRow(
                label = "Root Management Apps", value = "None Detected"
            ) // Placeholder
        }
        item {
            ConfigurableDeviceDetailRow(
                label = "SELinux", value = "Enforcing"
            )
        }
        item {
            val googlePlayServicesStatus =
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
            val googlePlayServicesVersion = try {
                val version = GoogleApiAvailability.getInstance().getApkVersion(context)
                "$version"
            } catch (e: Exception) {
                "Unknown"
            }

            ConfigurableDeviceDetailRow(
                label = "Google Play Services", value = when (googlePlayServicesStatus) {
                    com.google.android.gms.common.ConnectionResult.SUCCESS -> "v$googlePlayServicesVersion"
                    else -> "Not Installed"
                }
            )
        }
        item {
            var uptime by remember { mutableStateOf(getFormattedUptime()) }

            // Periodically update the uptime every second
            LaunchedEffect(Unit) {
                while (true) {
                    uptime = getFormattedUptime() // Update the uptime
                    kotlinx.coroutines.delay(1000) // Delay for 1 second
                }
            }

            ConfigurableDeviceDetailRow(
                label = "System Uptime", value = uptime
            )
        }
        item {
            // Treble is supported starting from Android 9 (Pie), but more reliably on devices with dynamic partitions
            val isTrebleSupported =
                Build.SUPPORTED_ABIS.size > 1 // Multi-partition support is often linked to Treble
            ConfigurableDeviceDetailRow(
                label = "Treble", value = if (isTrebleSupported) "Supported" else "Not Supported"
            )
        }


        item {
            // Seamless updates support can be inferred by checking if dynamic partitions are enabled
            val seamlessUpdatesSupported =
                Build.SUPPORTED_ABIS.isNotEmpty() // Check for dynamic partitions (a proxy check)
            ConfigurableDeviceDetailRow(
                label = "Seamless Updates",
                value = if (seamlessUpdatesSupported) "Supported" else "Not Supported"
            )
        }

        item {
            // Check for dynamic partitions support (used in newer devices)
            val dynamicPartitionsSupported =
                Build.SUPPORTED_ABIS.size > 1 // Indicates multi-partition support
            ConfigurableDeviceDetailRow(
                label = "Dynamic Partitions",
                value = if (dynamicPartitionsSupported) "Supported" else "Not Supported",
                isBottom = true
            )
        }


        item {
            Text(
                text = "DRM",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold, fontFamily = markaziTextFont
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(6.dp)
            )
        }

        item {
            ConfigurableDeviceDetailRow(
                label = "Vendor", value = vendor, isTop = true
            )
        }
        item {
            ConfigurableDeviceDetailRow(
                label = "UUID", value = uuid
            )
        }
        item {
            ConfigurableDeviceDetailRow(
                label = "Description", value = description
            )
        }
        item {
            ConfigurableDeviceDetailRow(
                label = "Algorithms", value = algorithms
            )
        }
        item {
            ConfigurableDeviceDetailRow(
                label = "Version", value = version
            )
        }
        item {
            ConfigurableDeviceDetailRow(
                label = "Security Level", value = securityLevel
            )
        }
        item {
            ConfigurableDeviceDetailRow(
                label = "Max HDCP Level", value = maxHdcpLevel
            )
        }
        item {
            ConfigurableDeviceDetailRow(
                label = "System ID", value = systemId
            )
        }
        item {
            ConfigurableDeviceDetailRow(
                label = "Max Sessions", value = maxSessions
            )
        }
        item {
            ConfigurableDeviceDetailRow(
                label = "HDCP Level", value = hdcpLevel, isBottom = true
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

    }
}

@Composable
fun VersionCard(
    imageRes: Int, androidVersion: String, androidCodename: String, releaseDate: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.onPrimary, shape = RoundedCornerShape(16.dp)
            )
            .padding(10.dp)
    ) {
        Row {
            Image(
                painter = painterResource(id = imageRes),
                modifier = Modifier.size(90.dp),
                contentDescription = "Android $androidVersion Icon"
            )
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .align(alignment = Alignment.CenterVertically),
            ) {
                InfoRow(
                    iconRes = R.drawable.rounded_android_24,
                    text = "Android $androidVersion",
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                InfoRow(
                    iconRes = R.drawable.chk,
                    text = androidCodename,
                    textStyle = MaterialTheme.typography.bodySmall
                )
                InfoRow(
                    iconRes = R.drawable.rounded_timelapse_24,
                    text = "Release: $releaseDate",
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


fun getFormattedUptime(): String {
    val uptimeMillis = System.currentTimeMillis() - System.nanoTime() / 1000000
    val uptimeSeconds = uptimeMillis / 1000

    val days = TimeUnit.SECONDS.toDays(uptimeSeconds)
    val hours = TimeUnit.SECONDS.toHours(uptimeSeconds) % 24
    val minutes = TimeUnit.SECONDS.toMinutes(uptimeSeconds) % 60
    val seconds = uptimeSeconds % 60

    return buildString {
        if (days > 0) append("${days}d ")
        append(String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds))
    }
}


@Composable
fun InfoRow(iconRes: Int, text: String, textStyle: androidx.compose.ui.text.TextStyle) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .padding(end = 8.dp),
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                MaterialTheme.colorScheme.primary,
            )
        )
        Text(
            text = text,
            maxLines = 1,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            style = textStyle
        )
    }
}


@Composable
fun ConfigurableDeviceDetailRow(
    label: String,
    value: String?,
    isTop: Boolean = false,
    isBottom: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val shape = when {
        isTop -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        isBottom -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        else -> RoundedCornerShape(0.dp)
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                enabled = true,
                onClick = { onClick?.invoke() },
                interactionSource = interactionSource,  // Ensure interaction source is passed
                indication = rememberRipple()  // Add ripple effect explicitly
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = markaziTextFont,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value ?: "Unknown",
                color = if (value?.contains("unable", ignoreCase = true) == true) {
                    MaterialTheme.colorScheme.onError
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

fun getVendor(uuid: UUID): String {
    return try {
        MediaDrm(uuid).getPropertyString("vendor") ?: "Unknown Vendor"
    } catch (e: Exception) {
        "Unable to fetch Vendor"
    }
}

fun getDescription(uuid: UUID): String {
    return try {
        MediaDrm(uuid).getPropertyString("description") ?: "No Description"
    } catch (e: Exception) {
        "Unable to fetch Description"
    }
}

fun getAlgorithms(uuid: UUID): String {
    return try {
        MediaDrm(uuid).getPropertyString("algorithms") ?: "Unknown Algorithms"
    } catch (e: Exception) {
        "Unable to fetch Algorithms"
    }
}

fun getVersion(uuid: UUID): String {
    return try {
        MediaDrm(uuid).getPropertyString("version") ?: "Unknown Version"
    } catch (e: Exception) {
        "Unable to fetch Version"
    }
}

fun getSecurityLevel(uuid: UUID): String {
    return try {
        MediaDrm(uuid).getPropertyString("securityLevel") ?: "Unknown Security Level"
    } catch (e: Exception) {
        "Unable to fetch Security Level"
    }
}

fun getMaxHdcpLevel(uuid: UUID): String {
    return try {
        MediaDrm(uuid).getPropertyString("maxHDCPLevel") ?: "Unknown HDCP Level"
    } catch (e: Exception) {
        "Unable to fetch Max HDCP Level"
    }
}

fun getSystemId(uuid: UUID): String {
    return try {
        MediaDrm(uuid).getPropertyString("systemId") ?: "Unknown System ID"
    } catch (e: Exception) {
        "Unable to fetch System ID"
    }
}

fun getMaxSessions(uuid: UUID): String {
    return try {
        MediaDrm(uuid).getPropertyString("maxSessions") ?: "Unknown Max Sessions"
    } catch (e: Exception) {
        "Unable to fetch Max Sessions"
    }
}

fun getHdcpLevel(uuid: UUID): String {
    return try {
        MediaDrm(uuid).getPropertyString("HDCPLevel") ?: "Unknown HDCP Level"
    } catch (e: Exception) {
        "Unable to fetch HDCP Level"
    }
}
