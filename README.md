# 🚀 JobPilotAI — Enterprise Job Application Tracker

<p align="center">
  <img src="src/main/resources/images/icon.png" width="120" alt="JobPilotAI Logo" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/JavaFX-21-0082C8?style=for-the-badge&logo=java&logoColor=white"/>
  <img src="https://img.shields.io/badge/SQLite-003B57?style=for-the-badge&logo=sqlite&logoColor=white"/>
  <img src="https://img.shields.io/badge/Apache_POI-D22128?style=for-the-badge&logo=apache&logoColor=white"/>
  <img src="https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white"/>
  <img src="https://img.shields.io/badge/Windows-0078D4?style=for-the-badge&logo=windows&logoColor=white"/>
  <img src="https://img.shields.io/badge/License-MIT-brightgreen?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Version-1.0.0-blue?style=for-the-badge"/>
</p>

<p align="center">
  <strong>JobPilotAI</strong> is a production-ready, enterprise-grade Windows desktop application built in Java 21 + JavaFX. It provides a beautiful, high-performance dashboard for tracking job applications, generating Excel reports, managing session state, and monitoring activity logs — all backed by a local SQLite database with zero cloud dependency.
</p>

---

## ✨ Features

- **Premium Dark / Light Themes** — JetBrains-inspired UI with smooth transitions, glowing accents, and professional card layouts.
- **Full Application Tracking** — Add, edit, delete, and search job applications with coloured status badges (Success, Failed, Pending OTP, Pending CAPTCHA, and more).
- **Excel Report Generation** — One-click Manual, Hourly, and Final reports using Apache POI — bold headers, auto-sized columns, alternating row colours, and a metadata sheet.
- **Session Management** — Automatically saves your session on exit; prompts you to resume or start fresh on next launch.
- **Daily Log Files** — Rotating daily log files + database log viewer with search and level filtering.
- **Windows Notifications** — Native system tray notifications + optional Google Apps Script email alerts.
- **Settings Panel** — Configure themes, resume path, email, report folder, window memory, and more — all persisted in SQLite.
- **Collapsible Sidebar** — Animated sidebar with smooth width animation and icon-only mode.
- **Live Dashboard** — Real-time statistics cards, live clock, progress bar, and system status panel.
- **History View** — Filterable, searchable history with bulk export and delete.
- **Robust Error Handling** — Never crashes; all exceptions are caught, logged, and shown as friendly dialogs.

---

## 🖼 Application Preview

> _Launch the app to see the full premium dark-themed dashboard. Screenshots can be found in the `screenshots/` folder after first run._

---

## 🏗 Architecture

```
MVVM + Repository Pattern + Service Layer + Clean Architecture
```

```
JobPilotAI/
├── app/            → MainApp (Application) + AppLauncher (entry point)
├── config/         → AppConfig (constants), PathConfig (filesystem paths)
├── database/       → DatabaseManager (SQLite connection), DatabaseMigration (versioned schema)
├── excel/          → ExcelReportGenerator (Apache POI)
├── logs/           → AppLogger (daily rotating log files)
├── model/          → JobApplication, Report, LogEntry, SavedSession
├── repository/     → DAO layer (ApplicationRepository, ReportRepository, etc.)
├── service/        → Business logic (ApplicationService, ReportService, etc.)
├── viewmodel/      → Observable properties (DashboardViewModel, ApplicationViewModel, etc.)
├── controller/     → FXML controllers (MainController, DashboardController, etc.)
├── ui/             → Custom components (SessionDialog)
└── notification/   → NotificationService (Windows tray + GAS email)
```

---

## 🗃 Database Schema

| Table | Purpose |
|---|---|
| `users` | Admin account |
| `settings` | Key/value settings store |
| `applications` | Job application records |
| `reports` | Generated report metadata |
| `logs` | In-app log entries |
| `saved_sessions` | Session state snapshots |

---

## 🎨 Design System

### Dark Theme
| Token | Value |
|---|---|
| Primary Background | `#0F172A` |
| Secondary Background | `#1E293B` |
| Sidebar | `#111827` |
| Card Background | `#1F2937` |
| Accent | `#3B82F6` |
| Success | `#22C55E` |
| Warning | `#F59E0B` |
| Danger | `#EF4444` |

### Light Theme
| Token | Value |
|---|---|
| Background | `#F8FAFC` |
| Cards | `#FFFFFF` |
| Sidebar | `#E2E8F0` |
| Accent | `#2563EB` |

---

## 🛠 Technology Stack

| Concern | Technology |
|---|---|
| Language | Java 21 (LTS) |
| UI Framework | JavaFX 21 |
| Build Tool | Apache Maven |
| Database | SQLite (xerial/sqlite-jdbc 3.45) |
| Excel | Apache POI 5.2.5 |
| JSON | Jackson 2.17 |
| Logging | `java.util.logging` (daily rotation) |
| Packaging | jpackage (Windows .exe / installer) |
| Notifications | Java AWT SystemTray + Google Apps Script |

---

## 💻 Requirements

- **OS:** Windows 10 / Windows 11 (64-bit)
- **JDK:** Java 21+ (Temurin / OpenJDK recommended)
- **Maven:** 3.9+
- **IDE:** IntelliJ IDEA Community Edition (recommended)

---

## 🚀 How to Build & Run

### Step 1 — Clone the Repository
```bash
git clone https://github.com/HelloWorld-Farhan/JobPilotAI.git
cd JobPilotAI
```

### Step 2 — Build with Maven
```bash
mvn clean compile
```

### Step 3 — Run with JavaFX Plugin
```bash
mvn javafx:run
```

### Step 4 — Package as Fat JAR
```bash
mvn clean package
java -jar target/JobPilotAI-1.0.0.jar
```

### Step 5 — Package as Windows .exe (requires JDK 21 + WiX Toolset)
```bash
mvn clean package
jpackage --input target --name JobPilotAI --main-jar JobPilotAI-1.0.0.jar \
         --main-class com.jobpilotai.app.AppLauncher \
         --type exe --win-shortcut --win-menu \
         --icon src/main/resources/images/icon.png \
         --app-version 1.0.0 --vendor "Farhan Khalid"
```

---

## 📁 Application Data Directory

All data is stored in `~/JobPilotAI/` — no admin rights required:

```
~/JobPilotAI/
├── database/       → SQLite database file
├── reports/        → Generated Excel (.xlsx) reports
├── logs/           → Daily log files (jobpilotai-yyyy-MM-dd.log)
├── resume/         → Resume files
├── settings/       → Additional config
├── screenshots/    → Captured screenshots
├── temp/           → Temporary files
└── backup/         → Backup data
```

---

## 📊 Dashboard Overview

| Panel | Description |
|---|---|
| Dashboard | Live stats cards, clock, system status, progress bar |
| Applications | Full CRUD table with status badges, search, filter |
| History | Read-only history with export and bulk delete |
| Reports | Generate Manual / Hourly / Final Excel reports |
| Settings | All user preferences, paths, themes, notifications |
| Logs | Searchable log viewer with level filtering |
| About | Version, technology stack, credits |

---

## 📈 Excel Reports

Every generated report includes:

- ✅ Bold, navy-blue header row
- ✅ Alternating row shading  
- ✅ Status-coloured cells (green/red/yellow/blue)
- ✅ Auto-sized columns
- ✅ Timestamp in filename (never overwrites)
- ✅ Separate metadata sheet

---

## 🔒 Security

- All SQL uses **prepared statements** — no injection risk
- User input is **validated** before persistence
- Configuration files are stored in the user's home directory
- No external telemetry or data collection

---

## 🗺 Roadmap

| Version | Features |
|---|---|
| **v1.0** ✅ | Core tracker, Excel reports, SQLite, dark/light themes, notifications |
| **v1.1** 🔜 | Playwright browser automation, auto job application |
| **v1.2** 🔜 | AI resume matching, smart suggestions |
| **v2.0** 🔜 | Cloud sync, multi-device support |

---

## 👨‍💻 Author

**Farhan Khalid**  
📧 farhankhalid17968@gmail.com  
🔗 [LinkedIn](https://www.linkedin.com/in/farhan-khalid-117514259/)  
🐙 [GitHub](https://github.com/HelloWorld-Farhan)

---

## 📄 License

```text
MIT License

Copyright (c) 2026 Farhan Khalid

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

## 🌟 Support

If **JobPilotAI** helps you land your dream job, please give it a ⭐ on GitHub!

<p align="center">Made with ❤️ — Windows Desktop · Java 21 · JavaFX</p>
