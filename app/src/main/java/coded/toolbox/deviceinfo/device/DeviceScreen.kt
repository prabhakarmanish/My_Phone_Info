package coded.toolbox.deviceinfo.device

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coded.toolbox.deviceinfo.AdBannerManager
import coded.toolbox.deviceinfo.R
import coded.toolbox.deviceinfo.markaziTextFont
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

@Composable
fun DeviceScreen(navController: NavController, adBannerManager: AdBannerManager) {
    val context = LocalContext.current

    val deviceName = Build.MODEL
    val manufacturer = Build.MANUFACTURER.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
    }
    val device = Build.DEVICE
    val board = Build.BOARD
    val hardware = Build.HARDWARE
    val brand = Build.BRAND.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
    }

    var dialogTitle by remember { mutableStateOf("") }
    var dialogContent by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    fun showDetails(title: String, content: String) {
        dialogTitle = title
        dialogContent = content
        showDialog = true
    }

    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    val permission = Manifest.permission.READ_PHONE_STATE

    // Check if the permission is granted
    val hasPermission =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

// State to hold the device network type
    var deviceType by remember { mutableStateOf("Unknown") }

// Launcher for requesting permission
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            deviceType = when (telephonyManager.dataNetworkType) { // Updated method
                TelephonyManager.NETWORK_TYPE_GSM -> "GSM"
                TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
                TelephonyManager.NETWORK_TYPE_LTE -> "4G"
                TelephonyManager.NETWORK_TYPE_NR -> "5G"
                else -> "Unknown"
            }
        } else {
            deviceType = "Permission Denied"
        }
    }

// If permission is granted, fetch the network type directly
    if (hasPermission) {
        deviceType = when (telephonyManager.dataNetworkType) { // Updated method
            TelephonyManager.NETWORK_TYPE_GSM -> "GSM"
            TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
            TelephonyManager.NETWORK_TYPE_LTE -> "4G"
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            else -> "Unknown"
        }
    }


// Fetch device information
    val googleServicesFrameworkId = Settings.Secure.getString(context.contentResolver, "android_id")

    fun getUniqueIdentifier(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        var uniqueId = sharedPreferences.getString("unique_id", null)
        if (uniqueId == null) {
            uniqueId = UUID.randomUUID().toString()
            sharedPreferences.edit().putString("unique_id", uniqueId).apply()
        }
        return uniqueId
    }

    val buildFingerprint = Build.FINGERPRINT

    val networkOperator =
        (context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager)?.networkOperatorName

    val networkType = getNetworkType(context)

// Bluetooth MAC Address
    val bluetoothMacAddress = try {
        Settings.Secure.getString(context.contentResolver, "bluetooth_address") ?: "Unavailable"
    } catch (e: Exception) {
        "Unavailable"
    }

    val usbDebugging =
        Settings.Secure.getInt(context.contentResolver, Settings.Secure.ADB_ENABLED, 0) == 1


    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK")
                }
            },
            title = { Text(dialogTitle, style = MaterialTheme.typography.headlineSmall) },
            text = { Text(dialogContent, style = MaterialTheme.typography.bodyMedium) }
        )
    }


    var googleAdvertisingId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch the Google Advertising ID on composition
    LaunchedEffect(context) {
        withContext(Dispatchers.IO) {
            val advertisingId = getGoogleAdvertisingId(context)
            withContext(Dispatchers.Main) {
                googleAdvertisingId = advertisingId
                isLoading = false
            }
        }
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize(), // Optional padding to avoid content overlapping with navigation bar
        verticalArrangement = Arrangement.Top
    ) {
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 13.dp),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.onPrimary)
                        .clickable {
                            val intent = Intent(Settings.ACTION_DEVICE_INFO_SETTINGS)
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            }
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.cellphone),
                        contentDescription = "Phone Icon",
                        modifier = Modifier.size(30.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = brand,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = markaziTextFont,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = deviceName,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = markaziTextFont,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
        item {
            DeviceDetailRow(
                label = "Device Name",
                value = deviceName,
                onClick = {
                    showDetails(
                        "Device Name",
                        "This is the name of your device model, as assigned by the manufacturer. It helps in identifying your specific device."
                    )
                }
            )
        }
        item {
            DeviceDetailRow(
                label = "Model",
                value = deviceName,
                onClick = {
                    showDetails(
                        "Model",
                        "The model name represents the specific version or variation of the device hardware."
                    )
                }
            )
        }
        item {
            DeviceDetailRow(
                label = "Manufacturer",
                value = manufacturer,
                onClick = {
                    showDetails(
                        "Manufacturer",
                        "The manufacturer is the company that built your device. It indicates the brand responsible for the hardware."
                    )
                }
            )
        }
        item {
            DeviceDetailRow(
                label = "Device",
                value = device,
                onClick = {
                    showDetails(
                        "Device",
                        "The device field uniquely identifies the device in terms of its underlying hardware and software compatibility."
                    )
                }
            )
        }
        item {
            DeviceDetailRow(
                label = "Board",
                value = board,
                onClick = {
                    showDetails(
                        "Board",
                        "This refers to the motherboard or the main printed circuit board in your device that houses key components."
                    )
                }
            )
        }
        item {
            DeviceDetailRow(
                label = "Hardware",
                value = hardware,
                onClick = {
                    showDetails(
                        "Hardware",
                        "Hardware indicates the specific hardware configuration or chipset details of your device."
                    )
                }
            )
        }
        item {
            DeviceDetailRow(
                label = "Brand",
                value = brand,
                isLastItem = true,
                onClick = {
                    showDetails(
                        "Brand",
                        "The brand represents the overarching name under which the device is marketed and sold."
                    )
                }
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }


        item {
            ConfigurableDeviceDetailRow(
                label = "Android Device ID",
                value = getUniqueIdentifier(context),
                isTop = true,
                onClick = {
                    showDetails(
                        "Android Device ID",
                        "The Android Device ID is a unique string that identifies your device within the Android ecosystem."
                    )
                }
            )
        }

        item {
            ConfigurableDeviceDetailRow(
                label = "Google Services Framework ID",
                value = googleServicesFrameworkId,
                onClick = {
                    showDetails(
                        "Google Services Framework ID",
                        "This ID is a unique identifier used by Google services to identify your device for synchronization and other purposes."
                    )
                }
            )
        }

        item {
            ConfigurableDeviceDetailRow(
                label = "Google Advertising ID",
                value = googleAdvertisingId ?: "Not Available",
                onClick = {
                    showDetails(
                        "Google Advertising ID",
                        "This ID is a unique, user-resettable identifier used for advertising purposes. It allows advertisers to track user interactions."
                    )
                }
            )
        }

        item {
            ConfigurableDeviceDetailRow(
                label = "Build Fingerprint",
                value = buildFingerprint,
                onClick = {
                    showDetails(
                        "Build Fingerprint",
                        "The build fingerprint uniquely identifies the software build of your device, including OS version, manufacturer, and other details."
                    )
                }
            )
        }

        // USB Host Support
        item {
            ConfigurableDeviceDetailRow(
                label = "USB Host Support",
                value = if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST)) "Supported" else "Not Supported",
                onClick = {
                    showDetails(
                        "USB Host Support",
                        "USB Host Support allows your device to connect to external USB devices such as keyboards, mice, and storage drives. This feature is dependent on both hardware and software capabilities."
                    )
                }
            )
        }

// TimeZone
        item {
            val timeZone = java.util.TimeZone.getDefault().id
            ConfigurableDeviceDetailRow(
                label = "Time Zone",
                value = timeZone,
                onClick = {
                    showDetails(
                        "Time Zone",
                        "The Time Zone represents the region and time standard your device is configured to follow. It is used to display the correct local time."
                    )
                }
            )
        }

        item {
            ConfigurableDeviceDetailRow(
                label = "Device Type",
                value = deviceType,
                onClick = {
                    showDetails(
                        "Device Type",
                        "Indicates whether the device is categorized as a Phone or Tablet based on its screen size."
                    )
                }
            )
        }
        item {
            ConfigurableDeviceDetailRow(
                label = "Network Operator",
                value = networkOperator ?: "Unknown",
                onClick = {
                    showDetails(
                        "Network Operator",
                        "The network operator represents the cellular provider to which your device is currently connected."
                    )
                }
            )
        }
        item {
            ConfigurableDeviceDetailRow(
                label = "Network Type",
                value = networkType,
                onClick = {
                    showDetails(
                        "Network Type",
                        "The network type describes the type of cellular network your device is connected to, such as 4G or 5G."
                    )
                }
            )
        }
        item {
            ConfigurableDeviceDetailRow(
                label = "WiFi MAC Address",
                value = wifiMacAddress(),
                onClick = {
                    showDetails(
                        "WiFi MAC Address",
                        "The WiFi MAC address is a unique hardware address that identifies your device on a WiFi network."
                    )
                }
            )
        }
        item {
            ConfigurableDeviceDetailRow(
                label = "Bluetooth MAC Address",
                value = bluetoothMacAddress,
                onClick = {
                    showDetails(
                        "Bluetooth MAC Address",
                        "The Bluetooth MAC address uniquely identifies your device when using Bluetooth communication."
                    )
                }
            )
        }
        item {
            ConfigurableDeviceDetailRow(
                label = "USB Debugging",
                value = if (usbDebugging) "Enabled" else "Disabled",
                isBottom = true,
                onClick = {
                    showDetails(
                        "USB Debugging",
                        "USB Debugging allows your device to communicate with a computer for debugging and development purposes."
                    )
                }
            )
        }
    }
}


fun getNetworkType(context: Context): String {
    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

    // Check if the READ_PHONE_STATE permission is granted
    return if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        when (telephonyManager?.networkType) {
            TelephonyManager.NETWORK_TYPE_LTE -> "4G"
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            TelephonyManager.NETWORK_TYPE_HSPA -> "3G"
            TelephonyManager.NETWORK_TYPE_EDGE -> "2G"
            else -> "Unknown"
        }
    } else {
        "Permission Denied"
    }
}

@Composable
fun DeviceDetailRow(
    label: String,
    value: String?,
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp, horizontal = 13.dp)
            .clip(
                when {
                    isFirstItem -> RoundedCornerShape(0.dp)
                    isLastItem -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    else -> RoundedCornerShape(0.dp)
                }
            )
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick?.invoke() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
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
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = value ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
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
        else -> RoundedCornerShape(0.dp) // No rounding for intermediate items
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp, horizontal = 13.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick?.invoke() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
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
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = value ?: "",
                    style = if (value == "Permission Denied") {
                        MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    } else {
                        MaterialTheme.typography.bodyMedium
                    },
                    color = if (value == "Permission Denied") {
                        MaterialTheme.colorScheme.onError
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

            }
        }
    }
    if (isBottom) {
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun wifiMacAddress(): String {
    val context = LocalContext.current
    val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val permission = Manifest.permission.ACCESS_FINE_LOCATION

    // Check if the permission is granted
    val hasPermission =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    // State to hold the WiFi MAC address
    var wifiMacAddress = "Unavailable"

    // Launcher for requesting permission
    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted, retrieve Wi-Fi MAC Address
                wifiMacAddress = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    wifiManager.connectionInfo?.macAddress ?: "Unavailable"
                } else {
                    "Unavailable"
                }
            } else {
                // Handle the case when permission is not granted
                wifiMacAddress = "Permission Denied"
            }
        }

    // If permission is not granted, request permission
    LaunchedEffect(key1 = hasPermission) {
        if (!hasPermission) {
            requestPermissionLauncher.launch(permission)
        } else {
            // Permission already granted, retrieve Wi-Fi MAC Address
            wifiMacAddress = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                wifiManager.connectionInfo?.macAddress ?: "Unavailable"
            } else {
                "Unavailable"
            }
        }
    }
    return wifiMacAddress
}


fun getGoogleAdvertisingId(context: Context): String? {
    return try {
        val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
        adInfo.id // Returns the Advertising ID
    } catch (e: Exception) {
        null // If any error occurs, return null
    }
}
