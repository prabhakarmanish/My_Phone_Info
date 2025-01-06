package coded.toolbox.deviceinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SystemScreen(navController: NavController) {
    ComingSoonScreen("System Screen")
}

@Composable
fun CPUScreen(navController: NavController) {
    ComingSoonScreen("CPU Screen")
}

@Composable
fun BatteryScreen(navController: NavController) {
    ComingSoonScreen("Battery Screen")
}

@Composable
fun DisplayScreen(navController: NavController) {
    ComingSoonScreen("Display Screen")
}

@Composable
fun MemoryScreen(navController: NavController) {
    ComingSoonScreen("Memory Screen")
}

@Composable
fun SensorsScreen(navController: NavController) {
    ComingSoonScreen("Sensors Screen")
}

@Composable
fun CodecsScreen(navController: NavController) {
    ComingSoonScreen("Codecs Screen")
}

@Composable
fun NetworkScreen(navController: NavController) {
    ComingSoonScreen("Network Screen")
}

@Composable
fun InputDevicesScreen(navController: NavController) {
    ComingSoonScreen("Input Devices Screen")
}

@Composable
fun AppsScreen(navController: NavController) {
    ComingSoonScreen("Apps Screen")
}

@Composable
fun TestsScreen(navController: NavController) {
    ComingSoonScreen("Tests Screen")
}

@Composable
fun ComingSoonScreen(screenTitle: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // "Coming Soon" Section with a vibrant, bold color
            Text(
                text = "Coming Soon",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .padding(bottom = 16.dp)
            )

            // Description Section with a lighter, subtler color
            Text(
                text = "A great feature is on the way, stay tuned for $screenTitle!",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center, // Center the overall text
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .align(Alignment.CenterHorizontally)
            )


            // Image from drawable (coming soon icon) with gradient background
            val image = painterResource(id = R.drawable.calendar) // Replace with your image name

            // Rounded Icon with custom background and padding
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.errorContainer,
                                MaterialTheme.colorScheme.inverseOnSurface
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = image,
                    contentDescription = "Coming Soon Icon",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(60.dp)
                )
            }

            // Optional: A subtle footer to close the UI with a professional look
            Text(
                text = "Stay Tuned!",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Light
                ),
                modifier = Modifier
                    .padding(top = 32.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}


@Composable
fun PlaceholderScreen(category: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Placeholder for $category",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}