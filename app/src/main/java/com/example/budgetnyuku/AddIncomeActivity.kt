package com.example.budgetnyuku

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class AddIncomeActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var userId: Int = -1

    private lateinit var etAmount: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etSource: EditText
    private lateinit var etDescription: EditText
    private lateinit var tvDate: TextView
    private lateinit var btnSaveIncome: Button
    private lateinit var btnAddCategory: Button

    private val incomeCategories = mutableListOf<String>()
    private var selectedCategory: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_income)

        db = DatabaseHelper(this)
        userId = intent.getIntExtra("USER_ID", -1)

        if (userId == -1) {
            Toast.makeText(this, "Error: User session not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        loadIncomeCategories()
        setupDatePicker()
        setupClickListeners()
    }

    private fun initViews() {
        etAmount = findViewById(R.id.etAmount)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        etSource = findViewById(R.id.etSource)
        etDescription = findViewById(R.id.etDescription)
        tvDate = findViewById(R.id.tvDate)
        btnSaveIncome = findViewById(R.id.btnSaveIncome)
        btnAddCategory = findViewById(R.id.btnAddCategory)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        tvDate.text = dateFormat.format(Date())
    }

    private fun loadIncomeCategories() {
        incomeCategories.clear()
        // Add default income categories
        incomeCategories.addAll(listOf(
            "Salary", "Freelance", "Business", "Investment Returns",
            "Gift", "Bonus", "Rental Income", "Dividends",
            "Interest", "Refund", "Sale", "Other Income"
        ))

        // Add custom categories from database
        val customCategories = db.getIncomeCategories(userId)
        incomeCategories.addAll(customCategories)

        setupCategorySpinner()
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, incomeCategories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategory = incomeCategories[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        tvDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(year, month, day)
                tvDate.text = dateFormat.format(calendar.time)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setupClickListeners() {
        btnAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }

        btnSaveIncome.setOnClickListener {
            saveIncome()
        }
    }

    private fun showAddCategoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_income_category, null)
        val etCategoryName = dialogView.findViewById<EditText>(R.id.etCategoryName)

        AlertDialog.Builder(this)
            .setTitle("Add Income Category")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog: DialogInterface, _: Int ->
                val categoryName = etCategoryName.text.toString().trim()
                if (categoryName.isNotEmpty()) {
                    if (db.addIncomeCategory(userId, categoryName)) {
                        Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show()
                        loadIncomeCategories()
                    } else {
                        Toast.makeText(this, "Failed to add category", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please enter category name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveIncome() {
        val amountText = etAmount.text.toString()
        if (amountText.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val source = etSource.text.toString().trim()
        if (source.isEmpty()) {
            Toast.makeText(this, "Please enter income source", Toast.LENGTH_SHORT).show()
            return
        }

        val description = etDescription.text.toString().trim()
        val date = tvDate.text.toString()

        val transaction = Transaction(
            userId = userId,
            amount = amount,
            category = selectedCategory,
            subcategory = source,
            description = description,
            date = date,
            type = "Income"
        )

        if (db.addTransaction(transaction)) {
            Toast.makeText(this, "Income added successfully! 💰", Toast.LENGTH_LONG).show()
            setResult(RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "Failed to add income", Toast.LENGTH_SHORT).show()
        }
    }
}