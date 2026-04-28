First step for set up !

1. Clone the Repository
bash
git clone https://github.com//BudgetNyuku_OPSC6331
cd BudgetNyuku
2. Open in Android Studio
Launch Android Studio

Select File → Open

Navigate to the project folder

Click OK

3. Sync Gradle
Wait for automatic Gradle sync

If prompted, click Sync Now

Ensure all dependencies download successfully

4. Configure Android SDK
bash
 Check SDK location
./gradlew check

 If SDK not found, create local.properties
echo "sdk.dir=/path/to/Android/Sdk" > local.properties
5. Build the Project
bash
 Clean build
./gradlew clean

 Build debug APK
./gradlew assembleDebug

 Build release APK
./gradlew assembleRelease
6. Run the App
Connect an Android device via USB (enable USB debugging)

Or start an Android Virtual Device (AVD)

Click the Run button (▶) in Android Studio

Select your device/emulator


First Launch
Welcome Screen

App logo and name displayed

Two options: Sign In or Register

Create Account

text
Tap "Register"
Enter: Full Name
Enter: Email Address
Enter: Password
Confirm Password
Tap "Register"
Login

text
Enter registered email
Enter password
Tap "Sign In"
Dashboard Overview
The dashboard is your command center showing:

Welcome message with your name

Monthly Summary Card: Total spent, budget status

Action Cards: Quick access to all features

Recent Expenses: Last 5 transactions

Adding Income
text
Dashboard → Tap "Add Income" card
├── Enter amount (₱)
├── Select category (Salary, Freelance, etc.)
├── Add source/platform
├── Optional: Add description
├── Select date
└── Tap "Add Income"
Income Categories Available:

Salary, Freelance, Business

Investment Returns, Gift, Bonus

Rental Income, Dividends

Interest, Refund, Sale

Custom categories (add your own)

Adding Expenses
text
Dashboard → Tap "Add Expense" card
├── Select category (create in Categories first)
├── Enter amount (₱)
├── Add description
├── Select date and time
├── Optional: Take photo of receipt
├── Optional: Select photo from gallery
└── Tap "Save Expense"
Adding Investments
text
Dashboard → Tap "Add Investment" card
├── Select investment type
├── Enter platform/broker
├── Enter units/shares
├── Enter price per unit (auto-calculates total)
├── Optional: Enable recurring investment
├── Optional: Select frequency for recurring
├── Add notes
├── Select purchase date
└── Tap "Add Investment"
Investment Types Supported:

Stocks, Mutual Funds, Real Estate

Cryptocurrency, Bonds, ETF

Fixed Deposit, Gold/Silver

Business Investment, P2P Lending, REIT

Managing Categories
text
Dashboard → Tap "Categories" card
├── View existing categories
├── Tap + FAB to add new category
│   ├── Enter category name
│   └── Select color
├── Tap pen to edit category
└── Tap bin to delete category
Viewing Reports
Expense List
text
Dashboard → Tap "View Expenses" card
├── Select date range
├── View filtered expenses
├── Tap any expense for details
└── Option to delete expense
Category Report
text
Dashboard → Tap "Category Report" card
├── Select date range
├── View spending by category
├── Progress bars show relative spending
└── Total amount displayed
All Transactions
text
Dashboard → Tap "Transactions" card
├── Filter by tabs (All/Income/Expense/Investment)
├── Select date range
├── View all transactions
└── Tap for details/delete
Budget Settings
text
Dashboard → Tap "Budget Settings" card
├── Enter monthly budget amount
├── Tap "Save Settings"
└── Dashboard shows budget status
Currency Settings
text
Dashboard → Tap "Currency Settings" card

├── Select preferred currency
├── Tap "Save Currency"
└── All amounts update automatically

Manual Testing Checklist
Authentication:

Register new user

Login with valid credentials

Login with invalid credentials

Logout functionality

Income:

Add income with all fields

Add income with minimum fields

Add custom category

View income in transactions

Expense:

Add expense with photo

Add expense without photo

View expense details

Delete expense

Investment:

Add one-time investment

Add recurring investment

Auto-calculation of totals

Different investment types

Categories:

Create category

Edit category

Delete category

Color selection

Reports:

Filter by date range

Filter by type

View category totals

Progress bars display

Budget:

Set budget

Update budget

Budget status indicators

Currency:

Change currency

All displays update


