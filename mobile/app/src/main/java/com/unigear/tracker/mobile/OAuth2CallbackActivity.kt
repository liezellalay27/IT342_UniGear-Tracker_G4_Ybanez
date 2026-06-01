package com.unigear.tracker.mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class OAuth2CallbackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleRedirect(intent?.data)
    }

    private fun handleRedirect(data: Uri?) {
        if (data == null) {
            UiToast.show(this, "Google authentication failed.", UiToast.Style.ERROR)
            finishToLogin()
            return
        }

        val token = data.getQueryParameter("token")
        val error = data.getQueryParameter("error")

        if (!error.isNullOrBlank()) {
            UiToast.show(this, error, UiToast.Style.ERROR, long = true)
            finishToLogin()
            return
        }

        if (token.isNullOrBlank()) {
            UiToast.show(this, "No token received from Google login.", UiToast.Style.ERROR, long = true)
            finishToLogin()
            return
        }

        getSharedPreferences("unigear_auth", MODE_PRIVATE)
            .edit()
            .putString("token", token)
            .apply()

        Thread {
            val profileResult = AuthApiClient.getUserProfile(token)
            runOnUiThread {
                if (profileResult.success && profileResult.user != null) {
                    getSharedPreferences("unigear_auth", MODE_PRIVATE)
                        .edit()
                        .putString("name", profileResult.user.name)
                        .putString("email", profileResult.user.email)
                        .putString("role", profileResult.user.role ?: "USER")
                        .apply()

                    UiToast.show(this, "Google login successful.", UiToast.Style.SUCCESS)
                    val intent = if (profileResult.user.role.equals("ADMIN", ignoreCase = true)) {
                        Intent(this, AdminDashboardActivity::class.java)
                    } else {
                        Intent(this, HomeActivity::class.java)
                    }
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    UiToast.show(
                        this,
                        profileResult.message.ifBlank { "Google login successful." },
                        UiToast.Style.INFO,
                        long = true
                    )
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                finish()
            }
        }.start()
    }

    private fun finishToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
