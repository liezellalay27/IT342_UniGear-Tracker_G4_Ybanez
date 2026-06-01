package com.unigear.tracker.mobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var token: String
    private lateinit var loadingText: TextView
    private lateinit var errorText: TextView
    private lateinit var tabRequestsButton: Button
    private lateinit var tabBorrowedButton: Button
    private lateinit var tabUsersButton: Button
    private lateinit var contentTitle: TextView
    private lateinit var contentContainer: LinearLayout

    private var activeTab: String = "requests"
    private var requestItems: List<RequestItem> = emptyList()
    private var userItems: List<AdminUserItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        loadingText = findViewById(R.id.tvAdminLoading)
        errorText = findViewById(R.id.tvAdminError)
        tabRequestsButton = findViewById(R.id.btnAdminTabRequests)
        tabBorrowedButton = findViewById(R.id.btnAdminTabBorrowed)
        tabUsersButton = findViewById(R.id.btnAdminTabUsers)
        contentTitle = findViewById(R.id.tvAdminContentTitle)
        contentContainer = findViewById(R.id.llAdminContent)

        token = getSharedPreferences("unigear_auth", MODE_PRIVATE).getString("token", null).orEmpty()
        if (token.isBlank()) {
            finishWithMessage("Please login again.")
            return
        }

        activeTab = intent.getStringExtra("initial_tab") ?: "requests"
        setupActions()
        updateTabStyles()
        verifyAdminAndLoad()
    }

    private fun setupActions() {
        tabRequestsButton.setOnClickListener {
            activeTab = "requests"
            updateTabStyles()
            loadCurrentTab()
        }
        tabBorrowedButton.setOnClickListener {
            activeTab = "borrowed"
            updateTabStyles()
            loadCurrentTab()
        }
        tabUsersButton.setOnClickListener {
            activeTab = "users"
            updateTabStyles()
            loadCurrentTab()
        }

        findViewById<View>(R.id.btnAdminEquipmentShortcut).setOnClickListener {
            startActivity(Intent(this, AdminEquipmentActivity::class.java))
        }

        findViewById<View>(R.id.btnAdminLogout).setOnClickListener {
            showLogoutConfirmation()
        }

        findViewById<View>(R.id.btnAdminRefresh).setOnClickListener {
            loadCurrentTab()
        }

        findViewById<View>(R.id.btnAdminBack).setOnClickListener {
            finish()
        }
    }

    private fun verifyAdminAndLoad() {
        loadingText.visibility = View.VISIBLE
        loadingText.text = "Verifying admin access..."
        showError("")

        Thread {
            val profileResult = AuthApiClient.getUserProfile(token)
            runOnUiThread {
                loadingText.visibility = View.GONE
                if (!profileResult.success || profileResult.user == null) {
                    finishWithMessage(profileResult.message.ifBlank { "Unable to load profile" })
                    return@runOnUiThread
                }

                if (!profileResult.user.role.equals("ADMIN", ignoreCase = true)) {
                    finishWithMessage("Admin access only.")
                    return@runOnUiThread
                }

                loadCurrentTab()
            }
        }.start()
    }

    private fun loadCurrentTab() {
        loadingText.visibility = View.VISIBLE
        showError("")
        contentContainer.removeAllViews()

        when (activeTab) {
            "users" -> {
                contentTitle.text = "Users"
                Thread {
                    val result = AuthApiClient.getAdminUsers(token)
                    runOnUiThread {
                        loadingText.visibility = View.GONE
                        if (result.success) {
                            userItems = result.users
                            renderUsers()
                        } else {
                            showError(result.message)
                        }
                    }
                }.start()
            }
            "borrowed" -> {
                contentTitle.text = "Borrowed Items"
                Thread {
                    val result = AuthApiClient.getBorrowedRequests(token)
                    runOnUiThread {
                        loadingText.visibility = View.GONE
                        if (result.success) {
                            requestItems = result.requests
                            renderRequests(showActions = false)
                        } else {
                            showError(result.message)
                        }
                    }
                }.start()
            }
            else -> {
                contentTitle.text = "Requests"
                Thread {
                    val result = AuthApiClient.getAdminRequests(token)
                    runOnUiThread {
                        loadingText.visibility = View.GONE
                        if (result.success) {
                            requestItems = result.requests
                            renderRequests(showActions = true)
                        } else {
                            showError(result.message)
                        }
                    }
                }.start()
            }
        }
    }

    private fun renderRequests(showActions: Boolean) {
        contentContainer.removeAllViews()

        if (requestItems.isEmpty()) {
            showEmptyState("No items found.")
            return
        }

        requestItems.forEach { item ->
            val card = layoutInflater.inflate(R.layout.item_admin_request_card, contentContainer, false)
            card.findViewById<TextView>(R.id.tvAdminRequestTitle).text = item.equipmentName
            card.findViewById<TextView>(R.id.tvAdminRequestRequester).text =
                "Requester: ${item.requesterName.ifBlank { item.studentName.ifBlank { "-" } }}"
            card.findViewById<TextView>(R.id.tvAdminRequestEmail).text =
                "Email: ${item.requesterEmail.ifBlank { "-" }}"
            card.findViewById<TextView>(R.id.tvAdminRequestMeta).text =
                "Borrow: ${displayDate(item.borrowDate)} | Return: ${displayDate(item.returnDate)}"
            card.findViewById<TextView>(R.id.tvAdminRequestStudent).text =
                "Student: ${item.studentName.ifBlank { "-" }} | ID: ${item.schoolIdNumber.ifBlank { "-" }}"
            card.findViewById<TextView>(R.id.tvAdminRequestCourse).text =
                "${item.yearLevel.ifBlank { "-" }} | ${item.course.ifBlank { "-" }}"
            card.findViewById<TextView>(R.id.tvAdminRequestStatus).text = item.status
            card.findViewById<TextView>(R.id.tvAdminRequestDescription).text =
                if (item.description.isBlank()) "No description provided" else item.description

            val approveButton = card.findViewById<Button>(R.id.btnAdminApprove)
            val rejectButton = card.findViewById<Button>(R.id.btnAdminReject)
            val completeButton = card.findViewById<Button>(R.id.btnAdminComplete)
            val actionRow = card.findViewById<LinearLayout>(R.id.llAdminRequestActions)

            if (!showActions) {
                actionRow.visibility = View.GONE
            } else {
                when {
                    item.status.equals("PENDING", true) -> {
                        actionRow.visibility = View.VISIBLE
                        approveButton.visibility = View.VISIBLE
                        rejectButton.visibility = View.VISIBLE
                        completeButton.visibility = View.GONE

                        approveButton.setOnClickListener { promptUpdate(item.id, "APPROVED") }
                        rejectButton.setOnClickListener { promptUpdate(item.id, "REJECTED") }
                    }
                    item.status.equals("APPROVED", true) -> {
                        actionRow.visibility = View.VISIBLE
                        approveButton.visibility = View.GONE
                        rejectButton.visibility = View.GONE
                        completeButton.visibility = View.VISIBLE
                        completeButton.setOnClickListener { promptComplete(item.id) }
                    }
                    else -> {
                        actionRow.visibility = View.GONE
                    }
                }
            }

            contentContainer.addView(card)
        }
    }

    private fun renderUsers() {
        contentContainer.removeAllViews()

        if (userItems.isEmpty()) {
            showEmptyState("No users found.")
            return
        }

        userItems.forEach { item ->
            val card = layoutInflater.inflate(R.layout.item_admin_user_card, contentContainer, false)
            card.findViewById<TextView>(R.id.tvAdminUserName).text = item.name
            card.findViewById<TextView>(R.id.tvAdminUserEmail).text = item.email
            card.findViewById<TextView>(R.id.tvAdminUserRole).text = item.role
            card.findViewById<TextView>(R.id.tvAdminUserCreated).text = "Joined: ${displayDate(item.createdAt)}"
            contentContainer.addView(card)
        }
    }

    private fun promptUpdate(requestId: Long, status: String) {
        val input = EditText(this)
        input.hint = "Add an approval or rejection note"

        AlertDialog.Builder(this)
            .setTitle(if (status == "APPROVED") "Approve request" else "Reject request")
            .setMessage("Write a short note for the student, then confirm.")
            .setView(input)
            .setPositiveButton("Confirm") { _, _ ->
                updateRequest(requestId, status, input.text.toString().trim(), null)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun promptComplete(requestId: Long) {
        AlertDialog.Builder(this)
            .setTitle("Mark completed")
            .setMessage("Was the item returned on time?")
            .setPositiveButton("Yes") { _, _ ->
                updateRequest(requestId, "COMPLETED", null, true)
            }
            .setNegativeButton("No") { _, _ ->
                updateRequest(requestId, "COMPLETED", null, false)
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun updateRequest(requestId: Long, status: String, notes: String?, returnedOnTime: Boolean?) {
        loadingText.visibility = View.VISIBLE
        loadingText.text = "Updating request..."
        showError("")

        Thread {
            val result = AuthApiClient.updateAdminRequestStatus(token, requestId, status, notes, returnedOnTime)
            runOnUiThread {
                loadingText.visibility = View.GONE
                if (result.success) {
                    val friendlyMessage = when (status) {
                        "APPROVED" -> if (notes.isNullOrBlank()) {
                            "Request approved successfully."
                        } else {
                            "Request approved. Approval note saved."
                        }
                        "REJECTED" -> if (notes.isNullOrBlank()) {
                            "Request rejected successfully."
                        } else {
                            "Request rejected. Rejection note saved."
                        }
                        "COMPLETED" -> if (returnedOnTime == true) {
                            "Marked as returned on time."
                        } else {
                            "Marked as returned late."
                        }
                        else -> result.message.ifBlank { "Request updated successfully." }
                    }
                    UiToast.show(this, friendlyMessage, UiToast.Style.SUCCESS)
                    loadCurrentTab()
                } else {
                    showError(result.message)
                }
            }
        }.start()
    }

    private fun updateTabStyles() {
        val activeTextColor = getColor(R.color.ug_white)
        val inactiveTextColor = getColor(R.color.ug_maroon)

        if (activeTab == "requests") {
            tabRequestsButton.setBackgroundResource(R.drawable.home_chip_active)
            tabBorrowedButton.setBackgroundResource(R.drawable.home_chip_inactive)
            tabUsersButton.setBackgroundResource(R.drawable.home_chip_inactive)
            tabRequestsButton.setTextColor(activeTextColor)
            tabBorrowedButton.setTextColor(inactiveTextColor)
            tabUsersButton.setTextColor(inactiveTextColor)
        } else if (activeTab == "borrowed") {
            tabRequestsButton.setBackgroundResource(R.drawable.home_chip_inactive)
            tabBorrowedButton.setBackgroundResource(R.drawable.home_chip_active)
            tabUsersButton.setBackgroundResource(R.drawable.home_chip_inactive)
            tabRequestsButton.setTextColor(inactiveTextColor)
            tabBorrowedButton.setTextColor(activeTextColor)
            tabUsersButton.setTextColor(inactiveTextColor)
        } else {
            tabRequestsButton.setBackgroundResource(R.drawable.home_chip_inactive)
            tabBorrowedButton.setBackgroundResource(R.drawable.home_chip_inactive)
            tabUsersButton.setBackgroundResource(R.drawable.home_chip_active)
            tabRequestsButton.setTextColor(inactiveTextColor)
            tabBorrowedButton.setTextColor(inactiveTextColor)
            tabUsersButton.setTextColor(activeTextColor)
        }
    }

    private fun showEmptyState(message: String) {
        val empty = TextView(this)
        empty.text = message
        empty.setTextColor(getColor(android.R.color.darker_gray))
        empty.textSize = 14f
        empty.setPadding(8, 24, 8, 24)
        contentContainer.addView(empty)
    }

    private fun displayDate(raw: String): String {
        return if (raw.length >= 10) raw.substring(0, 10) else raw.ifBlank { "-" }
    }

    private fun showError(message: String) {
        if (message.isBlank()) {
            errorText.visibility = View.GONE
        } else {
            errorText.visibility = View.VISIBLE
            errorText.text = message
        }
    }

    private fun finishWithMessage(message: String) {
        UiToast.show(this, message, UiToast.Style.INFO)
        finish()
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
        sharedPref.edit().clear().apply()

        Thread {
            AuthApiClient.logout()
            runOnUiThread {
                UiToast.show(this, "You have been logged out.", UiToast.Style.INFO)
                val loginIntent = Intent(this, LoginActivity::class.java)
                loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(loginIntent)
                finish()
            }
        }.start()
    }
}
