package coded.toolbox.deviceinfo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coded.toolbox.deviceinfo.camera.CameraScreen
import coded.toolbox.deviceinfo.dashboard.DashboardScreen
import coded.toolbox.deviceinfo.thermal.ThermalScreen
import kotlinx.coroutines.launch
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val categories = listOf(
        "DASHBOARD", "SYSTEM", "CPU", "BATTERY", "DISPLAY", "MEMORY", "Camera",
        "THERMAL", "SENSORS", "CODECS", "NETWORK", "INPUT DEVICES", "APPS", "TESTS"
    )

    // Track the selected category both for the pager and chip
    val selectedCategory = remember { mutableStateOf(categories[0]) }
    val pagerState = rememberPagerState(pageCount = { categories.size })
    val coroutineScope = rememberCoroutineScope()

    // Synchronize selected category with the pager
    LaunchedEffect(pagerState.currentPage) {
        selectedCategory.value = categories[pagerState.currentPage]
    }

    // LazyListState to control the chip scrolling
    val listState = rememberLazyListState()

    val formattedCategory = when (selectedCategory.value) {
        "DASHBOARD" -> "Device Info"
        "SYSTEM" -> "System Info"
        "CPU" -> "CPU Info"
        "BATTERY" -> "Battery Info"
        "DISPLAY" -> "Display Info"
        "MEMORY" -> "Memory Info"
        "CAMERA" -> "Camera Info"
        "THERMAL" -> "Thermal Info"
        "SENSORS" -> "Sensors Info"
        "CODECS" -> "Codecs Info"
        "NETWORK" -> "Network Info"
        "INPUT DEVICES" -> "Input Devices Info"
        "APPS" -> "Apps Info"
        "TESTS" -> "Tests Info"
        else -> selectedCategory.value.lowercase(Locale.ROOT)
            .replaceFirstChar { it.uppercase(Locale.ROOT) } + " Info"
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = formattedCategory,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                // Scrollable Chips (Always at the top)
                LazyRow(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    items(categories) { category ->
                        val index = categories.indexOf(category)
                        Chip(
                            label = category,
                            isSelected = selectedCategory.value == category,
                            onClick = {
                                coroutineScope.launch {
                                    selectedCategory.value = category
                                    pagerState.scrollToPage(index)

                                    // Ensure the chip is fully visible in the viewport
                                    listState.layoutInfo.visibleItemsInfo.find { it.index == index }
                                        ?.let { visibleItem ->
                                            if (visibleItem.offset < 0 || visibleItem.offset + visibleItem.size > listState.layoutInfo.viewportEndOffset) {
                                                listState.animateScrollToItem(index)
                                            }
                                        } ?: run {
                                        // If not found in visible items, forcefully scroll to the chip
                                        listState.animateScrollToItem(index)
                                    }
                                }
                            }
                        )
                    }
                }

                // HorizontalPager (This is below the chips and independent)
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize(),
                    userScrollEnabled = true // Enable user scroll for manual swipe
                ) { page ->
                    when (page) {
                        0 -> DashboardScreen(navController)
                        1 -> SystemScreen(navController)
                        2 -> CPUScreen(navController)
                        3 -> BatteryScreen(navController)
                        4 -> DisplayScreen(navController)
                        5 -> MemoryScreen(navController)
                        6 -> CameraScreen(navController, context)
                        7 -> ThermalScreen(navController,context)
                        8 -> SensorsScreen(navController)
                        9 -> CodecsScreen(navController)
                        10 -> NetworkScreen(navController)
                        11 -> InputDevicesScreen(navController)
                        12 -> AppsScreen(navController)
                        13 -> TestsScreen(navController)
                        else -> PlaceholderScreen(categories[page])
                    }

                    // Center the chip when the page changes
                    LaunchedEffect(pagerState.currentPage) {
                        coroutineScope.launch {
                            val currentIndex = pagerState.currentPage
                            listState.layoutInfo.visibleItemsInfo.find { it.index == currentIndex }
                                ?.let { visibleItem ->
                                    if (visibleItem.offset < 0 || visibleItem.offset + visibleItem.size > listState.layoutInfo.viewportEndOffset) {
                                        listState.animateScrollToItem(currentIndex)
                                    }
                                } ?: run {
                                // If not found in visible items, forcefully scroll to the chip
                                listState.animateScrollToItem(currentIndex)
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun Chip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        border = if (isSelected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        } else {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    ) {
        // Using a Box to align the text in the center of the Card
        Box(
            modifier = Modifier
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ), // This ensures the Box fills the available space in the Card
            contentAlignment = Alignment.Center // This will center the text inside the Box
        ) {
            Text(
                text = label,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium.copy(
                    letterSpacing = 1.3.sp // Adjust the value as needed for desired spacing
                ),
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal // Bold for selected
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewHomeScreen() {
    HomeScreen(navController = rememberNavController())
}
