package com.unigear.tracker.mobile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog

class AdminEquipmentActivity : AppCompatActivity() {

    private lateinit var token: String
    private lateinit var loadingText: TextView
    private lateinit var errorText: TextView
    private lateinit var listContainer: LinearLayout
    private lateinit var searchInput: EditText
    private lateinit var categoryInput: EditText
    private lateinit var locationInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var specificationsInput: EditText
    private lateinit var totalQuantityInput: EditText
    private lateinit var availableQuantityInput: EditText
    private lateinit var nameInput: EditText
    private lateinit var refreshButton: LinearLayout
    private lateinit var createButton: Button
    private lateinit var cancelButton: Button

    private var equipment: List<EquipmentItem> = emptyList()
    private var editingId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_equipment)

        loadingText = findViewById(R.id.tvEquipmentAdminLoading)
        errorText = findViewById(R.id.tvEquipmentAdminError)
        listContainer = findViewById(R.id.llEquipmentAdminList)
        searchInput = findViewById(R.id.etAdminEquipmentSearch)
        categoryInput = findViewById(R.id.etAdminEquipmentCategory)
        locationInput = findViewById(R.id.etAdminEquipmentLocation)
        descriptionInput = findViewById(R.id.etAdminEquipmentDescription)
        specificationsInput = findViewById(R.id.etAdminEquipmentSpecifications)
        totalQuantityInput = findViewById(R.id.etAdminEquipmentTotalQty)
        availableQuantityInput = findViewById(R.id.etAdminEquipmentAvailableQty)
        nameInput = findViewById(R.id.etAdminEquipmentName)
        refreshButton = findViewById(R.id.btnAdminEquipmentRefresh)
        createButton = findViewById(R.id.btnAdminEquipmentCreate)
        cancelButton = findViewById(R.id.btnAdminEquipmentCancel)

        token = getSharedPreferences("unigear_auth", MODE_PRIVATE).getString("token", null).orEmpty()
        if (token.isBlank()) {
            finishWithMessage("Please login again.")
            return
        }

        findViewById<View>(R.id.btnAdminEquipmentBack).setOnClickListener { finish() }
        refreshButton.setOnClickListener { fetchEquipment() }
        createButton.setOnClickListener { 
            if (editingId != null) updateEquipment() else createEquipment()
        }
        cancelButton.setOnClickListener {
            editingId = null
            clearForm()
            cancelButton.visibility = View.GONE
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                renderEquipment()
            }
        })

        totalQuantityInput.setText("1")
        availableQuantityInput.setText("1")

        fetchEquipment()
    }

    private fun fetchEquipment() {
        loadingText.visibility = View.VISIBLE
        showError("")

        Thread {
            val token = getSharedPreferences("unigear_auth", MODE_PRIVATE).getString("token", null)
            val result = AuthApiClient.getEquipment(token)
            runOnUiThread {
                loadingText.visibility = View.GONE
                if (result.success) {
                    equipment = result.equipment
                    renderEquipment()
                } else {
                    showError(result.message)
                }
            }
        }.start()
    }

    private fun renderEquipment() {
        listContainer.removeAllViews()

        val query = searchInput.text.toString().trim().lowercase()
        val filtered = equipment.filter { item ->
            query.isBlank() ||
                item.name.lowercase().contains(query) ||
                item.category.lowercase().contains(query) ||
                item.location.lowercase().contains(query)
        }

        if (filtered.isEmpty()) {
            val empty = TextView(this)
            empty.text = "No equipment found."
            empty.setTextColor(getColor(android.R.color.darker_gray))
            empty.setPadding(8, 16, 8, 16)
            listContainer.addView(empty)
            return
        }

        filtered.forEach { item ->
            val card = layoutInflater.inflate(R.layout.item_equipment_card, listContainer, false)
            card.findViewById<TextView>(R.id.tvEquipmentInitial).text = item.name.take(1)
            card.findViewById<TextView>(R.id.tvEquipmentName).text = item.name
            card.findViewById<TextView>(R.id.tvEquipmentCategory).text = item.category
            card.findViewById<TextView>(R.id.tvEquipmentLocation).text = item.location

            val statusText = card.findViewById<TextView>(R.id.tvEquipmentStatus)
            val status = if (item.availableQuantity > 0) "Available" else "In Use"
            statusText.text = status
            if (status == "Available") {
                statusText.setBackgroundResource(R.drawable.status_available_bg)
                statusText.setTextColor(getColor(R.color.status_available_text))
            } else {
                statusText.setBackgroundResource(R.drawable.status_inuse_bg)
                statusText.setTextColor(getColor(R.color.status_inuse_text))
            }

            // add Edit and Delete buttons
                val editBtn = Button(this).apply {
                text = "Edit"
                setOnClickListener {
                    editingId = item.id
                    nameInput.setText(item.name)
                    categoryInput.setText(item.category)
                    locationInput.setText(item.location)
                    descriptionInput.setText(item.description)
                    specificationsInput.setText(item.description)
                    totalQuantityInput.setText(item.totalQuantity.toString())
                    availableQuantityInput.setText(item.availableQuantity.toString())
                    createButton.text = "Update"
                    cancelButton.visibility = View.VISIBLE
                }
            }

            val deleteBtn = Button(this).apply {
                text = "Delete"
                setOnClickListener {
                    AlertDialog.Builder(this@AdminEquipmentActivity)
                        .setTitle("Confirm delete")
                        .setMessage("Delete '${item.name}'? This cannot be undone.")
                        .setPositiveButton("Delete") { _, _ ->
                            Thread {
                                val result = AuthApiClient.deleteEquipment(token, item.id)
                                runOnUiThread {
                                    if (result.success) {
                                        UiToast.show(this@AdminEquipmentActivity, "Equipment deleted.", UiToast.Style.SUCCESS)
                                        fetchEquipment()
                                    } else showError(result.message)
                                }
                            }.start()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }

            val btnContainer = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 8, 0, 0)
                addView(editBtn)
                addView(deleteBtn)
            }

            card.findViewById<LinearLayout>(R.id.llEquipmentCardActions)?.addView(btnContainer)
                ?: listContainer.addView(btnContainer)

            listContainer.addView(card)
        }
    }

    

    private fun createEquipment() {
        val name = nameInput.text.toString().trim()
        val category = categoryInput.text.toString().trim()
        val location = locationInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()
        val specsRaw = specificationsInput.text.toString().trim()
        val totalQuantity = totalQuantityInput.text.toString().toIntOrNull() ?: 0
        val availableQuantity = availableQuantityInput.text.toString().toIntOrNull() ?: 0

        when {
            name.isBlank() -> showError("Equipment name is required")
            category.isBlank() -> showError("Category is required")
            location.isBlank() -> showError("Location is required")
            description.isBlank() -> showError("Description is required")
            specsRaw.isBlank() -> showError("At least one specification is required")
            totalQuantity < 1 -> showError("Total quantity must be at least 1")
            availableQuantity < 0 -> showError("Available quantity cannot be negative")
            availableQuantity > totalQuantity -> showError("Available quantity cannot exceed total quantity")
            else -> {
                val specifications = specsRaw
                    .split("\n", ",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                if (specifications.isEmpty()) {
                    showError("At least one specification is required")
                    return
                }

                loadingText.visibility = View.VISIBLE
                loadingText.text = "Creating equipment..."
                showError("")

                Thread {
                    val result = AuthApiClient.createEquipment(
                        token = token,
                        name = name,
                        category = category,
                        location = location,
                        description = description,
                        specifications = specifications,
                        totalQuantity = totalQuantity,
                        availableQuantity = availableQuantity
                    )
                    runOnUiThread {
                        loadingText.visibility = View.GONE
                        if (result.success) {
                            UiToast.show(this, "Equipment created successfully.", UiToast.Style.SUCCESS)
                            clearForm()
                            fetchEquipment()
                        } else {
                            showError(result.message)
                        }
                    }
                }.start()
            }
        }
    }

    private fun updateEquipment() {
        val id = editingId ?: return

        val name = nameInput.text.toString().trim()
        val category = categoryInput.text.toString().trim()
        val location = locationInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()
        val specsRaw = specificationsInput.text.toString().trim()
        val totalQuantity = totalQuantityInput.text.toString().toIntOrNull() ?: 0
        val availableQuantity = availableQuantityInput.text.toString().toIntOrNull() ?: 0

        when {
            name.isBlank() -> showError("Equipment name is required")
            category.isBlank() -> showError("Category is required")
            location.isBlank() -> showError("Location is required")
            description.isBlank() -> showError("Description is required")
            specsRaw.isBlank() -> showError("At least one specification is required")
            totalQuantity < 1 -> showError("Total quantity must be at least 1")
            availableQuantity < 0 -> showError("Available quantity cannot be negative")
            availableQuantity > totalQuantity -> showError("Available quantity cannot exceed total quantity")
            else -> {
                val specifications = specsRaw
                    .split("\n", ",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                loadingText.visibility = View.VISIBLE
                loadingText.text = "Updating equipment..."
                showError("")

                Thread {
                    val result = AuthApiClient.updateEquipment(
                        token = token,
                        id = id,
                        name = name,
                        category = category,
                        location = location,
                        description = description,
                        specifications = specifications,
                        totalQuantity = totalQuantity,
                        availableQuantity = availableQuantity
                    )
                    runOnUiThread {
                        loadingText.visibility = View.GONE
                        if (result.success) {
                            UiToast.show(this, "Equipment updated successfully.", UiToast.Style.SUCCESS)
                            clearForm()
                            editingId = null
                            fetchEquipment()
                        } else {
                            showError(result.message)
                        }
                    }
                }.start()
            }
        }
    }

    private fun clearForm() {
        nameInput.setText("")
        categoryInput.setText("")
        locationInput.setText("")
        descriptionInput.setText("")
        specificationsInput.setText("")
        totalQuantityInput.setText("1")
        availableQuantityInput.setText("1")
        createButton.text = "Create"
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
}
