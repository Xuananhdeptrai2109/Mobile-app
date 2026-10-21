# Project Name: SplitSense (Expense Tracker & Group Splitter)

> **Course:** Mobile Application Development (INT1449) · PTIT · Instructor: Dr. Hung N. Dang
>
> *One-sentence pitch: A 2-in-1 mobile application that helps users manage personal finances, track saving goals, and seamlessly split group expenses after trips.*

👋 **New here? Read first:** [`docs/student-guide.md`](docs/student-guide.md) · 📜 Brief & grading: [`INSTRUCTION.md`](INSTRUCTION.md) · 🚀 Workflow: [`GETTING_STARTED.md`](GETTING_STARTED.md)

---

## 1. Team (mandatory)

| # | Full name | Student ID | Class | GitHub | Role |
|:-:|-----------|:----------:|:-----:|--------|------|
| 1 | Đặng Xuân Anh | D23DCDT006 | E23HTTT03 | Xuananhdeptrai2109 | Mobile Dev / UIUX |
| 2 | Nguyễn Văn Hiếu | B23DCCE032 | E23HTTT03 | hieusieucappro2k5 | Mobile Dev / Database|
| 3 | Nguyễn Minh Hùng | B23DCCE041 | E23HTTT03 | Saahn-Uzaal| Mobile / SEC - Test |

**Topic:** Offline-first personal expense tracker & Group bill splitter

---

## 2. Problem & Idea (mandatory)

- **Problem:** People often have to use multiple apps: one for personal daily expenses (like MoneyLover) and another for group trips (like Splitwise). Managing group trips often leads to confusing debts, manual calculations, and the hassle of manually re-entering trip expenses into personal trackers.
- **Our idea:** An integrated app that tracks personal daily expenses (with saving goals) and includes a dedicated module for group trips. Users manually input their initial wallet balances and transactions. For group trips, users record shared expenses, the app calculates simplified debts, and smoothly syncs the user's actual trip spending back to their personal expense tracker.
- **Target users:** Young adults, travelers, students, and flatmates who frequently go on group trips and want to maintain personal budget goals clearly.
- **What makes it non-trivial:** Implementing a debt simplification algorithm (minimizing the number of transactions to settle debts), handling offline-first local data for personal records, and flexibly syncing group settlement data into the local personal database.
- **Out of scope:** Linking real bank accounts, direct bank transfers within the app, OCR for receipt scanning.

Full proposal: [`docs/proposal.md`](docs/proposal.md)

---

## 3. Screenshots & Features

| Home & Wallets | Saving Goals | Trip Dashboard | Settlement |
|:---:|:---:|:---:|:---:|
| *(image)* | *(image)* | *(image)* | *(image)* |


- [x] **Feature A - Personal Finance:** Manually input wallet balances, track daily income/expenses, and set saving goals with progress tracking.
- [x] **Feature B - Group Splitter:** Create trips, add members, log shared bills (who paid, who benefits), and auto-calculate simplified final debts.
- [x] **Feature C - Flexible Integration:** After a trip ends, the app asks if the user wants to log their total trip expense into their personal wallet automatically.
- [x] **Offline:** Personal expense records, wallets, saving goals, and local trip drafts are available without network.
- [x] **UI states:** Loading / Success / Empty / Error with retry on every data screen

---

## 4. Architecture

```mermaid
graph TD
    V["Views / Screens (Jetpack Compose)"] <-->|state (StateFlow) ↓ events ↑| VM["ViewModels"]
    VM --> R["Repositories"]
    R --> API["Remote data source (Retrofit / OkHttp)"]
    R --> DB["Local data source (Room DB / Keystore)"]
    API <-->|HTTP/JSON| BE[("Spring Boot Backend (:8081) / Mock API (:3000)")]
```

| Aspect | Our choice | Why (one line) |
|--------|-----------|----------------|
| Framework | **Kotlin + Jetpack Compose** | Modern declarative UI, officially recommended for Android development. |
| State management | **ViewModel + StateFlow** | Reactive, lifecycle-aware state handling with unidirectional data flow. |
| HTTP client | **Retrofit 2 + OkHttp 3** | Industry standard, robust interceptors for JWT auth and logging. |
| Local storage | **Room (SQLite) + Android Keystore** | Robust offline-first persistence with hardware-backed encryption for secrets. |

Details: [`docs/architecture.md`](docs/architecture.md) · UI/UX: [`docs/ui-design.md`](docs/ui-design.md)

---

## 5. Quick Start

### 1️⃣ Start Backend

**Option A — Real Backend (Spring Boot 3 + MySQL):**
```bash
cd backend
cp .env.example .env    # Configure your MySQL credentials
./gradlew bootRun       # Starts API at http://localhost:8081/api
```

**Option B — Mock API (Docker):**
```bash
make init && make api-up   # Starts mock API on http://localhost:3000
```

### 2️⃣ Run Mobile App

```bash
cd app
cp .env.example .env    # Configure BASE_URL
./gradlew installDebug  # Or open folder 'app' in Android Studio and click Run
```

| Run target | Spring Boot Backend URL | Mock API URL |
|------------|-------------------------|--------------|
| Android emulator | `http://10.0.2.2:8081/api/` | `http://10.0.2.2:3000/` |
| Physical device | `http://<LAN-IP>:8081/api/` | `http://<LAN-IP>:3000/` |

> 💡 **Configuration Note:** API Base URL is centrally managed in `.env` and injected into `BuildConfig.BASE_URL` in `app/app/build.gradle.kts`.

---

## 6. Test Evidence

*(Results for: airplane mode, slow/failed requests, empty data, rotation, different screen sizes.
Full results: [`docs/testing-guide.md`](docs/testing-guide.md).)*

---

## 7. Documentation

| Document | Content |
|----------|---------|
| [`docs/proposal.md`](docs/proposal.md) | M1 — problem, idea, scope, plan |
| [`docs/ui-design.md`](docs/ui-design.md) | Users, flows, screens, wireframes, design tokens |
| [`docs/architecture.md`](docs/architecture.md) | Layers, state management, data flow, offline strategy |
| [`docs/api-integration.md`](docs/api-integration.md) | API contract and error handling |
| [`docs/testing-guide.md`](docs/testing-guide.md) | Test methods and our test evidence |
| [`docs/ai-log.md`](docs/ai-log.md) | AI usage log |

---

## 8. AI Disclosure (mandatory)

> Policy: [`INSTRUCTION.md` §7](INSTRUCTION.md#7-ai-usage-policy). Disclosing AI use never lowers your score — hiding it does.

### 8.1 Summary

| Tool / model | Used by | Used for | Files / modules | Level |
|--------------|---------|----------|-----------------|-------|
| Gemini | Đặng Xuân Anh | Generate the project proposal, UI layout ideas & Compose state management review | `docs/proposal.md`, `docs/ui-design.md`, `ui/` | Co-write |
| Claude / Gemini | Nguyễn Văn Hiếu | Consult on Debt Simplification Algorithm & Room DB schema design | `docs/architecture.md`, `data/local/` | Co-write |
| Gemini / Copilot | Nguyễn Minh Hùng | Scaffold network interceptors, test cases and test evidence scripts | `docs/api-integration.md`, `docs/testing-guide.md` | Assist |

**Levels:** **Assist** — explanations, suggestions, review; we wrote the code. **Co-write** — AI drafted parts, we substantially rewrote. **Generated** — AI wrote most of it; we reviewed, tested and can explain it.

### 8.2 Decisions we made ourselves

- **Room Database as Single Source of Truth for Personal Finance:** We decided that personal records (wallets, daily expenses, savings) must be fully functional offline without network reliance, syncing up to the server only when online.
- **Android Keystore for JWT Security:** Rather than storing tokens in plain SharedPreferences, we integrated hardware-backed Android Keystore encryption to protect session tokens against reverse engineering.
- **Greedy Graph Debt Simplification:** Chose a greedy transaction-minimizing algorithm to settle group debts instead of keeping a complete matrix of debts, making settlements clean and easy for users.

### 8.3 Where AI was wrong — and how we found out

- **AI hallucinated plaintext SharedPreferences for auth tokens:** AI suggested saving the JWT bearer token directly in standard `SharedPreferences`. In security review, we detected this flaw and implemented Android Keystore with `EncryptedSharedPreferences`.
- **AI assumed synchronous REST calls for group bill settlement:** AI suggested blocking UI calls for splitting group expenses. We discovered UI stutter during poor network conditions and refactored to Kotlin Coroutines with asynchronous `StateFlow` and optimistic local updates.

### 8.4 Full log

See [`docs/ai-log.md`](docs/ai-log.md).

---

## 9. Contribution (mandatory)

| Member | Owns (modules / documents) | Key PRs / commits | AI-assisted parts | Contribution % |
|--------|----------------------------|-------------------|-------------------|:--------------:|
| **Đặng Xuân Anh** | • Lead Mobile Dev & UI/UX<br>• Jetpack Compose Theme & Screens (`ui/`): Home, Group Overview, Add Expense<br>• Navigation Graph & 4 UI States<br>• Documents: `docs/proposal.md`, `docs/ui-design.md` | `00a6475` — docs: update proposal<br>`fe-ui-init` — feat: compose theme & screens | AI-assisted proposal drafting and UI component ideas | 33.3% |
| **Nguyễn Văn Hiếu** | • Mobile Dev & Database / Architecture<br>• Data Layer & Repository Pattern (`data/repository/`)<br>• Local Persistence (Room DB / SQLite)<br>• Debt Simplification Algorithm & Group-to-Wallet Sync<br>• Document: `docs/architecture.md` | `data-room-init` — feat: Room database & DAOs<br>`debt-algo` — feat: simplify group debt | AI-assisted debt algorithm design & DB schema review | 33.3% |
| **Nguyễn Minh Hùng** | • Mobile Dev & Security, Network, QA<br>• Network & API Client (Retrofit, OkHttp, Interceptors)<br>• Secure Token Storage (Android Keystore)<br>• AI Extraction (Voice/Receipt)<br>• Documents: `docs/api-integration.md`, `docs/testing-guide.md`, `docs/ai-log.md` | `sec-keystore` — feat: secure token storage<br>`test-guide` — test: airplane mode & network retry | AI-assisted test-case generation and network error handling | 33.4% |

We confirm the table above is accurate and agreed by all members:

- [x] Đặng Xuân Anh
- [x] Nguyễn Minh Hùng
- [x] Nguyễn Văn Hiếu

---

<sub>Based on the [mobile-app-starter](https://github.com/hungdn1701/mobile-app-starter) template by Hung N. Dang.</sub>