# Project Name: MoneyCare (SplitSense) — Expense Tracker & Group Splitter

> **Course:** Mobile Application Development (INT1449) · PTIT · Instructor: Dr. Hung N. Dang
>
> *One-sentence pitch: A 2-in-1 mobile application that helps users manage personal finances, track saving goals, and seamlessly split group expenses after trips.*

👋 **New here? Read first:** [`docs/student-guide.md`](docs/student-guide.md) · 📜 Brief & grading: [`INSTRUCTION.md`](INSTRUCTION.md) · 🚀 Workflow: [`GETTING_STARTED.md`](GETTING_STARTED.md)

---

## 1. Team (mandatory)

| # | Full name | Student ID | Class | GitHub | Role |
|:-:|-----------|:----------:|:-----:|--------|------|
| 1 | Đặng Xuân Anh | D23DCDT006 | E23HTTT03 | Xuananhdeptrai2109 | Mobile Dev / UIUX |
| 2 | Nguyễn Văn Hiếu | B23DCCE032 | E23HTTT03 | hieusieucappro2k5 | Mobile Dev / Database |
| 3 | Nguyễn Minh Hùng | B23DCCE041 | E23HTTT03 | Saahn-Uzaal | Mobile / SEC - Test |

**Topic:** Offline-first personal expense tracker & Group bill splitter

---

## 2. Problem & Idea (mandatory)

- **Problem:** Young adults, students, and travelers frequently engage in group activities (trips, dining, sharing rent). They struggle to calculate exactly who owes whom at the end of the trip. Additionally, they currently have to use separate apps for their personal budgeting (e.g., MoneyLover, Sổ Thu Chi) and group debts (e.g., Splitwise). This separation creates friction: users have to manually calculate their final share and re-enter it into their personal finance app to keep their budgets accurate.
- **Our idea:** MoneyCare is a 2-in-1 mobile application operating independently of real bank accounts. It features a complete personal finance module (manual wallets, income/expense tracking, saving goals, monthly budgets) and a dedicated group trip module (shared bills, equal/custom splitting, simplified debt calculation). The key differentiator is seamless integration: once a trip is settled, the user's actual trip spending can be automatically synced back to their personal wallet without manual re-entry.
- **Target users:** Travelers, flatmates, students on a budget who need to track expenses on the go right after buying a coffee or paying for a taxi.
- **What makes it non-trivial (Key Challenges):**
  1. *Debt Simplification Algorithm:* Minimizing the number of transactions required to settle group debts (e.g., A owes B $10, B owes C $10 $\rightarrow$ A owes C $10).
  2. *Data Structures & Split Logic:* Handling multi-member bill splits (equal/custom) clearly without relying on banking APIs.
  3. *Offline-First & Sync:* Keeping personal finances 100% functional offline (SQLite/Room), while flexibly syncing group settlement data into the personal wallet.
- **Out of scope:** Linking real bank accounts, in-app direct bank transfers, OCR receipt scanning.

Full proposal: [`docs/proposal.md`](docs/proposal.md)

---

## 3. Screenshots & Features

| Home & Wallets | Saving Goals | Trip Dashboard | Settlement |
|:---:|:---:|:---:|:---:|
| ![Home](docs/asset/screenshots/03-home-personal.png) | ![Saving Goals](docs/asset/screenshots/05-savings-goals.png) | ![Trip Dashboard](docs/asset/screenshots/08-group-detail.png) | ![Settlement](docs/asset/screenshots/10-debt-settlement.png) |

<details>
<summary><b>📸 Xem đầy đủ 10 màn hình thực tế của ứng dụng</b></summary>

| 1. Đăng nhập | 2. Đăng ký | 3. Home Cá nhân | 4. Thêm Thu/Chi | 5. Mục tiêu tiết kiệm |
|:---:|:---:|:---:|:---:|:---:|
| ![Login](docs/asset/screenshots/01-auth-login.png) | ![Register](docs/asset/screenshots/02-auth-register.png) | ![Home](docs/asset/screenshots/03-home-personal.png) | ![Form](docs/asset/screenshots/04-transaction-form.png) | ![Savings](docs/asset/screenshots/05-savings-goals.png) |

| 6. Quản lý ví (Màu Pastel) | 7. Home Nhóm | 8. Chi tiết Nhóm | 9. Chia Bill Nhóm | 10. Quyết toán nợ (Settlement) |
|:---:|:---:|:---:|:---:|:---:|
| ![Wallets](docs/asset/screenshots/06-wallets.png) | ![Group Home](docs/asset/screenshots/07-home-group.png) | ![Detail](docs/asset/screenshots/08-group-detail.png) | ![Split](docs/asset/screenshots/09-add-expense-split.png) | ![Settlement](docs/asset/screenshots/10-debt-settlement.png) |

</details>

### 3.1 Scope & Milestones (from Proposal)

| Must have (Milestone M3) | Nice to have | Out of scope |
|--------------------------|--------------|--------------|
| [x] Manual Wallet & Category management | [ ] Multi-currency for trips | ❌ Linking real bank accounts |
| [x] Tùy chọn "Ví khác..." tạo ví động trong form | [ ] Export reports to PDF/Excel | ❌ In-app bank transfers |
| [x] Quản lý ví với bảng màu Pastel trang nhã | [x] Notifications for new bills/updates | ❌ OCR Receipt Scanning |
| [x] Daily Income / Expense logging | [x] Xuất báo cáo nhóm ra CSV | |
| [x] Saving goals with progress tracking | | |
| [x] Create Trip/Group & Add members | | |
| [x] Log shared expenses (payer & beneficiaries) | | |
| [x] Debt simplification algorithm (Settlement) | | |
| [x] Sync group settlement to personal wallet | | |

### 3.2 Key User Flows
- **Flow 1 (Personal Finance):** Home $\rightarrow$ Add Transaction $\rightarrow$ Chọn loại Thu nhập / Chi tiêu $\rightarrow$ Chọn ví có sẵn hoặc tạo "Ví khác..." $\rightarrow$ Lưu $\rightarrow$ Cập nhật tức thì Số dư hiện tại & Tiến độ mục tiêu tiết kiệm.
- **Flow 2 (Group Trip, Bill Splitting & Settlement):** Group Home $\rightarrow$ Tạo chuyến đi (e.g., "Đà Lạt Trip") $\rightarrow$ Thêm thành viên $\rightarrow$ Thêm Bill (chọn người trả & người hưởng thụ, chia đều / custom) $\rightarrow$ Tab **Quyết toán** (Xem thuật toán rút gọn nợ Greedy) $\rightarrow$ Xác nhận thanh toán $\rightarrow$ Bấm **"Đồng bộ vào ví cá nhân"** để lưu số tiền thực chi vào ví.

- [x] **Offline:** Personal expense records, wallets, saving goals, and local trip drafts are available without network.
- [x] **UI states:** Loading / Success / Empty / Error with retry on every data screen.

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
| Claude (Agent) | Đặng Xuân Anh | Jetpack Compose screens, UI layouts, navigation flow & document alignment | `docs/proposal.md`, `docs/ui-design.md`, `ui/`, `README.md` | Co-write |
| Claude (Agent) | Nguyễn Văn Hiếu | Debt Simplification Algorithm & Room DB / Repository architecture | `docs/architecture.md`, `data/repository/` | Co-write |
| Claude (Agent) | Nguyễn Minh Hùng | Network interceptors (Retrofit/OkHttp), token security & test cases | `docs/api-integration.md`, `docs/testing-guide.md` | Assist |

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
| **Nguyễn Minh Hùng** | • Mobile Dev & Security, Network, QA<br>• Network & API Client (Retrofit, OkHttp, Interceptors)<br>• Secure Token Storage (Android Keystore)<br>• Security Review & Testing Strategy<br>• Documents: `docs/api-integration.md`, `docs/testing-guide.md`, `docs/ai-log.md` | `sec-keystore` — feat: secure token storage<br>`test-guide` — test: airplane mode & network retry | AI-assisted test-case generation and network error handling | 33.4% |

We confirm the table above is accurate and agreed by all members:

- [x] Đặng Xuân Anh
- [x] Nguyễn Minh Hùng
- [x] Nguyễn Văn Hiếu

---

<sub>Based on the [mobile-app-starter](https://github.com/hungdn1701/mobile-app-starter) template by Hung N. Dang.</sub>