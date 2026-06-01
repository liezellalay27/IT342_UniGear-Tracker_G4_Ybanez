package com.unigear.tracker.mobile

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    private var equipment = mutableListOf<EquipmentItem>()
    private var selectedCategory = "all"
    private lateinit var searchInput: EditText
    private lateinit var listContainer: LinearLayout
    private lateinit var noResultsText: TextView

    private lateinit var categoryContainer: LinearLayout

    private val categoryButtons = mutableMapOf<String, Button>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        searchInput = findViewById(R.id.etSearch)
        listContainer = findViewById(R.id.llEquipmentList)
        noResultsText = findViewById(R.id.tvNoResults)

        categoryContainer = findViewById(R.id.llCategoryFilters)

        findViewById<View>(R.id.btnHomeLogout).setOnClickListener {
            showLogoutConfirmation()
        }

        findViewById<View>(R.id.btnNavCatalog).setOnClickListener { }
        findViewById<View>(R.id.btnNavRequests).setOnClickListener {
            startActivity(Intent(this, MyRequestsActivity::class.java))
        }
        findViewById<View>(R.id.btnNavProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        setupCategoryFilters()
        setupSearch()
        updateCategoryStyles()
        fetchEquipment()
    }

    private fun fetchEquipment() {
        Thread {
            val token = getSharedPreferences("unigear_auth", MODE_PRIVATE).getString("token", null)
            val result = AuthApiClient.getEquipment(token)
            runOnUiThread {
                if (result.success) {
                    equipment = result.equipment.toMutableList()
                    rebuildCategoryFilters()
                    renderEquipment()
                } else {
                    noResultsText.text = "Failed to load equipment: ${result.message}"
                    noResultsText.visibility = TextView.VISIBLE
                    listContainer.removeAllViews()
                }
            }
        }.start()
    }

    private fun setupSearch() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                renderEquipment()
            }
        })
    }

    private fun setupCategoryFilters() {
        rebuildCategoryFilters()
    }

    private fun setCategory(category: String) {
        selectedCategory = category
        updateCategoryStyles()
        renderEquipment()
    }

    private fun updateCategoryStyles() {
        categoryButtons.forEach { (category, button) ->
            if (category == selectedCategory) {
                button.setBackgroundResource(R.drawable.home_chip_active)
                button.setTextColor(getColor(R.color.ug_white))
            } else {
                button.setBackgroundResource(R.drawable.home_chip_inactive)
                button.setTextColor(getColor(R.color.ug_maroon))
            }
        }
    }

    private fun rebuildCategoryFilters() {
        categoryContainer.removeAllViews()
        categoryButtons.clear()

        addCategoryButton("all", "All")

        val categories = equipment
            .map { normalizeCategoryLabel(it.category) }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        categories.forEach { category ->
            addCategoryButton(category, formatCategoryLabel(category))
        }

        updateCategoryStyles()
    }

    private fun addCategoryButton(categoryKey: String, label: String) {
        val button = Button(this).apply {
            text = label
            isAllCaps = false
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(getColor(R.color.ug_maroon))
            setBackgroundResource(R.drawable.home_chip_inactive)
            setOnClickListener { setCategory(categoryKey) }
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 12, 0)
        }

        button.layoutParams = params
        categoryButtons[categoryKey] = button
        categoryContainer.addView(button)
    }

    private fun normalizeCategoryLabel(category: String): String {
        return category.trim().lowercase()
    }

    private fun formatCategoryLabel(category: String): String {
        return category.split(" ", "-", "_")
            .filter { it.isNotBlank() }
            .joinToString(" ") { part ->
                part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
    }

    private fun renderEquipment() {
        val searchText = searchInput.text.toString().trim().lowercase()
        val filtered = equipment.filter { item ->
            val matchesSearch = searchText.isBlank() ||
                item.name.lowercase().contains(searchText) ||
                item.category.lowercase().contains(searchText) ||
                item.location.lowercase().contains(searchText) ||
                item.description.lowercase().contains(searchText) ||
                item.condition.lowercase().contains(searchText)
            val matchesCategory = selectedCategory == "all" ||
                categoryMatches(item.category, selectedCategory)
            matchesSearch && matchesCategory
        }

        listContainer.removeAllViews()

        if (filtered.isEmpty()) {
            noResultsText.text = "No equipment found matching your search criteria."
            noResultsText.visibility = TextView.VISIBLE
            return
        }

        noResultsText.visibility = TextView.GONE

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

            card.setOnClickListener {
                val intent = Intent(this, EquipmentDetailActivity::class.java)
                intent.putExtra("equipment_id", item.id.toInt())
                intent.putExtra("equipment_name", item.name)
                intent.putExtra("equipment_category", item.category)
                intent.putExtra("equipment_status", status)
                intent.putExtra("equipment_location", item.location)
                intent.putExtra("equipment_description", item.description)
                intent.putExtra("equipment_specs", "${item.condition} - Qty: ${item.totalQuantity}")
                startActivity(intent)
            }

            listContainer.addView(card)
        }
    }

    private fun categoryMatches(itemCategory: String, selectedCategory: String): Boolean {
        val normalizedItem = itemCategory.lowercase().trim()
        val normalizedSelected = selectedCategory.lowercase().trim()
        return normalizedItem.contains(normalizedSelected) || normalizedSelected.contains(normalizedItem)
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
