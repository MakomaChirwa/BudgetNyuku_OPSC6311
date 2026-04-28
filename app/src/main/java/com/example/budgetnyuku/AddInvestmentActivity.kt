package com.example.budgetnyuku

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class AddInvestmentActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var userId: Int = -1

    private lateinit var etAmount: EditText
    private lateinit var spinnerType: Spinner
    private lateinit var etPlatform: EditText
    private lateinit var etUnits: EditText
    private lateinit var etPricePerUnit: EditText
    private lateinit var etDescription: EditText
    private lateinit var tvPurchaseDate: TextView
    private lateinit var btnAddInvestment: Button
    private lateinit var switchRecurring: Switch
    private lateinit var layoutRecurring: LinearLayout
    private lateinit var spinnerFrequency: Spinner

    private var selectedType: String = ""
    private var selectedFrequency: String = "Monthly"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_investment)

        db = DatabaseHelper(this)
        userId = intent.getIntExtra("USER_ID", -1)

        if (userId == -1) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupInvestmentTypes()
        setupDatePicker()
        setupRecurringToggle()
        setupClickListeners()
    }

    private fun initViews() {
        etAmount = findViewById(R.id.etAmount)
        spinnerType = findViewById(R.id.spinnerType)
        etPlatform = findViewById(R.id.etPlatform)
        etUnits = findViewById(R.id.etUnits)
        etPricePerUnit = findViewById(R.id.etPricePerUnit)
        etDescription = findViewById(R.id.etDescription)
        tvPurchaseDate = findViewById(R.id.tvPurchaseDate)
        btnAddInvestment = findViewById(R.id.btnAddInvestment)
        switchRecurring = findViewById(R.id.switchRecurring)
        layoutRecurring = findViewById(R.id.layoutRecurring)
        spinnerFrequency = findViewById(R.id.spinnerFrequency)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        tvPurchaseDate.text = dateFormat.format(Date())

        // Setup frequency spinner
        val frequencies = listOf("Daily", "Weekly", "Monthly", "Quarterly", "Yearly")
        val freqAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, frequencies)
        freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFrequency.adapter = freqAdapter
        spinnerFrequency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                selectedFrequency = frequencies[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Calculate total when units or price changes
        etUnits.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) { calculateTotal() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        etPricePerUnit.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) { calculateTotal() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun calculateTotal() {
        val units = etUnits.text.toString().toDoubleOrNull() ?: 0.0
        val pricePerUnit = etPricePerUnit.text.toString().toDoubleOrNull() ?: 0.0
        val total = units * pricePerUnit

        if (total > 0) {
            etAmount.setText(String.format("%.2f", total))
        }
    }

    private fun setupInvestmentTypes() {
        val investmentTypes = listOf(
            "Stocks", "Mutual Funds", "Real Estate", "Cryptocurrency",
            "Bonds", "ETF", "Fixed Deposit", "Gold/Silver",
            "Business Investment", "Peer-to-Peer Lending", "REIT", "Other"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, investmentTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter

        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                selectedType = investmentTypes[position]
                updateUIForInvestmentType(selectedType)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun updateUIForInvestmentType(type: String) {
        when (type) {
            "Stocks", "ETF" -> {
                etPlatform.hint = "Broker/Platform (e.g., Robinhood, eToro)"
                etUnits.hint = "Number of Shares"
                etPricePerUnit.hint = "Price per Share"
                etUnits.visibility = android.view.View.VISIBLE
                etPricePerUnit.visibility = android.view.View.VISIBLE
            }
            "Mutual Funds" -> {
                etPlatform.hint = "Fund House (e.g., Vanguard, Fidelity)"
                etUnits.hint = "Number of Units"
                etPricePerUnit.hint = "NAV per Unit"
                etUnits.visibility = android.view.View.VISIBLE
                etPricePerUnit.visibility = android.view.View.VISIBLE
            }
            "Cryptocurrency" -> {
                etPlatform.hint = "Exchange (e.g., Binance, Coinbase)"
                etUnits.hint = "Amount in Crypto"
                etPricePerUnit.hint = "Price per Coin/Token"
                etUnits.visibility = android.view.View.VISIBLE
                etPricePerUnit.visibility = android.view.View.VISIBLE
            }
            "Real Estate" -> {
                etPlatform.hint = "Property Address"
                etUnits.visibility = android.view.View.GONE
                etPricePerUnit.visibility = android.view.View.GONE
            }
            else -> {
                etPlatform.hint = "Platform/Provider"
                etUnits.visibility = android.view.View.GONE
                etPricePerUnit.visibility = android.view.View.GONE
            }
        }
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        tvPurchaseDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(year, month, day)
                tvPurchaseDate.text = dateFormat.format(calendar.time)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setupRecurringToggle() {
        switchRecurring.setOnCheckedChangeListener { _, isChecked ->
            layoutRecurring.visibility = if (isChecked) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun setupClickListeners() {
        btnAddInvestment.setOnClickListener {
            saveInvestment()
        }
    }

    private fun saveInvestment() {
        val amountText = etAmount.text.toString()
        if (amountText.isEmpty()) {
            Toast.makeText(this, "Please enter investment amount", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val platform = etPlatform.text.toString().trim()
        if (platform.isEmpty()) {
            Toast.makeText(this, "Please enter platform/provider", Toast.LENGTH_SHORT).show()
            return
        }

        val units = etUnits.text.toString().toDoubleOrNull()
        val pricePerUnit = etPricePerUnit.text.toString().toDoubleOrNull()
        val description = etDescription.text.toString().trim()
        val date = tvPurchaseDate.text.toString()
        val isRecurring = switchRecurring.isChecked
        val frequency = if (isRecurring) selectedFrequency else null

        val investmentDetails = buildInvestmentDetails(units, pricePerUnit)

        val transaction = Transaction(
            userId = userId,
            amount = amount,
            category = selectedType,
            subcategory = platform,
            description = description + investmentDetails,
            date = date,
            type = "Investment",
            isRecurring = isRecurring,
            recurringFrequency = frequency
        )

        if (db.addTransaction(transaction)) {
            val message = if (isRecurring) {
                "Recurring investment added successfully! 📈\nFrequency: $frequency"
            } else {
                "Investment added successfully! 📈"
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            setResult(RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "Failed to add investment", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildInvestmentDetails(units: Double?, pricePerUnit: Double?): String {
        val details = StringBuilder()
        if (units != null && units > 0) {
            details.append("\nUnits: $units")
        }
        if (pricePerUnit != null && pricePerUnit > 0) {
            details.append("\nPrice per unit: ₱${String.format("%.2f", pricePerUnit)}")
        }
        return details.toString()
    }
}