package com.unigear.tracker.mobile

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var errorText: TextView
    private lateinit var successText: TextView
    private lateinit var createAccountButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        nameInput = findViewById(R.id.etName)
        emailInput = findViewById(R.id.etRegisterEmail)
        passwordInput = findViewById(R.id.etRegisterPassword)
        confirmPasswordInput = findViewById(R.id.etConfirmPassword)
        errorText = findViewById(R.id.tvRegisterError)
        successText = findViewById(R.id.tvRegisterSuccess)

        createAccountButton = findViewById(R.id.btnCreateAccount)
        val goLogin = findViewById<TextView>(R.id.tvGoLogin)

        createAccountButton.setOnClickListener {
            attemptRegister()
        }

        goLogin.setOnClickListener {
            finish()
        }
    }

    private fun attemptRegister() {
        val name = nameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        val confirmPassword = confirmPasswordInput.text.toString()

        when {
            name.isEmpty() -> showError("Name is required")
            name.length < 2 -> showError("Name must be at least 2 characters")
            email.isEmpty() -> showError("Email is required")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> showError("Email is invalid")
            password.isEmpty() -> showError("Password is required")
            password.length < 6 -> showError("Password must be at least 6 characters")
            confirmPassword.isEmpty() -> showError("Please confirm your password")
            password != confirmPassword -> showError("Passwords do not match")
            else -> performRegister(name, email, password)
        }
    }

    private fun performRegister(name: String, email: String, password: String) {
        showError("")
        successText.visibility = View.GONE
        setLoading(true)

        Thread {
            val result = AuthApiClient.register(name, email, password)
            runOnUiThread {
                setLoading(false)
                if (result.success) {
                    successText.text = "Registration successful! Redirecting to login..."
                    successText.visibility = View.VISIBLE
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        finish()
                    }, 1200)
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
            successText.visibility = View.GONE
            errorText.text = message
            errorText.visibility = View.VISIBLE
        }
    }

    private fun setLoading(isLoading: Boolean) {
        createAccountButton.isEnabled = !isLoading
        createAccountButton.text = if (isLoading) "Creating Account..." else "Create Account"
    }
}
