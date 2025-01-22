package coded.toolbox.deviceinfo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define the light color scheme
private val LightColorScheme = lightColorScheme(
    primary = Color.White,
    onPrimary = Color(0xFF07305e),
    background = Color(0xFFF3F3F3),
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    tertiary = Color(0xff397ca6),
    secondary = Color(0xFF07305e),
    onSecondary = Color.White,
    error = Color(0xFF07305e),
    onError = Color.Red,
    errorContainer = Color(0xFF07305e),
    onErrorContainer = Color.White,
    outline = Color(0xffcccccc),
    outlineVariant = Color.LightGray.copy(alpha = 0.2f)
)
//0xFF134a69
// Define the dark color scheme
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF1E1F20),
    onPrimary = Color.White,
    background = Color(0xFF131314),
    onBackground = Color.White,
    surface = Color(0xFF1E1F20),
    onSurface = Color.White,
    tertiary = Color(0xffd5d4d4),
    onError = Color.Red,
    secondary = Color(0xb5b3afaf),
    onSecondary = Color.Black,
    outline = Color(0xff3c3c3c),
    outlineVariant = Color.LightGray
)

@Composable
fun DeviceInfoTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
