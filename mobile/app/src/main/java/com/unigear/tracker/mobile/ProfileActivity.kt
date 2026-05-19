package com.unigear.tracker.mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var profileInitials: TextView
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var userJoinDate: TextView
    private lateinit var userRole: TextView
    private lateinit var loadingText: TextView
    private lateinit var editNameButton: Button
    private lateinit var adminMenuContainer: LinearLayout

    private var currentProfileImagePath: String? = null
    private var userToken: String? = null
    private var currentRole: String = "USER"

    private val PICK_IMAGE_REQUEST = 1
    private val CAMERA_REQUEST = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileImage = findViewById(R.id.ivProfilePicture)
        profileInitials = findViewById(R.id.tvProfileInitials)
        userName = findViewById(R.id.tvProfileName)
        userEmail = findViewById(R.id.tvProfileEmail)
        userJoinDate = findViewById(R.id.tvProfileJoinDate)
        userRole = findViewById(R.id.tvProfileRole)
        loadingText = findViewById(R.id.tvProfileLoading)
        editNameButton = findViewById(R.id.btnEditName)
        adminMenuContainer = findViewById(R.id.llAdminMenu)

        setupActions()
        fetchUserProfile()
    }

    private fun setupActions() {
        profileImage.setOnClickListener {
            showImagePickerOptions()
        }

        findViewById<View>(R.id.btnChangeProfilePicture).setOnClickListener {
            showImagePickerOptions()
        }

        editNameButton.setOnClickListener {
            showEditNameDialog()
        }

        findViewById<View>(R.id.btnLogout).setOnClickListener {
            showLogoutConfirmation()
        }

        findViewById<View>(R.id.btnNavCatalog).setOnClickListener {
            val homeIntent = Intent(this, HomeActivity::class.java)
            homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(homeIntent)
        }

        findViewById<View>(R.id.btnNavRequests).setOnClickListener {
            startActivity(Intent(this, MyRequestsActivity::class.java))
        }

        findViewById<View>(R.id.btnNavProfile).setOnClickListener { }
    }

    private fun fetchUserProfile() {
        userToken = getSharedPreferences("unigear_auth", MODE_PRIVATE).getString("token", null)
        if (userToken.isNullOrBlank()) {
            showError("No session token found. Please login again.")
            return
        }

        loadingText.visibility = TextView.VISIBLE
        loadingText.text = "Loading profile..."

        Thread {
            val result = AuthApiClient.getUserProfile(userToken!!)
            runOnUiThread {
                loadingText.visibility = TextView.GONE
                if (result.success && result.user != null) {
                    displayUserProfile(result.user)
                    currentRole = result.user.role ?: "USER"
                    setupRoleBasedUI()
                    if (!result.user.profilePictureUrl.isNullOrBlank()) {
                        loadProfilePicture(result.user.profilePictureUrl)
                    } else {
                        setDefaultProfilePicture(result.user.name)
                    }
                } else {
                    showError(result.message)
                }
            }
        }.start()
    }

    private fun displayUserProfile(user: UserProfile) {
        userName.text = user.name
        userEmail.text = user.email
        userRole.text = user.role ?: "User"
        userJoinDate.text = "Member since ${formatDate(user.createdAt ?: "")}"
    }

    private fun setupRoleBasedUI() {
        if (currentRole == "ADMIN") {
            adminMenuContainer.visibility = View.VISIBLE
            setupAdminNav()
        } else {
            adminMenuContainer.visibility = View.GONE
        }
    }

    private fun setupAdminNav() {
        val btnAdminEquipment = findViewById<View>(R.id.btnAdminEquipment)
        val btnAdminUsers = findViewById<View>(R.id.btnAdminUsers)
        val btnAdminBorrowed = findViewById<View>(R.id.btnAdminBorrowed)
        val btnAdminRequests = findViewById<View>(R.id.btnAdminRequests)

        btnAdminEquipment?.setOnClickListener {
            Toast.makeText(this, "Admin Equipment Management (Coming Soon)", Toast.LENGTH_SHORT).show()
        }
        btnAdminUsers?.setOnClickListener {
            Toast.makeText(this, "Admin User Management (Coming Soon)", Toast.LENGTH_SHORT).show()
        }
        btnAdminBorrowed?.setOnClickListener {
            Toast.makeText(this, "Admin Borrowed Items (Coming Soon)", Toast.LENGTH_SHORT).show()
        }
        btnAdminRequests?.setOnClickListener {
            Toast.makeText(this, "Admin Requests (Coming Soon)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatDate(dateString: String): String {
        if (dateString.isBlank()) return "Recently"
        return try {
            val parts = dateString.split("T")[0].split("-")
            if (parts.size >= 3) {
                "${parts[2]}/${parts[1]}/${parts[0]}"
            } else {
                dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }

    private fun setDefaultProfilePicture(userName: String) {
        val initials = userName.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull() }
            .joinToString("")
            .uppercase()
            .take(2)

        profileInitials.text = initials
        profileInitials.visibility = TextView.VISIBLE
    }

    private fun loadProfilePicture(url: String) {
        try {
            if (url.startsWith("/") || url.startsWith("file://")) {
                val file = File(url.replace("file://", ""))
                if (file.exists()) {
                    profileImage.setImageURI(Uri.fromFile(file))
                    profileInitials.visibility = TextView.GONE
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Could not load profile picture", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        AlertDialog.Builder(this)
            .setTitle("Change Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                    2 -> {}
                }
            }
            .show()
    }

    private fun openCamera() {
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, CAMERA_REQUEST)
        } catch (e: Exception) {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        } catch (e: Exception) {
            Toast.makeText(this, "Gallery not available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    val imageUri: Uri? = data?.data
                    if (imageUri != null) {
                        profileImage.setImageURI(imageUri)
                        profileInitials.visibility = TextView.GONE
                        currentProfileImagePath = imageUri.toString()
                        uploadProfilePicture()
                    }
                }
                CAMERA_REQUEST -> {
                    Toast.makeText(this, "Photo captured", Toast.LENGTH_SHORT).show()
                    fetchUserProfile()
                }
            }
        }
    }

    private fun uploadProfilePicture() {
        if (userToken.isNullOrBlank() || currentProfileImagePath.isNullOrBlank()) {
            Toast.makeText(this, "Unable to upload picture", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show()
    }

    private fun showEditNameDialog() {
        val editText = EditText(this)
        editText.setText(userName.text)
        editText.hint = "Enter your name"

        AlertDialog.Builder(this)
            .setTitle("Edit Profile Name")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotBlank()) {
                    updateProfileName(newName)
                } else {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateProfileName(newName: String) {
        if (userToken.isNullOrBlank()) {
            showError("No session token. Please login again.")
            return
        }

        loadingText.visibility = TextView.VISIBLE
        loadingText.text = "Updating profile..."

        Thread {
            val result = AuthApiClient.updateUserProfile(userToken!!, newName)
            runOnUiThread {
                loadingText.visibility = TextView.GONE
                if (result.success) {
                    userName.text = newName
                    setDefaultProfilePicture(newName)
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    showError(result.message)
                }
            }
        }.start()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun performLogout() {
        val sharedPref = getSharedPreferences("unigear_auth", MODE_PRIVATE)
        sharedPref.edit().apply {
            clear()
            apply()
        }

        Thread {
            AuthApiClient.logout()
            runOnUiThread {
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                val loginIntent = Intent(this, LoginActivity::class.java)
                loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(loginIntent)
                finish()
            }
        }.start()
    }

    private fun showError(message: String) {
        loadingText.text = message
        loadingText.visibility = TextView.VISIBLE
    }
}


