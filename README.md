#  Pockitify – Personal Finance Manager

**Pockitify** is a native Android personal finance management application designed to make tracking income, expenses, account balances, and everyday financial activities simple and organized.

The application helps users manage their finances in one place through an intuitive interface, offline data storage, financial reports, and secure PIN authentication.

Pockitify started as a personal project developed through **AI-assisted development (Vibe Coding)**, using Google AI Studio and GitHub Copilot.

**Current Version:** 1.0 – Personal Edition  
**Upcoming Version:** 2.0 – Smart Finance Manager  
**Platform:** Android  
**Development Status:** Version 1.0 completed; Version 2.0 planned

---

## 📱 Version 1.0 – Personal Edition

Version 1.0 is the first completed version of Pockitify, designed for personal financial tracking and management.

###  Key Features

#### 🏠 Dashboard
- Overview of total available balances.
- Account-wise balance information.
- Income and expense summaries.
- Quick access to financial transactions.

#### 🏦 Account Management
- Preconfigured Allied Bank (ABL) and United Bank (UBL) accounts.
- Separate wallet account for cash management.
- Individual account balance tracking.
- Money transfers between accounts.

#### 💸 Income & Expense Tracking
- Record income and expenses.
- Assign transactions to categories.
- Track spending across different accounts.
- View transaction history.
- Manage financial records.

#### 📊 Financial Reports
- Monthly income and expense summaries.
- Category-wise spending information.
- Transaction history reports.
- PDF export functionality.
- Manual monthly reset option.

#### 🔐 Security
- Secure 6-digit PIN authentication.
- PIN upgrade functionality.
- Manual app locking and logout.
- Automatic locking when the application goes into the background.
- Locally stored financial information.

#### ⚙️ Additional Features
- Custom transaction categories.
- In-app notifications.
- User preferences and settings.
- Offline financial management.
- Modern user interface built with Jetpack Compose.

---

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Kotlin | Native Android development |
| Jetpack Compose | Modern declarative UI |
| Room Database | Local financial data storage |
| DataStore | Preferences and authentication state |
| Android SDK | Android application functionality |
| Gradle | Dependency management and builds |
| Git & GitHub | Version control |
| GitHub Actions | Cloud-based APK generation |
| Google AI Studio | Initial AI-assisted development |
| GitHub Copilot | Code improvements, debugging, and feature completion |
| Visual Studio Code | Development environment |

---

## 🤖 Development Approach – Vibe Coding

Pockitify was developed using an **AI-assisted software development approach**, commonly referred to as *Vibe Coding*.

The initial application was built using Google AI Studio, while GitHub Copilot was later used in Visual Studio Code for debugging, improvements, and feature completion.

The development workflow included:

1. Planning application features and functionality.
2. Writing structured prompts to guide AI development tools.
3. Generating and improving application code.
4. Reviewing implementations and fixing errors.
5. Using GitHub Actions to build Android APK files.
6. Testing the application on a physical Android device.

This project demonstrates how AI-assisted development tools can support the creation of a functional native Android application.

---

## 🚀 Version 2.0 – Smart Finance Manager (Planned)

Version 2.0 will focus on transforming Pockitify from a personal-use application into a customizable and intelligent financial management solution for a wider audience.

### 🏦 1. Dynamic Bank & Account Management

Unlike Version 1.0, which uses two predefined bank accounts, Version 2.0 will allow users to configure their own financial accounts.

**Planned Features:**
- Select banks from a supported list.
- Add multiple bank accounts.
- Support digital wallets and cash accounts.
- Enter opening balances when creating accounts.
- Add, edit, and manage financial accounts.
- Display bank-specific account cards and themes.
- View combined balances across all accounts.

### 💰 2. Savings Management

Version 2.0 will introduce dedicated savings features to help users build better financial habits.

**Planned Features:**
- Create savings goals.
- Set monthly savings targets.
- Track savings contributions.
- Monitor progress toward financial goals.
- Compare income, expenses, and savings.
- Receive suggestions for improving savings habits.

### 🧠 3. AI-Powered Financial Assistant

One of the major planned additions to Version 2.0 is an **AI Financial Assistant**.

The assistant will help users understand their financial activities by analyzing their recorded transactions and providing personalized insights.

**Planned Capabilities:**

- **Expense Analysis:** Calculate total spending over a selected period.
- **Income Analysis:** Summarize monthly and yearly income.
- **Savings Insights:** Estimate savings based on recorded income, expenses, and account activity.
- **Category Analysis:** Identify the categories where users spend the most.
- **Spending Comparisons:** Compare spending across months.
- **Budget Guidance:** Suggest ways to manage spending.
- **Savings Recommendations:** Help users identify opportunities to save more.
- **Natural Language Interaction:** Answer financial questions through a conversational interface.

**Example Questions:**

> 💬 "How much did I spend this month?"

> 💬 "Which category did I spend the most on?"

> 💬 "How much money did I save in September?"

> 💬 "Compare my expenses for August and September."

> 💬 "How can I reduce my unnecessary expenses?"

> 💬 "How much can I realistically save next month?"

The goal is to make financial information easier to understand without requiring users to manually analyze their transaction records.

### 📈 4. Advanced Financial Analytics

Planned improvements include:

- Interactive spending charts.
- Monthly and yearly financial comparisons.
- Category-wise expense breakdowns.
- Income and savings trends.
- Financial summaries and visual reports.
- Improved budget tracking.

### 🎨 5. Personalization & User Experience

- Flexible account configuration.
- Improved financial dashboard customization.
- Personalized financial goals.
- Additional account themes and visual improvements.
- Better support for different user preferences.

### 🔒 6. Privacy & Data Security

Protecting financial information will remain a priority.

Planned considerations include:

- Secure storage of sensitive financial information.
- Clear user consent before sharing financial data with an AI service.
- Minimizing financial information sent to external AI providers.
- User control over AI-powered features.
- Maintaining offline access to core financial management features where possible.

---

## 🗺️ Development Roadmap

| Feature | Version 1.0 | Version 2.0 |
|---|---|---|
| Income & expense tracking | ✅ Implemented | Planned enhancements |
| ABL and UBL accounts | ✅ Implemented | Replaced by customizable accounts |
| Wallet management | ✅ Implemented | Expanded wallet support |
| Account transfers | ✅ Implemented | Planned enhancements |
| Transaction history | ✅ Implemented | Planned enhancements |
| Monthly reports | ✅ Implemented | Advanced analytics planned |
| PDF export | ✅ Implemented | Planned enhancements |
| 6-digit PIN security | ✅ Implemented | Continued improvements |
| Automatic app locking | ✅ Implemented | Continued improvements |
| Custom bank selection | — | 📅 Planned |
| Multiple configurable accounts | — | 📅 Planned |
| Savings goals | — | 📅 Planned |
| AI Financial Assistant | — | 📅 Planned |
| Personalized financial recommendations | — | 📅 Planned |
| Advanced visual analytics | — | 📅 Planned |

*Version 2.0 features are proposed and may change during development.*

---

## 📦 Installation

Version 1.0 is currently a personal development build for Android devices.

To obtain a debug APK from GitHub Actions:

1. Open the repository's **Actions** tab.
2. Select a successful Android build workflow.
3. Find the **Artifacts** section.
4. Download the `Pockitify-Debug-APK` artifact.
5. Extract the ZIP archive.
6. Transfer the APK to an Android phone.
7. Install the APK and launch Pockitify.

**Note:** Debug APKs are intended for development and testing. Availability of workflow artifacts may be limited, and a debug build is not equivalent to a signed production release.

---

## 🔏 Privacy

Pockitify Version 1.0 is designed to manage financial records locally without requiring a direct connection to banking systems.

Financial information is entered and managed manually by the user.

Future AI-powered functionality will require additional privacy and security considerations, especially when processing personal financial records.

---

## 🎯 Project Vision

The long-term vision of Pockitify is to become an intelligent personal finance companion that goes beyond recording transactions.

The goal is to help users:

- Understand where their money goes.
- Manage multiple financial accounts.
- Develop better spending habits.
- Build and track savings.
- Receive meaningful financial insights.
- Make more informed financial decisions.

**Track Smarter. Spend Wisely. Save Better.**

---

## 👨‍💻 Developer

**Ali Sultan**

Software Engineering Student | Android Development | AI-Assisted Development

**GitHub:** [alishah18105](https://github.com/alishah18105)

**Repository:** [Pockitify-App](https://github.com/alishah18105/Pockitify-App)

---

## 📄 License

No open-source license has been specified for this repository. All rights remain reserved unless a license is added.

---

**Pockitify Version 1.0 – Personal Edition**

*Built with Kotlin, Jetpack Compose, Google AI Studio, GitHub Copilot, and a vision to make personal finance management simpler.*