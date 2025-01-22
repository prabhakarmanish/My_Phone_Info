package coded.toolbox.deviceinfo

import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdSize

@Composable
fun BannerAd(
    adBannerManager: AdBannerManager,
    adUnitId: String,
    adSize: AdSize,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Create a new ad view instance for this Composable
    val adView = remember {
        adBannerManager.createNewAdView(context, adUnitId, adSize)
    }

    // Clean up the ad view when this Composable is disposed
    DisposableEffect(adView) {
        onDispose {
            adBannerManager.destroyAdView(adView)
        }
    }

    // Display the ad view using AndroidView
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            FrameLayout(ctx).apply {
                addView(adView)
            }
        }
    )
}
