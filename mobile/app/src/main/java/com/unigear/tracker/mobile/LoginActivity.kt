package com.unigear.tracker.mobile

import android.content.Intent
import android.os.Build
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private val mobileRedirectUri = "unigear://auth"
    private var hasPromptedBackendSetup = false

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var errorText: TextView
    private lateinit var loginButton: Button
    private lateinit var googleLoginButton: Button
    private lateinit var backendLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailInput = findViewById(R.id.etEmail)
        passwordInput = findViewById(R.id.etPassword)
        errorText = findViewById(R.id.tvLoginError)

        loginButton = findViewById(R.id.btnLogin)
        googleLoginButton = findViewById(R.id.btnGoogleLogin)
        backendLabel = findViewById(R.id.tvBackendLabel)
        val changeBackend = findViewById<TextView>(R.id.tvChangeBackend)
        val forgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val goRegister = findViewById<TextView>(R.id.tvGoRegister)

        // Allow overriding backend URL for physical-device testing.
        val configuredBaseUrl = getSharedPreferences("unigear_config", MODE_PRIVATE)
            .getString("backend_base_url", null)
        AuthApiClient.setBackendBaseUrl(configuredBaseUrl)
        updateBackendLabel()

        if (!isProbablyEmulator() && AuthApiClient.backendBaseUrl.contains("10.0.2.2")) {
            showError("Set backend URL to your PC LAN IP before using Google login.")
            hasPromptedBackendSetup = true
            showBackendDialog()
        }

        googleLoginButton.visibility = View.VISIBLE
        googleLoginButton.isEnabled = true

        loginButton.setOnClickListener {
            attemptLogin()
        }

        googleLoginButton.setOnClickListener {
            startGoogleLogin()
        }

        forgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

        changeBackend.setOnClickListener {
            showBackendDialog()
        }

        goRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()

        when {
            email.isEmpty() -> showError("Email is required")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> showError("Email is invalid")
            password.isEmpty() -> showError("Password is required")
            else -> performLogin(email, password)
        }
    }

    private fun performLogin(email: String, password: String) {
        showError("")
        setLoading(true)

        Thread {
            val result = AuthApiClient.login(email, password)
            runOnUiThread {
                setLoading(false)
                if (result.success) {
                    val prefs = getSharedPreferences("unigear_auth", MODE_PRIVATE)
                    prefs.edit()
                        .putString("token", result.token)
                        .putString("name", result.name)
                        .putString("email", result.email ?: email)
                        .putString("role", result.role ?: "USER")
                        .apply()

                    UiToast.show(this, "Login successful. Welcome back.", UiToast.Style.SUCCESS)
                    val intent = if (result.role.equals("ADMIN", ignoreCase = true)) {
                        Intent(this, AdminDashboardActivity::class.java)
                    } else {
                        Intent(this, HomeActivity::class.java)
                    }
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    showError(result.message)
                }
            }
        }.start()
    }

    private fun showError(message: String) {
        if (message.isBlank()) {
            errorText.visibility = View.GONE
        } else {
            errorText.text = message
            errorText.visibility = View.VISIBLE
        }
    }

    private fun setLoading(isLoading: Boolean) {
        loginButton.isEnabled = !isLoading
        loginButton.text = if (isLoading) "Logging in..." else "Login"
    }

    private fun setGoogleLoading(isLoading: Boolean) {
        googleLoginButton.isEnabled = !isLoading
        googleLoginButton.text = if (isLoading) "Connecting..." else "Continue with Google"
    }

    private fun isProbablyEmulator(): Boolean {
        return Build.FINGERPRINT.contains("generic", ignoreCase = true) ||
            Build.MODEL.contains("Emulator", ignoreCase = true) ||
            Build.MODEL.contains("Android SDK", ignoreCase = true)
    }

    private fun updateBackendLabel() {
        backendLabel.text = "Backend: ${AuthApiClient.backendBaseUrl}"
    }

    private fun showBackendDialog() {
        val input = EditText(this)
        input.setSingleLine(true)
        input.hint = "http://192.168.x.x:8080"
        input.setText(AuthApiClient.backendBaseUrl)

        AlertDialog.Builder(this)
            .setTitle("Set Backend URL")
            .setMessage("Use PC LAN IP for real phone, e.g. http://192.168.1.10:8080")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val value = input.text.toString().trim()
                if (!value.startsWith("http://") && !value.startsWith("https://")) {
                    showError("Backend URL must start with http:// or https://")
                    return@setPositiveButton
                }

                getSharedPreferences("unigear_config", MODE_PRIVATE)
                    .edit()
                    .putString("backend_base_url", value)
                    .apply()

                AuthApiClient.setBackendBaseUrl(value)
                updateBackendLabel()
                showError("")
                UiToast.show(this, "Backend URL updated.", UiToast.Style.SUCCESS)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showForgotPasswordDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_forgot_password, null)
        val input = view.findViewById<EditText>(R.id.etForgotEmail)
        input.setText(emailInput.text?.toString()?.trim().orEmpty())

        val dialog = AlertDialog.Builder(this)
            .setTitle("Forgot Password")
            .setView(view)
            .setPositiveButton("Send", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val send = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            send.setOnClickListener {
                val email = input.text.toString().trim()
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    showError("Please enter a valid email address")
                    return@setOnClickListener
                }

                showError("")
                setLoading(true)

                Thread {
                    val result = AuthApiClient.requestPasswordReset(email)
                    runOnUiThread {
                        setLoading(false)
                        if (result.success) {
                            UiToast.show(this, "Password reset email sent. Check your inbox.", UiToast.Style.SUCCESS, long = true)
                            dialog.dismiss()
                        } else {
                            showError(result.message)
                        }
                    }
                }.start()
            }
        }

        dialog.show()
    }

    private fun startGoogleLogin() {
        val baseUrl = AuthApiClient.backendBaseUrl
        if (baseUrl.contains("10.0.2.2") && !isProbablyEmulator()) {
            showError(
                "You are on a physical device. 10.0.2.2 only works on emulator. " +
                    "Set backend_base_url in shared prefs to your PC LAN IP, e.g. http://192.168.x.x:8080"
            )
            if (!hasPromptedBackendSetup) {
                hasPromptedBackendSetup = true
                showBackendDialog()
            }
            return
        }

        setGoogleLoading(true)
        Thread {
            val reachable = AuthApiClient.canReachBackend()
            runOnUiThread {
                setGoogleLoading(false)
                if (!reachable) {
                    showError("Cannot reach backend at $baseUrl. Start backend and verify network access.")
                    return@runOnUiThread
                }

                val authUrl = Uri.parse("$baseUrl/api/auth/mobile/google")
                    .buildUpon()
                    .appendQueryParameter("redirect_uri", mobileRedirectUri)
                    .build()

                startActivity(Intent(Intent.ACTION_VIEW, authUrl))
            }
        }.start()
    }
}
