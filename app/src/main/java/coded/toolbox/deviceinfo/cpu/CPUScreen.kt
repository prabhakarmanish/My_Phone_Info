package coded.toolbox.deviceinfo.cpu

import android.os.Build
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coded.toolbox.deviceinfo.AdBannerManager
import coded.toolbox.deviceinfo.R
import coded.toolbox.deviceinfo.markaziTextFont
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.Locale

@Composable
fun CPUScreen(navController: NavController, adBannerManager: AdBannerManager) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        item { CPUCard() }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        // Dynamic details
        listOf(
            "Processor" to getProcessorInfo(),
            "CPU Cores" to Runtime.getRuntime().availableProcessors().toString(),
            "Architecture" to getCpuFrequenciesGrouped(),
            "CPU Frequency" to getCpuFrequencyRangesGrouped(),
            "Supported ABIs" to getSupportedABIs(),
            "Processor Name" to getManufacrurerDetails(),
            "Fabrication Process" to getFabricationProcess(),
            "CPU Hardware" to getChipsetName(),
            "CPU Governor" to getCpuGovernor(),
            "Java Heap" to getJavaHeapSize(),

            ).forEachIndexed { index, (label, value) ->
            item {
                CPUDetail(
                    label = label,
                    value = value,
                    isTop = index == 0,
                    isBottom = index == 15
                )
            }
        }

        item {
            Text(
                text = "/proc/cpuinfo",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = markaziTextFont
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(6.dp)
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { /* Handle click */ }
            ) {
                Text(
                    text = getProcessorInfoname(),
                    modifier = Modifier.padding(6.dp)
                )
            }
        }
    }
}

@Composable
fun CPUDetail(
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
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
                    text = value ?: "Unknown",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
    if (isBottom) {
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun CPUCard() {
    val chipName = getChipName()
    val cores = getCpuCores()
    val chipSize = getChipSize()
    val imageRes = when (chipName) {
        "Snapdragon" -> R.drawable.snapdragon
        "Exynos" -> R.drawable.exynos
        "MediaTek" -> R.drawable.mediatek
        "Kirin" -> R.drawable.kirin
        "Spreadtrum" -> R.drawable.spreadtrum
        "Allwinner" -> R.drawable.allwinner
        else -> R.drawable.memory
    }

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
                modifier = Modifier
                    .size(90.dp)
                    .padding(vertical = 6.dp),
                contentDescription = "Chip Icon"
            )
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .align(alignment = Alignment.CenterVertically),
            ) {
                Text(
                    text = "${chipName.uppercase(Locale.getDefault())} ${
                        getProcessorName().uppercase(
                            Locale.ROOT
                        )
                    } ${getModelDetails().uppercase(Locale.ROOT)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Codename: ${getExactChipset().uppercase(Locale.getDefault())}",
                    maxLines = 1,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Manufacturer: ${getManufacrurerDetails().uppercase(Locale.getDefault())}",
                    maxLines = 1,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Model: ${getProcessorNameUsingSystemProperties().uppercase(Locale.getDefault())}",
                    maxLines = 1,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

fun getProcessorInfo(): String {
    val hardware = Build.HARDWARE ?: "Unknown"
    val bitness = when {
        Build.SUPPORTED_ABIS.contains("arm64-v8a") -> "64-bit"
        Build.SUPPORTED_ABIS.contains("armeabi-v7a") -> "32-bit"
        else -> "Unknown Bitness"
    }
    return "$hardware ($bitness)"
}

fun getCpuFrequenciesGrouped(): String {
    val cpuDir = "/sys/devices/system/cpu/"
    val frequencyCounts = mutableMapOf<String, Int>()

    try {
        // Iterate over CPU cores (cpu0, cpu1, etc.)
        File(cpuDir).listFiles { file -> file.name.startsWith("cpu") && file.name.matches(Regex("cpu[0-9]+")) }
            ?.forEach { cpuCore ->
                val maxFreqPath = File(cpuCore, "cpufreq/cpuinfo_max_freq")

                if (maxFreqPath.exists()) {
                    val maxFreq = maxFreqPath.readText().trim().toLongOrNull()
                        ?.let { it / 1_000_000.0 } // Convert to GHz
                    maxFreq?.let {
                        val formattedFreq = "${it.format(2)} GHz"
                        frequencyCounts[formattedFreq] =
                            frequencyCounts.getOrDefault(formattedFreq, 0) + 1
                    }
                }
            }
    } catch (e: Exception) {
        return "Error retrieving CPU frequencies"
    }

    return if (frequencyCounts.isNotEmpty()) {
        frequencyCounts.entries.joinToString("\n") {
            "${it.value} x ${it.key}"
        }
    } else {
        "Unknown Frequencies"
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)

fun getFabricationProcess(): String {
    // This is typically specific to the chip manufacturer (e.g., 7nm, 5nm)
    return "7nm" // Example, replace with actual retrieval logic if possible
}

fun getSupportedABIs(): String {
    return Build.SUPPORTED_ABIS.joinToString(", ")
}

fun getCpuGovernor(): String {
    val cpuDir = "/sys/devices/system/cpu/"
    val governors = mutableSetOf<String>()

    try {
        File(cpuDir).listFiles { file -> file.name.startsWith("cpu") && file.name.matches(Regex("cpu[0-9]+")) }
            ?.forEach { cpuCore ->
                val governorPath = File(cpuCore, "cpufreq/scaling_governor")
                if (governorPath.exists()) {
                    val governor = governorPath.readText().trim()
                    if (governor.isNotEmpty()) {
                        governors.add(governor)
                    }
                }
            }
    } catch (e: Exception) {
        return "Error retrieving CPU governor"
    }

    return if (governors.isNotEmpty()) {
        governors.joinToString(", ") // Combine multiple governors if different for each core
    } else {
        "Unknown Governor"
    }
}

fun getCpuFrequencyRangesGrouped(): String {
    val cpuDir = "/sys/devices/system/cpu/"
    val frequencyRanges = mutableMapOf<String, Int>()

    try {
        // Iterate over CPU cores (cpu0, cpu1, etc.)
        File(cpuDir).listFiles { file -> file.name.startsWith("cpu") && file.name.matches(Regex("cpu[0-9]+")) }
            ?.forEach { cpuCore ->
                val minFreqPath = File(cpuCore, "cpufreq/cpuinfo_min_freq")
                val maxFreqPath = File(cpuCore, "cpufreq/cpuinfo_max_freq")

                if (minFreqPath.exists() && maxFreqPath.exists()) {
                    val minFreq = minFreqPath.readText().trim().toLongOrNull()
                        ?.let { it / 1_000.0 } // Convert to MHz
                    val maxFreq = maxFreqPath.readText().trim().toLongOrNull()
                        ?.let { it / 1_000.0 } // Convert to MHz

                    if (minFreq != null && maxFreq != null) {
                        val range = "${minFreq.format(0)} MHz - ${maxFreq.format(0)} MHz"
                        frequencyRanges[range] = frequencyRanges.getOrDefault(range, 0) + 1
                    }
                }
            }
    } catch (e: Exception) {
        return "Error retrieving CPU frequency ranges"
    }

    return if (frequencyRanges.isNotEmpty()) {
        frequencyRanges.entries.joinToString("\n") {
            "${it.value} x ${it.key}"
        }
    } else {
        "Unknown Frequency Ranges"
    }
}

fun getProcessorInfoname(): String {
    val cpuInfo = try {
        val reader = BufferedReader(FileReader("/proc/cpuinfo"))
        val cpuInfoString = reader.readText()
        reader.close()
        cpuInfoString
    } catch (e: Exception) {
        "Unknown"
    }

    return cpuInfo
}

fun getChipsetName(): String {
    return try {
        // Attempt to fetch from system properties
        val systemPropertiesClass = Class.forName("android.os.SystemProperties")
        val getMethod = systemPropertiesClass.getMethod("get", String::class.java)

        val chipName = getMethod.invoke(null, "ro.chipname") as? String
            ?: getMethod.invoke(null, "ro.board.platform") as? String
            ?: parseCpuInfoForChipset()
            ?: "Unknown"

        chipName.ifEmpty { "Unknown" }
    } catch (e: Exception) {
        "Error retrieving chipset name"
    }
}

fun parseCpuInfoForChipset(): String? {
    try {
        val cpuInfoFile = File("/proc/cpuinfo")
        if (cpuInfoFile.exists()) {
            val cpuInfo = cpuInfoFile.readText()
            val hardwareLine = cpuInfo.lines().find { it.startsWith("Hardware") }
            return hardwareLine?.split(":")?.getOrNull(1)?.trim()
        }
    } catch (e: Exception) {
        // Ignore parsing errors
    }
    return null
}

fun getManufacrurerDetails(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Build.SOC_MANUFACTURER
    } else {
        "Unknown"
    }
}

fun getModelDetails(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Build.SOC_MODEL
    } else {
        "Unknown"
    }
}

fun getChipsetFromSystemProperties(): String {
    return try {
        val systemPropertiesClass = Class.forName("android.os.SystemProperties")
        val getMethod = systemPropertiesClass.getMethod("get", String::class.java)
        val chipset = getMethod.invoke(null, "ro.board.platform") as String
        chipset.ifEmpty { "Unknown" }
    } catch (e: Exception) {
        "Unknown"
    }
}

fun getChipsetFromCpuInfo(): String {
    return try {
        val cpuInfo = File("/proc/cpuinfo").readText()
        val regex = Regex("Hardware\\s+:\\s+(.*)")
        val matchResult = regex.find(cpuInfo)
        matchResult?.groupValues?.get(1)?.trim() ?: "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }
}

fun getExactChipset(): String {
    val chipset = getChipsetFromSystemProperties()
    if (chipset != "Unknown") return chipset

    val cpuInfoChipset = getChipsetFromCpuInfo()
    return if (cpuInfoChipset != "Unknown") cpuInfoChipset else "Unknown Chipset"
}

fun getChipName(): String {
    return when {
        Build.MANUFACTURER.contains("Qualcomm", ignoreCase = true) -> "Snapdragon"
        Build.MANUFACTURER.contains("Samsung", ignoreCase = true) && Build.MODEL.contains(
            "Exynos",
            ignoreCase = true
        ) -> "Exynos"

        Build.MANUFACTURER.contains("MediaTek", ignoreCase = true) -> "MediaTek"
        Build.MANUFACTURER.contains("Huawei", ignoreCase = true) && Build.MODEL.contains(
            "Kirin",
            ignoreCase = true
        ) -> "Kirin"

        Build.MANUFACTURER.contains("Spreadtrum", ignoreCase = true) -> "Spreadtrum"
        Build.MANUFACTURER.contains("Allwinner", ignoreCase = true) -> "Allwinner"
        else -> {
            val hardware = Build.HARDWARE.lowercase(Locale.ROOT)
            when {
                hardware.contains("qcom") -> "Snapdragon"
                hardware.contains("exynos") -> "Exynos"
                hardware.contains("mtk") -> "MediaTek"
                hardware.contains("kirin") -> "Kirin"
                else -> "Unknown"
            }
        }
    }
}

fun getProcessorName(): String {
    return try {
        // Use Build.HARDWARE if available
        Build.HARDWARE.ifEmpty {
            val fileReader = FileReader("/proc/cpuinfo")
            val bufferedReader = BufferedReader(fileReader)
            var line: String?

            // Read through each line of cpuinfo
            while (bufferedReader.readLine().also { line = it } != null) {
                // Check for "model name" or "Processor" in the line
                if (line!!.contains("model name", ignoreCase = true) || line!!.contains(
                        "Processor",
                        ignoreCase = true
                    )
                ) {
                    bufferedReader.close()
                    // Split the line at ':' and return the second part (after the colon)
                    return line!!.split(":").getOrNull(1)?.trim() ?: "Unknown"
                }
            }
            bufferedReader.close()
            "Unknown"
        }
    } catch (e: IOException) {
        "Unknown"
    }
}

fun getProcessorNameUsingSystemProperties(): String {
    return try {
        val systemPropertiesClass = Class.forName("android.os.SystemProperties")
        val getMethod = systemPropertiesClass.getMethod("get", String::class.java)
        val processorName = getMethod.invoke(null, "ro.product.board") as String
        if (processorName.isNotEmpty()) processorName else "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }
}

fun getCpuCores(): String {
    // Fetch CPU core count dynamically (can also use the `/proc/cpuinfo` file)
    return Runtime.getRuntime().availableProcessors().toString()
}

fun getChipSize(): String {
    // Typically, chip size (nm) is not available via Android APIs, but you can set a fixed value
    // for popular chips or use manufacturer documentation.
    return when {
        Build.MANUFACTURER.contains("Qualcomm", ignoreCase = true) -> "7nm"
        Build.MANUFACTURER.contains("Samsung", ignoreCase = true) && Build.MODEL.contains(
            "Exynos",
            ignoreCase = true
        ) -> "5nm"

        Build.MANUFACTURER.contains("MediaTek", ignoreCase = true) -> "7nm"
        else -> "Unknown"
    }
}

fun getJavaHeapSize(): String {
    val runtime = Runtime.getRuntime()
    val maxHeapSize = runtime.maxMemory() / (1024 * 1024)  // Convert to MB
    val totalHeapSize = runtime.totalMemory() / (1024 * 1024)  // Convert to MB
    val freeHeapSize = runtime.freeMemory() / (1024 * 1024)  // Convert to MB

    return "Max Heap Size: $maxHeapSize MB\n" +
            "Total Heap Size: $totalHeapSize MB\n" +
            "Free Heap Size: $freeHeapSize MB"
}

