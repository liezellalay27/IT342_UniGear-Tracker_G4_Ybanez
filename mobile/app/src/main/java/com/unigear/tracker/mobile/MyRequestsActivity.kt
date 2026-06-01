package com.unigear.tracker.mobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MyRequestsActivity : AppCompatActivity() {

    private lateinit var requestsContainer: LinearLayout
    private lateinit var emptyText: TextView
    private lateinit var errorText: TextView

    private lateinit var totalCountText: TextView
    private lateinit var pendingCountText: TextView
    private lateinit var approvedCountText: TextView
    private lateinit var completedCountText: TextView

    private lateinit var activeTabButton: Button
    private lateinit var historyTabButton: Button

    private lateinit var formContainer: View
    private lateinit var equipmentNameInput: EditText
    private lateinit var quantityInput: EditText
    private lateinit var borrowDateInput: EditText
    private lateinit var returnDateInput: EditText
    private lateinit var studentNameInput: EditText
    private lateinit var schoolIdInput: EditText
    private lateinit var yearLevelInput: EditText
    private lateinit var courseInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var categorySpinner: Spinner

    private var requests: List<RequestItem> = emptyList()
    private var activeTab = "active"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_requests)

        requestsContainer = findViewById(R.id.llRequestList)
        emptyText = findViewById(R.id.tvNoRequests)
        errorText = findViewById(R.id.tvRequestError)

        totalCountText = findViewById(R.id.tvTotalCount)
        pendingCountText = findViewById(R.id.tvPendingCount)
        approvedCountText = findViewById(R.id.tvApprovedCount)
        completedCountText = findViewById(R.id.tvCompletedCount)

        activeTabButton = findViewById(R.id.btnTabActive)
        historyTabButton = findViewById(R.id.btnTabHistory)

        formContainer = findViewById(R.id.requestFormContainer)
        equipmentNameInput = findViewById(R.id.etRequestEquipmentName)
        quantityInput = findViewById(R.id.etRequestQuantity)
        borrowDateInput = findViewById(R.id.etBorrowDate)
        returnDateInput = findViewById(R.id.etReturnDate)
        studentNameInput = findViewById(R.id.etStudentName)
        schoolIdInput = findViewById(R.id.etSchoolIdNumber)
        yearLevelInput = findViewById(R.id.etYearLevel)
        courseInput = findViewById(R.id.etCourse)
        descriptionInput = findViewById(R.id.etRequestDescription)
        categorySpinner = findViewById(R.id.spRequestCategory)

        setupCategorySpinner()
        setupActions()
        applyPrefillFromIntent()
        updateTabStyles()
        fetchRequests()
    }

    private fun setupCategorySpinner() {
        val categories = listOf("Laptop", "Desktop", "Monitor", "Keyboard", "Mouse", "Headset", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    private fun setupActions() {
        findViewById<View>(R.id.btnToggleForm).setOnClickListener {
            formContainer.visibility = if (formContainer.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        findViewById<View>(R.id.btnSubmitRequest).setOnClickListener {
            createRequest()
        }

        activeTabButton.setOnClickListener {
            activeTab = "active"
            updateTabStyles()
            renderRequests()
        }

        historyTabButton.setOnClickListener {
            activeTab = "history"
            updateTabStyles()
            renderRequests()
        }

        findViewById<View>(R.id.btnNavCatalog).setOnClickListener {
            val homeIntent = Intent(this, HomeActivity::class.java)
            homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(homeIntent)
        }

        findViewById<View>(R.id.btnNavRequests).setOnClickListener { }

        findViewById<View>(R.id.btnNavProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun applyPrefillFromIntent() {
        val openForm = intent.getBooleanExtra("open_form", false)
        val prefillName = intent.getStringExtra("prefill_equipment_name")
        val prefillCategory = intent.getStringExtra("prefill_category")

        if (!prefillName.isNullOrBlank()) {
            equipmentNameInput.setText(prefillName)
        }

        if (!prefillCategory.isNullOrBlank()) {
            val normalized = prefillCategory.lowercase()
            val options = listOf("laptop", "desktop", "monitor", "keyboard", "mouse", "headset", "other")
            val selectedIndex = options.indexOfFirst { normalized.contains(it) }
            if (selectedIndex >= 0) {
                categorySpinner.setSelection(selectedIndex)
            } else {
                categorySpinner.setSelection(options.lastIndex)
            }
        }

        if (openForm) {
            formContainer.visibility = View.VISIBLE
        }

        val storedName = getSharedPreferences("unigear_auth", MODE_PRIVATE).getString("name", null)
        if (!storedName.isNullOrBlank() && studentNameInput.text.isNullOrBlank()) {
            studentNameInput.setText(storedName)
        }
    }

    private fun updateTabStyles() {
        if (activeTab == "active") {
            activeTabButton.setBackgroundResource(R.drawable.home_chip_active)
            historyTabButton.setBackgroundResource(R.drawable.home_chip_inactive)
            activeTabButton.setTextColor(getColor(R.color.ug_white))
            historyTabButton.setTextColor(getColor(R.color.ug_maroon))
        } else {
            activeTabButton.setBackgroundResource(R.drawable.home_chip_inactive)
            historyTabButton.setBackgroundResource(R.drawable.home_chip_active)
            activeTabButton.setTextColor(getColor(R.color.ug_maroon))
            historyTabButton.setTextColor(getColor(R.color.ug_white))
        }
    }

    private fun fetchRequests() {
        val token = getSharedPreferences("unigear_auth", MODE_PRIVATE).getString("token", null)
        if (token.isNullOrBlank()) {
            showError("No session token found. Please login again.")
            return
        }

        showError("")
        Thread {
            val result = AuthApiClient.getRequests(token)
            runOnUiThread {
                if (result.success) {
                    requests = result.requests
                    renderStats()
                    renderRequests()
                } else {
                    showError(result.message)
                }
            }
        }.start()
    }

    private fun createRequest() {
        val token = getSharedPreferences("unigear_auth", MODE_PRIVATE).getString("token", null)
        if (token.isNullOrBlank()) {
            showError("No session token found. Please login again.")
            return
        }

        val equipmentName = equipmentNameInput.text.toString().trim()
        val category = categorySpinner.selectedItem?.toString()?.trim().orEmpty()
        val description = descriptionInput.text.toString().trim()
        val quantity = quantityInput.text.toString().toIntOrNull() ?: 0
        val borrowDate = borrowDateInput.text.toString().trim()
        val returnDate = returnDateInput.text.toString().trim()
        val studentName = studentNameInput.text.toString().trim()
        val schoolIdNumber = schoolIdInput.text.toString().trim()
        val yearLevel = yearLevelInput.text.toString().trim()
        val course = courseInput.text.toString().trim()

        when {
            equipmentName.isBlank() -> showError("Equipment name is required")
            category.isBlank() -> showError("Category is required")
            quantity < 1 -> showError("Quantity must be at least 1")
            borrowDate.isBlank() -> showError("Borrow date is required")
            returnDate.isBlank() -> showError("Return date is required")
            studentName.isBlank() -> showError("Student name is required")
            schoolIdNumber.isBlank() -> showError("School ID number is required")
            !Regex("^\\d{2}-\\d{4}-\\d{3}$").matches(schoolIdNumber) -> showError("School ID must follow the format 17-0635-488")
            yearLevel.isBlank() -> showError("Year level is required")
            course.isBlank() -> showError("Course is required")
            else -> {
                showError("")
                Thread {
                    val result = AuthApiClient.createRequest(
                        token,
                        equipmentName,
                        category,
                        description,
                        quantity,
                        borrowDate,
                        returnDate,
                        studentName,
                        schoolIdNumber,
                        yearLevel,
                        course
                    )
                    runOnUiThread {
                        if (result.success) {
                            UiToast.show(this, "Request submitted successfully.", UiToast.Style.SUCCESS)
                            clearForm()
                            formContainer.visibility = View.GONE
                            fetchRequests()
                        } else {
                            showError(result.message)
                        }
                    }
                }.start()
            }
        }
    }

    private fun deleteRequest(item: RequestItem) {
        val token = getSharedPreferences("unigear_auth", MODE_PRIVATE).getString("token", null)
        if (token.isNullOrBlank()) {
            showError("No session token found. Please login again.")
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Delete request")
            .setMessage("Delete request for ${item.equipmentName}?")
            .setPositiveButton("Delete") { _, _ ->
                Thread {
                    val result = AuthApiClient.deleteRequest(token, item.id)
                    runOnUiThread {
                        if (result.success) {
                            fetchRequests()
                        } else {
                            showError(result.message)
                        }
                    }
                }.start()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun renderStats() {
        val pending = requests.count { it.status.equals("PENDING", true) }
        val approved = requests.count { it.status.equals("APPROVED", true) }
        val completed = requests.count { it.status.equals("COMPLETED", true) }

        totalCountText.text = requests.size.toString()
        pendingCountText.text = pending.toString()
        approvedCountText.text = approved.toString()
        completedCountText.text = completed.toString()
    }

    private fun renderRequests() {
        requestsContainer.removeAllViews()

        val filtered = if (activeTab == "active") {
            requests.filter { !it.status.equals("COMPLETED", true) && !it.status.equals("REJECTED", true) }
        } else {
            requests.filter { it.status.equals("COMPLETED", true) || it.status.equals("REJECTED", true) }
        }

        if (filtered.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            emptyText.text = if (activeTab == "active") {
                "No active requests yet."
            } else {
                "No request history yet."
            }
            return
        }

        emptyText.visibility = View.GONE

        filtered.forEach { item ->
            val card = layoutInflater.inflate(R.layout.item_request_card, requestsContainer, false)
            card.findViewById<TextView>(R.id.tvRequestTitle).text = item.equipmentName
            card.findViewById<TextView>(R.id.tvRequestCategory).text = "Category: ${item.category}"
            card.findViewById<TextView>(R.id.tvRequestQuantity).text = "Quantity: ${item.quantity}"
            card.findViewById<TextView>(R.id.tvRequestBorrowReturn).text =
                "Borrow: ${displayDate(item.borrowDate)} | Return: ${displayDate(item.returnDate)}"
            card.findViewById<TextView>(R.id.tvRequestStudent).text =
                "Student: ${item.studentName.ifBlank { "-" }}"
            card.findViewById<TextView>(R.id.tvRequestDate).text = "Created: ${displayDate(item.createdAt)}"
            card.findViewById<TextView>(R.id.tvRequestDescription).text =
                if (item.description.isBlank()) "No purpose provided" else item.description

            val statusView = card.findViewById<TextView>(R.id.tvRequestStatus)
            statusView.text = item.status
            if (item.status.equals("PENDING", true)) {
                statusView.setBackgroundResource(R.drawable.status_inuse_bg)
                statusView.setTextColor(getColor(R.color.status_inuse_text))
            } else {
                statusView.setBackgroundResource(R.drawable.status_available_bg)
                statusView.setTextColor(getColor(R.color.status_available_text))
            }

            val deleteButton = card.findViewById<Button>(R.id.btnDeleteRequest)
            if (item.status.equals("PENDING", true)) {
                deleteButton.visibility = View.VISIBLE
                deleteButton.setOnClickListener { deleteRequest(item) }
            } else {
                deleteButton.visibility = View.GONE
            }

            requestsContainer.addView(card)
        }
    }

    private fun displayDate(raw: String): String {
        return if (raw.length >= 10) raw.substring(0, 10) else raw
    }

    private fun clearForm() {
        equipmentNameInput.setText("")
        quantityInput.setText("1")
        borrowDateInput.setText("")
        returnDateInput.setText("")
        studentNameInput.setText(getSharedPreferences("unigear_auth", MODE_PRIVATE).getString("name", "") ?: "")
        schoolIdInput.setText("")
        yearLevelInput.setText("")
        courseInput.setText("")
        descriptionInput.setText("")
        categorySpinner.setSelection(0)
    }

    private fun showError(message: String) {
        if (message.isBlank()) {
            errorText.visibility = View.GONE
        } else {
            errorText.visibility = View.VISIBLE
            errorText.text = message
        }
    }
}
