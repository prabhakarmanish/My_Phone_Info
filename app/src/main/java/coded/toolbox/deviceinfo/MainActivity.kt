package coded.toolbox.deviceinfo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coded.toolbox.deviceinfo.ui.theme.DeviceInfoTheme
val markaziTextFont = FontFamily(Font(R.font.markazitext))
val serif = FontFamily(Font(R.font.seriftextregular))

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeviceInfoTheme(darkTheme = isSystemInDarkTheme()) {
                val navController = rememberNavController()
                DeviceInfoApp(navController)
            }
        }
    }
}

@Composable
fun DeviceInfoApp(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("settings") {
            SettingsScreen(navController = navController)
        }
    }
}
