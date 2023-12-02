package com.example.google






import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


class Home : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var tvCoordinates: TextView
    private lateinit var global:String
    private lateinit var nativeAdContainer: FrameLayout
    private var rewardedAd: RewardedAd? = null
    private var userCurrency = 0 // User's current in-app currency
    private lateinit var currencyTextView: TextView // TextView to display currency

    private var appOpenAd: AppOpenAd? = null
    private var loadCallback: AppOpenAd.AppOpenAdLoadCallback? = null

    private lateinit var appOpenManager: AppOpenManager
    private var isShowingAd = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        MobileAds.initialize(this) {}
        tvCoordinates = findViewById(R.id.tvCoordinates)
        val btnOpenMap: Button = findViewById(R.id.btnOpenMap)
        val btnShare: Button = findViewById(R.id.btnShare)
        nativeAdContainer = findViewById(R.id.nativeAdContainer)
        appOpenManager = AppOpenManager(this)

        loadNativeAd()
        loadRewardedAd()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                fetchAndUpdateCoordinates()
                handler.postDelayed(this, 15000) // 15 seconds
            }
        }, 0)

        btnOpenMap.setOnClickListener {
            openGoogleMaps()
            showRewardedAd()
        }

        btnShare.setOnClickListener {
            shareCoordinates()
//            showRewardedAd()
        }
    }

    private fun showRewardedAd() {
        rewardedAd?.show(this, OnUserEarnedRewardListener { rewardItem: RewardItem ->
            val rewardAmount = rewardItem.amount
            userCurrency += rewardAmount
            updateCurrencyDisplay(userCurrency)
        })
    }

    private fun updateCurrencyDisplay(userCurrency: Int) {
        currencyTextView.text = "Currency: $userCurrency"
    }


    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917", adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                super.onAdLoaded(ad)
               rewardedAd = ad
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                super.onAdFailedToLoad(adError)
            }
        })
    }

    private fun loadNativeAd() {
        val adOptions = NativeAdOptions.Builder().build()
       val adLoader = AdLoader.Builder(this, "ca-app-pub-3940256099942544/2247696110")
            .forNativeAd { nativeAd: NativeAd ->
                val adView = layoutInflater.inflate(R.layout.nativeadd, null) as NativeAdView
                populateNativeAdView(nativeAd, adView)
                showNativeAd(adView)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {

                }
            })
            .withNativeAdOptions(adOptions)
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {

        adView.headlineView = adView.findViewById(R.id.ad_headline)
        (adView.headlineView as TextView).text = nativeAd.headline

        adView.setNativeAd(nativeAd)
    }

    private fun showNativeAd(adView: View) {
        nativeAdContainer.removeAllViews()
        nativeAdContainer.addView(adView)
    }


    private fun fetchAndUpdateCoordinates() {
        if (hasLocationPermission()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val coordinates = "Lat: ${location.latitude}, Long: ${location.longitude}"
                    tvCoordinates.text = "Current Coordinates: $coordinates"
                }
            }
        } else {

            requestLocationPermission()
        }
    }

    private fun openGoogleMaps() {
        if (hasLocationPermission()) {
            val coordinates = tvCoordinates.text.toString()

            // Extract latitude and longitude using regular expressions
            val latRegex = Regex("Lat: ([-+]?[0-9]*\\.?[0-9]+)")
            val lonRegex = Regex("Long: ([-+]?[0-9]*\\.?[0-9]+)")

            val latMatch = latRegex.find(coordinates)
            val lonMatch = lonRegex.find(coordinates)

            val lat = latMatch?.groupValues?.getOrNull(1)
            val lon = lonMatch?.groupValues?.getOrNull(1)

            if (!lat.isNullOrBlank() && !lon.isNullOrBlank()) {
                global = "https://www.google.com/maps?q=$lat,$lon"
                val gmmIntentUri = Uri.parse("geo:$lat,$lon")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")

                if (mapIntent.resolveActivity(packageManager) != null) {
                    startActivity(mapIntent)
                }
             else {
                    val mapWebUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lon")
                    val mapWebIntent = Intent(Intent.ACTION_VIEW, mapWebUri)
                    startActivity(mapWebIntent)
                }
            }
        } else {
            requestLocationPermission()
        }
    }
    private fun shareCoordinates() {
        val coordinates = tvCoordinates.text.toString()
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out my location: $global")
        shareIntent.`package` = "com.whatsapp" // Specify WhatsApp package
        startActivity(Intent.createChooser(shareIntent, "Share using"))
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        val REQUEST_LOCATION_PERMISSIONS = 1
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQUEST_LOCATION_PERMISSIONS
        )
    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }
    override fun onStart() {
        super.onStart()
        showAppOpenAdIfAvailable()
    }

    private fun showAppOpenAdIfAvailable() {
        if (!isShowingAd && isAdAvailable()) {
            loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    super.onAdLoaded(ad)
                    appOpenAd = ad
                    showAdIfAvailable()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                }
            }
            AppOpenAd.load(
                this,
                "ca-app-pub-3940256099942544/9257395921",
                AdRequest.Builder().build(),
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                loadCallback!!
            )
        }
    }

    private fun showAdIfAvailable() {
        if (appOpenAd != null) {
            isShowingAd = true
            appOpenAd!!.show(this)
        } else {
            isShowingAd = false

        }
    }

    private fun isAdAvailable(): Boolean {
        return loadCallback != null
    }

}
