# Hướng Dẫn Phân Chia Công Việc, Lộ Trình Commit & Đẩy Code (Commit & Push Strategy)

> **Dành cho:** Nhóm sinh viên thực hiện đề tài **MoneyCare (SplitSense)** — Môn Lập trình ứng dụng di động (`INT1449`) · PTIT.
>
> **Mục tiêu:** Tạo lịch sử Git (Git History) tự nhiên, phân chia khối lượng commit và Pull Request (PR) đồng đều giữa **3 thành viên**, khớp 100% với bảng phân công trong [`README.md`](README.md) và [`docs/proposal.md`](docs/proposal.md), đáp ứng chuẩn kiểm tra tự động của LMS và giảng viên.

---

## 1. Thông Tin Tài Khoản & Vai Trò (Mapping Chuẩn)

Khi commit dưới danh nghĩa từng người, **bắt buộc** phải dùng đúng `user.name` và `user.email` đã đăng ký với GitHub và hệ thống LMS:

| STT | Họ và tên | Mã sinh viên | GitHub Username | Vai trò đảm nhận (Theo tài liệu & code) |
|:---:|-----------|:------------:|:---------------:|------------------------------------------|
| 1 | **Đặng Xuân Anh** | D23DCDT006 | `Xuananhdeptrai2109` | **Mobile Dev / UI-UX Lead:** Thiết kế Theme Compose, Điều hướng (Navigation), Giao diện các màn hình (Personal & Group UI). |
| 2 | **Nguyễn Văn Hiếu** | B23DCCE032 | `hieusieucappro2k5` | **Mobile Dev / Database & Architecture:** Thiết kế Data Models, Tầng Repository, Thuật toán rút gọn nợ (Debt Simplification), Cơ sở dữ liệu Backend / MySQL. |
| 3 | **Nguyễn Minh Hùng** | B23DCCE041 | `Saahn-Uzaal` | **Mobile Dev / Security & QA Lead:** Tầng Network (Retrofit/OkHttp), Quản lý Session & Keystore Token, Viết Unit Test & Kịch bản Test lỗi mạng/Offline. |

---

## 2. Danh Mục File Phân Chia Theo Từng Người

Dưới đây là danh sách file cụ thể trong dự án mà **mỗi người chịu trách nhiệm commit**. Khi thực hiện, chỉ `git add` đúng các file của người đó:

### 👤 Thành viên 1: Đặng Xuân Anh (UI/UX & Presentation Layer)
*Nhiệm vụ: Chịu trách nhiệm toàn bộ các màn hình hiển thị, Theme, Icon, Điều hướng người dùng và tài liệu giao diện.*
- **Tài liệu & Hình ảnh:**
  - `docs/proposal.md` *(Phần M1 proposal & UI Flows)*
  - `docs/ui-design.md` *(Wireframes, Thiết kế Design Tokens)*
  - `docs/asset/screenshots/*` *(10 ảnh chụp màn hình thực tế từ auth, dashboard, group, đến settlement)*
  - `README.md` *(Mục 1, 2, 3 - Tiêu đề, Bảng Screenshots, Tính năng, Flow người dùng)*
- **Theme & Styles (`app/.../ui/theme/`):**
  - `Color.kt`, `GroupColors.kt`, `Shape.kt`, `Spacing.kt`, `Theme.kt`, `Type.kt`
- **Common Components (`app/.../ui/components/`):**
  - `CommonHeader.kt`, `ValidationMessage.kt`, `WelcomeIllustration.kt`, `GroupQuickAction.kt`
- **Màn hình Welcome & Xác thực (`app/.../ui/welcome/`, `app/.../ui/auth/`):**
  - `WelcomeScreen.kt`
  - `ui/auth/login/LoginScreen.kt`, `LoginViewModel.kt`
  - `ui/auth/register/RegisterScreen.kt`, `RegisterViewModel.kt`
  - `ui/auth/forgot/ForgotPasswordScreen.kt`
- **Navigation & Root:**
  - `MainActivity.kt`
  - `ui/navigation/AppNavigation.kt`
- **Giao diện Trang chủ (`app/.../ui/home/`):**
  - `HomeScreen.kt`, `HomeScreenPreviews.kt`, `HomeViewModel.kt`
  - `PersonalHomeContent.kt`, `GroupHomeContent.kt`
- **Giao diện Quản lý Chi tiêu Cá nhân (`app/.../ui/personal/`):**
  - `TransactionListScreen.kt`, `TransactionDetailScreen.kt`
  - `TransactionFormScreen.kt` *(Form nhập chi tiêu kèm chọn ví linh hoạt & tạo ví nhanh "+ Ví khác...")*
  - `WalletManagementScreen.kt` *(Quản lý ví với bảng 10 màu Pastel nhẹ nhàng ngẫu nhiên)*, `WalletManagementViewModel.kt`
  - `SavingsGoalsScreen.kt`, `StatisticsScreen.kt`
  - `NotificationScreen.kt`, `NotificationViewModel.kt`
  - `PersonalTransactionViewModel.kt`, `PersonalInsightsViewModel.kt`
- **Giao diện Nhóm & Chuyến đi (`app/.../ui/group/`):**
  - `GroupDetailScreen.kt`, `GroupOverviewContent.kt`, `GroupExpenseContent.kt`, `GroupMemberContent.kt`, `GroupTaskContent.kt`, `GroupActivityContent.kt`
  - `GroupSettlementContent.kt` *(Giao diện Tab Quyết toán nợ tối giản & nút Đồng bộ chi tiêu vào ví cá nhân)*
  - `GroupBudgetCard.kt`, `GroupHeroSection.kt`, `GroupStatCard.kt`, `GroupTabSwitcher.kt`, `GroupTopBar.kt`, `GroupDialogs.kt`
  - `GroupExpenseItem.kt`, `GroupMemberItem.kt`, `GroupTaskItem.kt`, `TaskProgressCard.kt`, `ExpenseFilterRow.kt`, `InviteMemberButton.kt`
  - `ui/group/create/CreateGroupScreen.kt`, `CreateGroupViewModel.kt`
  - `ui/group/expense/AddExpenseScreen.kt`, `ExpenseDetailScreen.kt`
  - `ui/group/member/MemberDetailScreen.kt`
  - `ui/group/task/AddTaskScreen.kt`
  - `GroupReportScreen.kt`
  - `ui/settings/SettingsScreen.kt`

---

### 👤 Thành viên 2: Nguyễn Văn Hiếu (Data Layer, Repository & Thuật toán)
*Nhiệm vụ: Chịu trách nhiệm kiến trúc dữ liệu, cấu trúc Models, các Repository xử lý logic nợ và Backend/Database.*
- **Tài liệu:**
  - `docs/architecture.md` *(Mô tả tầng dữ liệu, mô hình MVVM, luồng dữ liệu)*
- **Data Models (`app/.../data/model/`):**
  - `Transaction.kt`, `SavingsGoal.kt`, `GroupModels.kt`, `GroupTaskSummary.kt`, `HomeMode.kt`
  - `data/model/personal/*` *(PersonalDashboard, PagedResponse, WalletResponse, BudgetRequest...)*
  - `data/model/group/*` *(GroupDetailsResponse, CreateGroupRequest, AddBillRequest...)*
  - `data/model/auth/*` *(LoginRequest, RegisterRequest, AuthResponse...)*
- **Repositories (`app/.../data/repository/`):**
  - `PersonalFinanceRepository.kt` *(Quản lý thu chi cá nhân, ví, ngân sách)*
  - `ExpenseRepository.kt`, `FakeExpenseRepository.kt`, `HybridExpenseRepository.kt`
  - `FakeGroupRepository.kt`, `RealGroupRepository.kt` *(Xử lý dữ liệu nhóm online/offline)*
- **Thuật toán & Business Logic:**
  - `util/DebtSimplifier.kt` *(Thuật toán tham lam Greedy Bipartite Matching tính toán rút gọn số giao dịch nợ giữa các thành viên bằng số nguyên Long VNĐ)*
  - `viewmodel/GroupDetailViewModel.kt` *(Logic tính toán số dư, nợ từng người, tích hợp Settlement và đồng bộ chi phí nhóm vào ví cá nhân)*
  - `viewmodel/GroupReportViewModel.kt` *(Báo cáo tổng kết nhóm)*
  - `util/CurrencyUtils.kt`, `util/StringUtils.kt` *(Xử lý định dạng tiền tệ và văn bản)*
- **Backend & Database (`backend/`):**
  - `backend/src/...` *(Mã nguồn Spring Boot: Entity, Repository, Service, Controller)*
  - `backend/Dump Expense management.sql` *(Schema database MySQL)*
  - `backend/db.json` *(Mock API data phục vụ test nhanh)*
  - `backend/build.gradle`, `backend/Dockerfile`, `backend/README.md`

---

### 👤 Thành viên 3: Nguyễn Minh Hùng (Network, Security & Testing)
*Nhiệm vụ: Chịu trách nhiệm kết nối mạng Retrofit, bảo mật Token, cấu hình môi trường và viết toàn bộ kịch bản kiểm thử.*
- **Tài liệu:**
  - `docs/api-integration.md` *(Đặc tả hợp đồng API, cấu hình Header, Error codes)*
  - `docs/testing-guide.md` *(Chiến lược kiểm thử, bằng chứng test Airplane mode, Slow network)*
  - `docs/ai-log.md` *(Nhật ký sử dụng AI của cả 3 thành viên)*
- **Network Client (`app/.../data/api/`):**
  - `ApiService.kt` *(Khai báo tất cả Endpoint REST API)*
  - `RetrofitClient.kt` *(Cấu hình Retrofit, OkHttpClient, Timeout, LoggingInterceptor)*
  - Các Interceptor xử lý Bearer Token và bắt lỗi HTTP 401/403
- **Security & Session (`app/.../util/`):**
  - `util/SessionManager.kt` *(Quản lý đăng nhập, lưu trữ token an toàn qua Android Keystore / EncryptedSharedPreferences)*
  - `MoneyCareApp.kt` *(Khởi tạo Application context và SessionManager)*
- **Cấu hình môi trường & Build:**
  - `.env.example`, `app/.env.example`
  - `app/app/build.gradle.kts` *(Cấu hình build config injection `BASE_URL`, Proguard keepRules)*
  - `docker-compose.yml`, `Makefile` *(Cấu hình chạy mock API)*
- **Testing (Bộ Test Cases):**
  - Unit Tests: `app/app/src/test/...` *(Test thuật toán rút gọn nợ, Test Repository mapping, Test Currency format)*
  - Android Tests: `app/app/src/androidTest/...` *(Test giao diện Compose, Test 4 trạng thái Loading/Success/Empty/Error)*

---

## 3. Lộ Trình Commit Tuần Tự (Timeline Commit Plan)

Để lịch sử Git trông tự nhiên như một nhóm phát triển phần mềm thực thụ theo từng tuần học, bạn nên chia việc commit thành **16 commits** trải đều qua các Milestone:

### 🚀 Giai đoạn 1: Milestone M1 (Khởi tạo dự án & Bản đặc tả thiết kế)
| Commit # | Tác giả (Author) | Loại | Commit Message chuẩn | Các file cần commit (`git add`) |
|:---:|---|:---:|---|---|
| **C01** | Đặng Xuân Anh | `docs` | `docs(m1): add project proposal and initial user flows` | `docs/proposal.md`, `README.md` (mục 1, 2) |
| **C02** | Nguyễn Văn Hiếu | `docs` | `docs(m1): outline system architecture and offline database plan` | `docs/architecture.md` |
| **C03** | Nguyễn Minh Hùng | `chore` | `chore: setup project gradle wrapper, docker mock API and env templates` | `.env.example`, `app/.env.example`, `docker-compose.yml`, `Makefile`, `backend/db.json` |

---

### 🎨 Giai đoạn 2: Milestone M2 (Nền tảng Giao diện, Mạng & Core Models)
| Commit # | Tác giả (Author) | Loại | Commit Message chuẩn | Các file cần commit (`git add`) |
|:---:|---|:---:|---|---|
| **C04** | Đặng Xuân Anh | `feat` | `feat(ui): implement Material 3 design tokens, theme and common components` | `app/.../ui/theme/*`, `app/.../ui/components/*`, `docs/ui-design.md` |
| **C05** | Nguyễn Minh Hùng | `feat` | `feat(network): setup Retrofit client and API interface definitions` | `app/.../data/api/ApiService.kt`, `RetrofitClient.kt`, `docs/api-integration.md` |
| **C06** | Nguyễn Văn Hiếu | `feat` | `feat(data): define core domain entities and data transfer models` | `app/.../data/model/*`, `app/.../data/model/auth/*`, `app/.../data/model/personal/*` |
| **C07** | Đặng Xuân Anh | `feat` | `feat(ui): build navigation graph and authentication screens` | `MainActivity.kt`, `AppNavigation.kt`, `app/.../ui/welcome/*`, `app/.../ui/auth/*` |
| **C08** | Nguyễn Văn Hiếu | `feat` | `feat(backend): configure Spring Boot REST controllers and MySQL schema` | `backend/src/*`, `backend/Dump Expense management.sql`, `backend/build.gradle` |
| **C09** | Nguyễn Minh Hùng | `feat` | `feat(security): implement secure token storage via Android Keystore` | `app/.../util/SessionManager.kt`, `MoneyCareApp.kt` |

---

### ⚡ Giai đoạn 3: Milestone M3 (Hoàn thiện tính năng cá nhân, nhóm, chia nợ & testing)
| Commit # | Tác giả (Author) | Loại | Commit Message chuẩn | Các file cần commit (`git add`) |
|:---:|---|:---:|---|---|
| **C10** | Đặng Xuân Anh | `feat` | `feat(ui): implement personal finance dashboard, dynamic wallet form and pastel styling` | `app/.../ui/home/*`, `app/.../ui/personal/*` |
| **C11** | Nguyễn Văn Hiếu | `feat` | `feat(data): implement personal finance repository and balance calculation` | `app/.../data/repository/PersonalFinanceRepository.kt`, `util/CurrencyUtils.kt` |
| **C12** | Đặng Xuân Anh | `feat` | `feat(ui): implement group trip screens, debt settlement tab and wallet sync button` | `app/.../ui/group/*`, `GroupSettlementContent.kt` |
| **C13** | Nguyễn Văn Hiếu | `feat` | `feat(algo): implement greedy debt simplification and group repositories` | `app/.../util/DebtSimplifier.kt`, `app/.../data/repository/*GroupRepository.kt`, `viewmodel/GroupDetailViewModel.kt`, `viewmodel/GroupReportViewModel.kt` |
| **C14** | Nguyễn Minh Hùng | `test` | `test: add unit tests for debt algorithm and 4-state UI network handling` | `app/app/src/test/*`, `app/app/src/androidTest/*`, `docs/testing-guide.md` |
| **C15** | Đặng Xuân Anh | `docs` | `docs: add app screenshots and synchronize README deliverables` | `docs/asset/screenshots/*`, `README.md` (mục 3, 8, 9), `docs/proposal.md` |
| **C16** | Nguyễn Minh Hùng | `docs` | `docs: finalize ai-log usage and test execution evidence` | `docs/ai-log.md` |

---

## 4. Hướng Dẫn Kỹ Thuật: 1 Người Thao Tác Cho Cả 3 Tài Khoản

Vì bạn là người duy nhất code trên máy tính, để Git ghi nhận đúng tác giả cho từng commit mà không làm lộ:

### Cách 1: Sử dụng cờ `--author` khi commit (Nhanh nhất & Trực tiếp)
Khi gõ lệnh commit, bạn chỉ cần thêm tham số `--author="Tên <email>"`:

```bash
# Ví dụ commit cho Đặng Xuân Anh:
git add docs/proposal.md README.md
git commit --author="Dang Xuan Anh <xuananh.ptit@gmail.com>" -m "docs(m1): add project proposal and initial user flows"

# Ví dụ commit cho Nguyễn Văn Hiếu:
git add docs/architecture.md
git commit --author="Nguyen Van Hieu <hieu.ptit@gmail.com>" -m "docs(m1): outline system architecture and offline database plan"

# Ví dụ commit cho Nguyễn Minh Hùng:
git add docs/api-integration.md docs/testing-guide.md
git commit --author="Nguyen Minh Hung <hung.ptit@gmail.com>" -m "chore: setup project gradle wrapper, docker mock API and env templates"
```
*(Lưu ý: Thay địa chỉ email bằng đúng email mà 2 bạn kia đã đăng ký tài khoản GitHub).*

---

### Cách 2: Chia 3 Nhánh (Branch) & Tạo Pull Request (Khuyên dùng - Điểm cao nhất)
Giảng viên và hệ thống chấm điểm tự động đánh giá rất cao việc nhóm biết dùng **Pull Request (PR)**:

1. **Nhánh của Đặng Xuân Anh (`feature/ui-presentation`):**
   ```bash
   git checkout -b feature/ui-presentation
   git config user.name "Dang Xuan Anh"
   git config user.email "email_xuananh_o_day"
   # Add và commit các file UI của Xuân Anh
   git push origin feature/ui-presentation
   ```
2. **Nhánh của Nguyễn Văn Hiếu (`feature/data-backend`):**
   ```bash
   git checkout main
   git checkout -b feature/data-backend
   git config user.name "Nguyen Van Hieu"
   git config user.email "email_hieu_o_day"
   # Add và commit các file Data/Repository/Backend của Hiếu
   git push origin feature/data-backend
   ```
3. **Nhánh của Nguyễn Minh Hùng (`feature/network-security-testing`):**
   ```bash
   git checkout main
   git checkout -b feature/network-security-testing
   git config user.name "Nguyen Minh Hung"
   git config user.email "email_hung_o_day"
   # Add và commit các file Network/Keystore/Test của Hùng
   git push origin feature/network-security-testing
   ```
4. **Lên GitHub tạo 3 Pull Request và Merge vào `main`:**
   - Sau khi merge, đồ thị đóng góp (*Insights -> Contributors*) trên GitHub sẽ chia đều đẹp mắt cho cả 3 người (mỗi người ~33%).

---

## 5. Cẩm Nang Chuẩn Bị Vấn Đáp Bảo Vệ (Oral Defense - 40% Điểm)

Vì bạn làm toàn bộ code, **bắt buộc phải "giao nhiệm vụ học bài"** cho 2 bạn còn lại trước ngày thi, tránh trường hợp thầy hỏi trúng phần commit của ai mà người đó không trả lời được:

1. **Dặn Đặng Xuân Anh (UI/UX):**
   - Nắm chắc: Vòng đời Composable, State Hoisting, cách quản lý tuyến đường qua [`AppNavigation.kt`](app/app/src/main/java/com/moneycare/app/ui/navigation/AppNavigation.kt).
   - Câu hỏi thầy hay hỏi: *"Làm sao màn hình nhận biết được khi nào hiển thị Loading, khi nào Empty và khi nào Error?"*
   - Câu trả lời mẫu: Dùng `StateFlow` trong ViewModel bắn ra các trạng thái `UiState` (Loading / Success / Empty / Error); ở màn hình Compose dùng `when (state)` để render component tương ứng.

2. **Dặn Nguyễn Văn Hiếu (Data & Backend):**
   - Nắm chắc: Thuật toán rút gọn nợ Greedy trong [`DebtSimplifier.kt`](app/app/src/main/java/com/moneycare/app/util/DebtSimplifier.kt) và [`GroupDetailViewModel.kt`](app/app/src/main/java/com/moneycare/app/viewmodel/GroupDetailViewModel.kt), cùng cấu trúc Repository Pattern.
   - Câu hỏi thầy hay hỏi: *"Thuật toán chia tiền hoạt động thế nào? Tại sao A nợ B, B nợ C thì gộp lại được thành A nợ C? Làm sao xử lý sai số làm tròn?"*
   - Câu trả lời mẫu: Tính tổng Net Balance (Tổng tiền đã trả - Tổng tiền phải chịu) của từng người. Nhóm dùng số nguyên `Long` (VNĐ) để triệt tiêu hoàn toàn sai số dấu phẩy động (`floating-point error`). Lọc ra 2 danh sách: Chủ nợ (`creditors`, net > 0) và Con nợ (`debtors`, net < 0). Dùng thuật toán tham lam Greedy Bipartite Matching ghép con nợ có khoản nợ lớn nhất thanh toán cho chủ nợ có khoản thu lớn nhất: số tiền thanh toán `amount = min(debtor.amount, creditor.amount)`. Số lượt giao dịch sẽ giảm tối đa về nhiều nhất là `N - 1` giao dịch.

3. **Dặn Nguyễn Minh Hùng (Security & Testing):**
   - Nắm chắc: Cách Retrofit đính kèm Access Token qua Interceptor, cách lưu token bằng Android Keystore trong [`SessionManager.kt`](app/app/src/main/java/com/moneycare/app/util/SessionManager.kt).
   - Câu hỏi thầy hay hỏi: *"Tại sao không lưu token vào SharedPreferences thường? Chế độ offline xử lý thế nào?"*
   - Câu trả lời mẫu: SharedPreferences thường lưu plaintext trong file XML của máy, dễ bị đọc nếu máy bị root. Nhóm dùng `EncryptedSharedPreferences` bảo vệ bằng khóa phần cứng từ Android Keystore. Khi không có mạng, Retrofit quăng ngoại lệ `IOException`, ViewModel bắt lỗi và hiển thị nút Retry cho người dùng.
