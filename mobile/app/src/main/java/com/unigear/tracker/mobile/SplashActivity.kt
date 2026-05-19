package com.unigear.tracker.mobile

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val splashDurationMs = 3000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val brandingView = findViewById<ImageView>(R.id.ivBranding)
        val glowView = findViewById<View>(R.id.vLogoGlow)

        // Subtle fade pulse on the branding image.
        val alphaPulse = ObjectAnimator.ofFloat(brandingView, "alpha", 0.78f, 1f)
        alphaPulse.duration = 950
        alphaPulse.repeatCount = ObjectAnimator.INFINITE
        alphaPulse.repeatMode = ObjectAnimator.REVERSE
        alphaPulse.interpolator = AccelerateDecelerateInterpolator()

        // Gentle scale pulse for a glow-like breathing effect.
        val scaleX = ObjectAnimator.ofFloat(brandingView, "scaleX", 1f, 1.05f)
        scaleX.duration = 1000
        scaleX.repeatCount = ObjectAnimator.INFINITE
        scaleX.repeatMode = ObjectAnimator.REVERSE

        val scaleY = ObjectAnimator.ofFloat(brandingView, "scaleY", 1f, 1.05f)
        scaleY.duration = 1000
        scaleY.repeatCount = ObjectAnimator.INFINITE
        scaleY.repeatMode = ObjectAnimator.REVERSE

        val glowAlpha = ObjectAnimator.ofFloat(glowView, "alpha", 0.55f, 1f)
        glowAlpha.duration = 1050
        glowAlpha.repeatCount = ObjectAnimator.INFINITE
        glowAlpha.repeatMode = ObjectAnimator.REVERSE
        glowAlpha.interpolator = AccelerateDecelerateInterpolator()

        val glowScaleX = ObjectAnimator.ofFloat(glowView, "scaleX", 0.96f, 1.08f)
        glowScaleX.duration = 1100
        glowScaleX.repeatCount = ObjectAnimator.INFINITE
        glowScaleX.repeatMode = ObjectAnimator.REVERSE

        val glowScaleY = ObjectAnimator.ofFloat(glowView, "scaleY", 0.96f, 1.08f)
        glowScaleY.duration = 1100
        glowScaleY.repeatCount = ObjectAnimator.INFINITE
        glowScaleY.repeatMode = ObjectAnimator.REVERSE

        alphaPulse.start()
        scaleX.start()
        scaleY.start()
        glowAlpha.start()
        glowScaleX.start()
        glowScaleY.start()

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, splashDurationMs)
    }
}
