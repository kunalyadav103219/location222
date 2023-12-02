package com.example.google

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class Splash : AppCompatActivity() {
    private lateinit var authManager: AuthManager
    private var mInterstitialAd: InterstitialAd? = null
    private final var TAG = "MainActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        MobileAds.initialize(this){
            loadInterstitialAd()
        }



        authManager = AuthManager(this)

        Handler().postDelayed({
           if(authManager.isLoggedIn)
           {
               val intent=Intent(this,Home::class.java)
               startActivity(intent)
           }
            else
           {
               val intent=Intent(this,login::class.java)
               startActivity(intent)
           }
        }, SPLASH_TIME_OUT)
    }

    private fun loadInterstitialAd() {
        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this,"ca-app-pub-8891265325254642/8364203034", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
              showtoast("failed to load ads")
                mInterstitialAd = null
            }


            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd

                mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        super.onAdFailedToShowFullScreenContent(adError)
                        showtoast("Failed to show interstitial ad: ${adError.message}")
                    }

                    override fun onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent()
                        // Ad showed successfully, you can perform any actions after ad display
                    }

                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        // Ad dismissed, you can navigate to the next screen or perform any post-ad actions
                    }
                }

                mInterstitialAd?.show(this@Splash)
            }
        })
    }

    private fun showtoast(s: String) {
      Toast.makeText(this,s,Toast.LENGTH_LONG).show()
    }


    companion object {
            private const val SPLASH_TIME_OUT = 5000L // 5 seconds
        }
    }
