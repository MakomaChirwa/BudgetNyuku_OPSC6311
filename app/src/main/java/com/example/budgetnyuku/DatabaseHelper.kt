package com.example.budgetnyuku

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "BudgetDB", null, 6) {

    companion object {
        private const val TABLE_USERS = "users"
        private const val TABLE_CATEGORIES = "categories"
        private const val TABLE_EXPENSES = "expenses"
        private const val TABLE_BUDGET_SETTINGS = "budget_settings"
        private const val TABLE_CUSTOM_CATEGORIES = "custom_categories"
        private const val TABLE_TRANSACTIONS = "transactions"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Users table
        db.execSQL("""
            CREATE TABLE $TABLE_USERS(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                email TEXT UNIQUE,
                password TEXT
            )
        """)

        // Categories table (for expenses)
        db.execSQL("""
            CREATE TABLE $TABLE_CATEGORIES(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                name TEXT,
                color TEXT,
                FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id) ON DELETE CASCADE
            )
        """)

        // Expenses table
        db.execSQL("""
            CREATE TABLE $TABLE_EXPENSES(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                category_id INTEGER,
                amount REAL,
                date TEXT,
                start_time TEXT,
                end_time TEXT,
                description TEXT,
                photo BLOB,
                FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id) ON DELETE CASCADE,
                FOREIGN KEY(category_id) REFERENCES $TABLE_CATEGORIES(id) ON DELETE CASCADE
            )
        """)

        // Budget settings table
        db.execSQL("""
            CREATE TABLE $TABLE_BUDGET_SETTINGS(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER UNIQUE,
                monthly_min_goal REAL,
                monthly_max_goal REAL,
                FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id) ON DELETE CASCADE
            )
        """)

        // Custom categories table for income and investment
        db.execSQL("""
            CREATE TABLE $TABLE_CUSTOM_CATEGORIES(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                name TEXT,
                type TEXT,
                color TEXT,
                FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id) ON DELETE CASCADE
            )
        """)

        // Unified transactions table (Income, Investment, etc.)
        db.execSQL("""
            CREATE TABLE $TABLE_TRANSACTIONS(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                amount REAL,
                category TEXT,
                subcategory TEXT,
                description TEXT,
                date TEXT,
                type TEXT,
                is_recurring INTEGER DEFAULT 0,
                recurring_frequency TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id) ON DELETE CASCADE
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CUSTOM_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BUDGET_SETTINGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // User methods
    fun registerUser(name: String, email: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("email", email)
            put("password", password)
        }
        val result = db.insert(TABLE_USERS, null, values)
        return result != -1L
    }

    fun loginUser(email: String, password: String): Int? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id FROM $TABLE_USERS WHERE email=? AND password=?",
            arrayOf(email, password)
        )
        return if (cursor.moveToFirst()) {
            val userId = cursor.getInt(0)
            cursor.close()
            userId
        } else {
            cursor.close()
            null
        }
    }

    fun getUserName(userId: Int): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT name FROM $TABLE_USERS WHERE id=?", arrayOf(userId.toString()))
        return if (cursor.moveToFirst()) {
            val name = cursor.getString(0)
            cursor.close()
            name
        } else {
            cursor.close()
            ""
        }
    }

    // Category methods
    fun addCategory(userId: Int, name: String, color: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_id", userId)
            put("name", name)
            put("color", color)
        }
        val result = db.insert(TABLE_CATEGORIES, null, values)
        return result != -1L
    }

    fun getCategories(userId: Int): List<Category> {
        val categories = mutableListOf<Category>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_CATEGORIES WHERE user_id=? ORDER BY name",
            arrayOf(userId.toString())
        )

        while (cursor.moveToNext()) {
            categories.add(
                Category(
                    id = cursor.getInt(0),
                    userId = cursor.getInt(1),
                    name = cursor.getString(2),
                    color = cursor.getString(3)
                )
            )
        }
        cursor.close()
        return categories
    }

    fun updateCategory(categoryId: Int, name: String, color: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("color", color)
        }
        val result = db.update(TABLE_CATEGORIES, values, "id=?", arrayOf(categoryId.toString()))
        return result > 0
    }

    fun deleteCategory(categoryId: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_CATEGORIES, "id=?", arrayOf(categoryId.toString()))
        return result > 0
    }

    // Expense methods
    fun addExpense(expense: Expense): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_id", expense.userId)
            put("category_id", expense.categoryId)
            put("amount", expense.amount)
            put("date", expense.date)
            put("start_time", expense.startTime)
            put("end_time", expense.endTime)
            put("description", expense.description)
            if (expense.photo != null) {
                put("photo", expense.photo)
            }
        }
        val result = db.insert(TABLE_EXPENSES, null, values)
        return result != -1L
    }

    fun getExpensesByDateRange(userId: Int, startDate: String, endDate: String): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            """SELECT e.*, c.name as category_name, c.color as category_color 
               FROM $TABLE_EXPENSES e
               JOIN $TABLE_CATEGORIES c ON e.category_id = c.id
               WHERE e.user_id=? AND e.date BETWEEN ? AND ?
               ORDER BY e.date DESC, e.start_time DESC""",
            arrayOf(userId.toString(), startDate, endDate)
        )

        while (cursor.moveToNext()) {
            expenses.add(
                Expense(
                    id = cursor.getInt(0),
                    userId = cursor.getInt(1),
                    categoryId = cursor.getInt(2),
                    amount = cursor.getDouble(3),
                    date = cursor.getString(4),
                    startTime = cursor.getString(5),
                    endTime = cursor.getString(6),
                    description = cursor.getString(7),
                    photo = cursor.getBlob(8),
                    categoryName = cursor.getString(9),
                    categoryColor = cursor.getString(10)
                )
            )
        }
        cursor.close()
        return expenses
    }

    fun getCategoryTotals(userId: Int, startDate: String, endDate: String): List<CategoryTotal> {
        val totals = mutableListOf<CategoryTotal>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            """SELECT c.id, c.name, c.color, COALESCE(SUM(e.amount), 0) as total
               FROM $TABLE_CATEGORIES c
               LEFT JOIN $TABLE_EXPENSES e ON c.id = e.category_id 
                   AND e.user_id=? AND e.date BETWEEN ? AND ?
               WHERE c.user_id=?
               GROUP BY c.id
               ORDER BY total DESC""",
            arrayOf(userId.toString(), startDate, endDate, userId.toString())
        )

        while (cursor.moveToNext()) {
            val total = cursor.getDouble(3)
            if (total > 0) {
                totals.add(
                    CategoryTotal(
                        categoryId = cursor.getInt(0),
                        categoryName = cursor.getString(1),
                        categoryColor = cursor.getString(2),
                        totalAmount = total
                    )
                )
            }
        }
        cursor.close()
        return totals
    }

    fun deleteExpense(expenseId: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_EXPENSES, "id=?", arrayOf(expenseId.toString()))
        return result > 0
    }

    // Budget settings methods
    fun saveBudgetSettings(userId: Int, minGoal: Double, maxGoal: Double): Boolean {
        val db = writableDatabase

        val cursor = db.rawQuery(
            "SELECT id FROM $TABLE_BUDGET_SETTINGS WHERE user_id=?",
            arrayOf(userId.toString())
        )

        val result = if (cursor.moveToFirst()) {
            val values = ContentValues().apply {
                put("monthly_min_goal", minGoal)
                put("monthly_max_goal", maxGoal)
            }
            cursor.close()
            db.update(TABLE_BUDGET_SETTINGS, values, "user_id=?", arrayOf(userId.toString())) > 0
        } else {
            cursor.close()
            val values = ContentValues().apply {
                put("user_id", userId)
                put("monthly_min_goal", minGoal)
                put("monthly_max_goal", maxGoal)
            }
            db.insert(TABLE_BUDGET_SETTINGS, null, values) != -1L
        }
        return result
    }

    fun getBudgetSettings(userId: Int): Pair<Double, Double>? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT monthly_min_goal, monthly_max_goal FROM $TABLE_BUDGET_SETTINGS WHERE user_id=?",
            arrayOf(userId.toString())
        )
        return if (cursor.moveToFirst()) {
            val minGoal = cursor.getDouble(0)
            val maxGoal = cursor.getDouble(1)
            cursor.close()
            Pair(minGoal, maxGoal)
        } else {
            cursor.close()
            null
        }
    }

    fun getMonthlyTotal(userId: Int, yearMonth: String): Double {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COALESCE(SUM(amount), 0) FROM $TABLE_EXPENSES WHERE user_id=? AND substr(date,1,7)=?",
            arrayOf(userId.toString(), yearMonth)
        )
        return if (cursor.moveToFirst()) {
            val total = cursor.getDouble(0)
            cursor.close()
            total
        } else {
            cursor.close()
            0.0
        }
    }

    // Income/Investment Category methods
    fun addIncomeCategory(userId: Int, categoryName: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_id", userId)
            put("name", categoryName)
            put("type", "Income")
            put("color", "#4CAF50")
        }
        val result = db.insert(TABLE_CUSTOM_CATEGORIES, null, values)
        return result != -1L
    }

    fun getIncomeCategories(userId: Int): List<String> {
        val categories = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT name FROM $TABLE_CUSTOM_CATEGORIES WHERE user_id=? AND type='Income' ORDER BY name",
            arrayOf(userId.toString())
        )
        while (cursor.moveToNext()) {
            categories.add(cursor.getString(0))
        }
        cursor.close()
        return categories
    }

    fun addInvestmentCategory(userId: Int, categoryName: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_id", userId)
            put("name", categoryName)
            put("type", "Investment")
            put("color", "#FF9800")
        }
        val result = db.insert(TABLE_CUSTOM_CATEGORIES, null, values)
        return result != -1L
    }

    fun getInvestmentCategories(userId: Int): List<String> {
        val categories = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT name FROM $TABLE_CUSTOM_CATEGORIES WHERE user_id=? AND type='Investment' ORDER BY name",
            arrayOf(userId.toString())
        )
        while (cursor.moveToNext()) {
            categories.add(cursor.getString(0))
        }
        cursor.close()
        return categories
    }

    // Transaction methods
    fun addTransaction(transaction: Transaction): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_id", transaction.userId)
            put("amount", transaction.amount)
            put("category", transaction.category)
            put("subcategory", transaction.subcategory)
            put("description", transaction.description)
            put("date", transaction.date)
            put("type", transaction.type)
            put("is_recurring", if (transaction.isRecurring) 1 else 0)
            put("recurring_frequency", transaction.recurringFrequency)
        }
        val result = db.insert(TABLE_TRANSACTIONS, null, values)
        return result != -1L
    }
}