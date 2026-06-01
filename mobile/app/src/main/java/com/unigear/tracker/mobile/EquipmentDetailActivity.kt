package com.unigear.tracker.mobile

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.DateFormat
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.util.Calendar

class EquipmentDetailActivity : AppCompatActivity() {

    private var images = listOf(
        "https://via.placeholder.com/400x300?text=Equipment+View+1",
        "https://via.placeholder.com/400x300?text=Equipment+View+2",
        "https://via.placeholder.com/400x300?text=Equipment+View+3",
        "https://via.placeholder.com/400x300?text=Equipment+View+4"
    )
    private var currentImageIndex = 0
    private var borrowedDates = mutableSetOf<String>()
    private var currentCalendarMonth = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equipment_detail)

        val name = intent.getStringExtra("equipment_name") ?: "Equipment"
        val category = intent.getStringExtra("equipment_category") ?: "N/A"
        val status = intent.getStringExtra("equipment_status") ?: "Unknown"
        val location = intent.getStringExtra("equipment_location") ?: "N/A"
        val description = intent.getStringExtra("equipment_description") ?: "No description available."
        val specs = intent.getStringExtra("equipment_specs") ?: "N/A"

        // Set basic equipment info
        findViewById<TextView>(R.id.tvDetailName).text = name
        findViewById<TextView>(R.id.tvDetailCategory).text = category
        findViewById<TextView>(R.id.tvDetailLocation).text = location
        findViewById<TextView>(R.id.tvDetailDescription).text = description
        findViewById<TextView>(R.id.tvDetailSpecs).text = specs

        val statusView = findViewById<TextView>(R.id.tvDetailStatus)
        statusView.text = status
        if (status.equals("Available", ignoreCase = true)) {
            statusView.setBackgroundResource(R.drawable.status_available_bg)
            statusView.setTextColor(getColor(R.color.status_available_text))
        } else {
            statusView.setBackgroundResource(R.drawable.status_inuse_bg)
            statusView.setTextColor(getColor(R.color.status_inuse_text))
        }

        // Setup image gallery
        setupImageGallery()

        // Fetch borrowed dates from backend
        fetchBorrowedDates(name)

        // Setup calendar
        setupCalendar()

        // Button listeners
        findViewById<View>(R.id.btnRequestNow).setOnClickListener {
            val requestIntent = Intent(this, MyRequestsActivity::class.java)
            requestIntent.putExtra("prefill_equipment_name", name)
            requestIntent.putExtra("prefill_category", category)
            requestIntent.putExtra("open_form", true)
            startActivity(requestIntent)
        }

        findViewById<View>(R.id.btnBackToCatalog).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btnNavCatalog).setOnClickListener {
            val homeIntent = Intent(this, HomeActivity::class.java)
            homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(homeIntent)
        }

        findViewById<View>(R.id.btnNavRequests).setOnClickListener {
            startActivity(Intent(this, MyRequestsActivity::class.java))
        }

        findViewById<View>(R.id.btnNavProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupImageGallery() {
        val imageView = findViewById<ImageView>(R.id.ivEquipmentImage)
        val prevButton = findViewById<Button>(R.id.btnImagePrev)
        val nextButton = findViewById<Button>(R.id.btnImageNext)
        val imageCounter = findViewById<TextView>(R.id.tvImageCounter)

        // Load first image
        loadImage(imageView, 0)
        updateImageCounter(imageCounter)

        prevButton.setOnClickListener {
            currentImageIndex = if (currentImageIndex > 0) currentImageIndex - 1 else images.size - 1
            loadImage(imageView, currentImageIndex)
            updateImageCounter(imageCounter)
        }

        nextButton.setOnClickListener {
            currentImageIndex = (currentImageIndex + 1) % images.size
            loadImage(imageView, currentImageIndex)
            updateImageCounter(imageCounter)
        }
    }

    private fun loadImage(imageView: ImageView, index: Int) {
        if (index in images.indices) {
            Glide.with(this)
                .load(images[index])
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(imageView)
        }
    }

    private fun updateImageCounter(textView: TextView) {
        textView.text = "${currentImageIndex + 1}/${images.size}"
    }

    private fun fetchBorrowedDates(equipmentName: String) {
        Thread {
            try {
                val token = getSharedPreferences("unigear_auth", MODE_PRIVATE)
                    .getString("token", null)
                    ?: return@Thread

                val result = AuthApiClient.getRequests(token)
                
                if (result.success) {
                    val dates = mutableSetOf<String>()
                    result.requests.forEach { request ->
                        if (request.equipmentName == equipmentName && 
                            (request.status == "APPROVED" || request.status == "COMPLETED")) {
                            // Add dates between createdAt and updatedAt as borrowed dates
                            dates.add(request.createdAt)
                        }
                    }
                    borrowedDates = dates
                    
                    runOnUiThread {
                        setupCalendar()
                    }
                }
            } catch (e: Exception) {
                // Silently fail - continue without borrowed dates
            }
        }.start()
    }

    private fun setupCalendar() {
        val calendarGrid = findViewById<GridLayout>(R.id.calendarGrid)
        val calendarTitle = findViewById<TextView>(R.id.tvCalendarMonth)
        val prevMonthBtn = findViewById<Button>(R.id.btnPrevMonth)
        val nextMonthBtn = findViewById<Button>(R.id.btnNextMonth)

        // Update title
        calendarTitle.text = DateFormat.format("MMMM yyyy", currentCalendarMonth)

        // Generate calendar days
        val daysInMonth = currentCalendarMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = Calendar.getInstance().apply {
            set(currentCalendarMonth.get(Calendar.YEAR), currentCalendarMonth.get(Calendar.MONTH), 1)
        }.get(Calendar.DAY_OF_WEEK) - 1

        calendarGrid.removeAllViews()

        // Add empty cells for days before month starts
        repeat(firstDayOfWeek) {
            val emptyCell = TextView(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 60
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }
            }
            calendarGrid.addView(emptyCell)
        }

        // Add days of month
        for (day in 1..daysInMonth) {
            val dayView = TextView(this).apply {
                text = day.toString()
                textSize = 16f
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                setPadding(8, 8, 8, 8)
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 60
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }

                val currentDateStr = String.format("%04d-%02d-%02d", 
                    currentCalendarMonth.get(Calendar.YEAR),
                    currentCalendarMonth.get(Calendar.MONTH) + 1,
                    day)

                // Check if date is borrowed
                val isBorrowed = borrowedDates.any { it.contains(currentDateStr) }
                
                if (isBorrowed) {
                    setBackgroundColor(getColor(android.R.color.holo_red_light))
                    setTextColor(getColor(android.R.color.white))
                } else {
                    setBackgroundColor(getColor(android.R.color.darker_gray))
                    setTextColor(getColor(android.R.color.white))
                }
            }
            calendarGrid.addView(dayView)
        }

        prevMonthBtn.setOnClickListener {
            currentCalendarMonth.add(Calendar.MONTH, -1)
            setupCalendar()
        }

        nextMonthBtn.setOnClickListener {
            currentCalendarMonth.add(Calendar.MONTH, 1)
            setupCalendar()
        }
    }
}
