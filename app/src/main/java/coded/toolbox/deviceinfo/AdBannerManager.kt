package coded.toolbox.deviceinfo

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

private const val TAG = "AdBannerManager"

// Original Ad ID for Medium Rectangle ads
const val MEDIUM_RECTANGLE_AD_ID = "ca-app-pub-4154300661292859/3071416314"

// New Ad ID for Full Banner ads
const val FULL_BANNER_AD_ID = "ca-app-pub-4154300661292859/2506587333"

class AdBannerManager {

    private val adRequests = mutableMapOf<Pair<String, AdSize>, AdRequest?>()

    // Preload AdRequest for a specific AdSize and Ad ID
    fun preloadAd(context: Context, adUnitId: String, adSize: AdSize) {
        val key = Pair(adUnitId, adSize)
        if (!adRequests.containsKey(key)) {
            adRequests[key] = AdRequest.Builder().build()
            Log.i(TAG, "AdRequest preloaded successfully for AdUnitId: $adUnitId and size: $adSize")
        }
    }

    // Create a new AdView with the given AdSize and AdUnitId
    fun createNewAdView(context: Context, adUnitId: String, adSize: AdSize): AdView {
        val adView = AdView(context).apply {
            this.adUnitId = adUnitId
            this.setAdSize(adSize)

            adListener = object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "AdView failed to load: ${adError.message}")
                }

                override fun onAdLoaded() {
                    Log.i(TAG, "AdView loaded successfully for AdUnitId: $adUnitId and size: $adSize")
                }
            }
        }

        // Use preloaded AdRequest if available, or create a new one
        val key = Pair(adUnitId, adSize)
        val adRequest = adRequests[key] ?: AdRequest.Builder().build()
        adView.loadAd(adRequest)
        return adView
    }

    // Clean up resources for a given AdView
    fun destroyAdView(adView: AdView) {
        try {
            adView.destroy()
            Log.i(TAG, "AdView destroyed successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to destroy AdView: ${e.message}")
        }
    }
}
