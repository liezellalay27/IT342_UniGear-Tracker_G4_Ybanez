package com.unigear.tracker.mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class OAuth2CallbackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleRedirect(intent?.data)
    }

    private fun handleRedirect(data: Uri?) {
        if (data == null) {
            Toast.makeText(this, "Google authentication failed", Toast.LENGTH_SHORT).show()
            finishToLogin()
            return
        }

        val token = data.getQueryParameter("token")
        val error = data.getQueryParameter("error")

        if (!error.isNullOrBlank()) {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            finishToLogin()
            return
        }

        if (token.isNullOrBlank()) {
            Toast.makeText(this, "No token received from Google login", Toast.LENGTH_LONG).show()
            finishToLogin()
            return
        }

        getSharedPreferences("unigear_auth", MODE_PRIVATE)
            .edit()
            .putString("token", token)
            .apply()

        Toast.makeText(this, "Google login successful", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun finishToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
