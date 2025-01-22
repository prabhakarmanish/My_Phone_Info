package coded.toolbox.deviceinfo

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coded.toolbox.deviceinfo.ui.theme.DeviceInfoTheme
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val markaziTextFont = FontFamily(Font(R.font.markazitext))
val serif = FontFamily(Font(R.font.seriftextregular))

class MainActivity : ComponentActivity()
{
    private lateinit var appOpenAdManager: AppOpenAdManager
    private lateinit var adBannerManager: AdBannerManager

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        MobileAds.initialize(this){}
        appOpenAdManager = AppOpenAdManager()
        adBannerManager = AdBannerManager()
        // Preload Medium Rectangle Ad
        adBannerManager.preloadAd(this, MEDIUM_RECTANGLE_AD_ID, AdSize.MEDIUM_RECTANGLE)
        // Preload Full Banner Ad
        adBannerManager.preloadAd(this, FULL_BANNER_AD_ID, AdSize.FULL_BANNER)

        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            MobileAds.initialize(this@MainActivity) {}
        }


        setContent {
            DeviceInfoTheme(darkTheme = isSystemInDarkTheme()) {
                val navController = rememberNavController()
                DeviceInfoApp(navController,adBannerManager)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        appOpenAdManager.loadAd(this)
    }
    override fun onRestart() {
        super.onRestart()
        appOpenAdManager.showAdIfAvailable(this)
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun DeviceInfoApp(navController: NavHostController, adBannerManager: AdBannerManager) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController,adBannerManager)
        }
        composable("settings") {
            SettingsScreen(navController = navController,adBannerManager)
        }
    }
}
