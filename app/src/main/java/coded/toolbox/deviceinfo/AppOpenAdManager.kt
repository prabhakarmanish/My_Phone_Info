package coded.toolbox.deviceinfo

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

const val Ad_ID = "ca-app-pub-4154300661292859/3170385017"
private const val TAG = "AppOpenAdManager"

class AppOpenAdManager {
    private var loadTime: Long = 0
    var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd = false

    fun loadAd(context: Context) {
        if (isLoadingAd || isAdAvailable()) {
            return
        }
        isLoadingAd = true

        var request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            Ad_ID,
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    super.onAdLoaded(ad)
                    Log.i(TAG, "Ad loaded")
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = System.currentTimeMillis()
                }

                override fun onAdFailedToLoad(ad: LoadAdError) {
                    super.onAdFailedToLoad(ad)
                    Log.i(TAG, "Ad failed to load")
                    isLoadingAd = false
                }
            }
        )
    }

    private fun wasLodTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return (dateDifference < (numMilliSecondsPerHour * numHours))
    }

    private fun isAdAvailable(): Boolean {
        return (appOpenAd != null && wasLodTimeLessThanNHoursAgo(4))
    }

    fun showAdIfAvailable(activity: MainActivity) {
        if (isShowingAd) {
            Log.i(TAG, "The app open ad is already showing.")
            return
        }

        if (!isAdAvailable()) {
            Log.i(TAG, "The app open ad is not ready yet.")
            loadAd(activity)
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.i(TAG, "Ad dismissed")
                appOpenAd = null
                isShowingAd = false
                loadAd(activity)
            }

            override fun onAdFailedToShowFullScreenContent(ad: AdError) {
                Log.i(TAG, ad.message)
                appOpenAd = null
                isShowingAd = false
                loadAd(activity)
            }

            override fun onAdShowedFullScreenContent() {
                Log.i(TAG, "Ad showed")
                isShowingAd = true
            }
        }
        isShowingAd = true
        appOpenAd?.show(activity)
    }

}