# Hướng dẫn chia commit cho dự án MoneyCare

Tài liệu này mô tả cách chia **các thay đổi đang có trong working tree** thành những commit nhỏ, dễ đọc, dễ kiểm thử và đúng phần việc của ba thành viên.

> Quan trọng: commit phải phản ánh công việc mà thành viên thực sự thực hiện, đã đọc và có thể giải thích trong vấn đáp. Không dùng `--author` để ghi tên một người không trực tiếp review và xác nhận commit; không sửa ngày commit, không tạo lịch sử giả và không ghi kết quả test chưa chạy.

## 1. Thành viên và phạm vi chịu trách nhiệm

| Mã | Thành viên | Phạm vi chính |
|---|---|---|
| `XA` | **Đặng Xuân Anh** — D23DCDT006 | Jetpack Compose UI, trạng thái màn hình, navigation, UX và tài liệu giao diện |
| `VH` | **Nguyễn Văn Hiếu** — B23DCCE032 | Models, Room, repositories, thuật toán chia nợ, ViewModel nghiệp vụ, backend và database migration |
| `MH` | **Nguyễn Minh Hùng** — B23DCCE041 | API client, xác thực/session, dependency wiring, cấu hình dự án, unit test, security và tài liệu kiểm thử/API |

Số lượng commit không phải phần trăm đóng góp. UI có nhiều màn hình nên số commit của Xuân Anh có thể nhiều hơn, trong khi commit backend/Room của Hiếu hoặc test/security của Hùng thường lớn và phức tạp hơn.

## 2. Quy tắc commit bắt buộc

### 2.1 Không stage toàn bộ repository

Không dùng:

```powershell
git add .
git add -A
```

Luôn stage đúng file được liệt kê trong từng commit. Riêng khi xóa module mẫu, dùng `git add -A -- app/myapplication` để Git ghi nhận file bị xóa.

### 2.2 Mỗi thành viên tự cấu hình danh tính và tự commit

Trước khi commit, người đang thực hiện kiểm tra danh tính:

```powershell
git config --local user.name
git config --local user.email
```

Nếu chưa đúng, chính thành viên đó cấu hình email GitHub thật của mình:

```powershell
git config --local user.name "TEN THAT CUA THANH VIEN"
git config --local user.email "EMAIL_GITHUB_THAT"
```

Không ghi email ví dụ trong tài liệu này vào lịch sử Git. Không dùng `git commit --author=...` để commit hộ người khác.

### 2.3 Kiểm tra trước mỗi commit

```powershell
git status --short
git diff --stat
git diff --cached --check
git diff --cached
```

Nếu `git diff --cached` có file ngoài phạm vi, bỏ file đó khỏi staging area mà không làm mất nội dung:

```powershell
git restore --staged -- duong/dan/file
```

### 2.4 Chuẩn message

Dùng Conventional Commits:

```text
type(scope): mô tả ngắn ở thể mệnh lệnh
```

Các type dùng trong dự án:

- `feat`: thêm chức năng.
- `fix`: sửa lỗi hành vi.
- `test`: thêm hoặc sửa kiểm thử.
- `docs`: chỉ thay đổi tài liệu.
- `refactor`: đổi cấu trúc nhưng không đổi yêu cầu nghiệp vụ.
- `chore`: cấu hình, dependency hoặc dọn dự án.

Message không dùng nội dung chung chung như `update code`, `fix bug`, `done UI` hoặc `final`.

## 3. Cách làm với working tree hiện tại

Repository đang có nhiều thay đổi chưa commit của cả ba thành viên. Cách an toàn nhất là tạo một nhánh tích hợp mà vẫn giữ nguyên working tree:

```powershell
git switch -c integration/finalize-moneycare
```

Sau đó lần lượt thực hiện các commit trong mục 4, 5 và 6. Mỗi lần đổi người commit, thành viên mới phải kiểm tra lại `user.name` và `user.email`.

Không cần ép mỗi thành viên vào một branch riêng ở thời điểm này vì thay đổi đã trộn trong cùng working tree. Sau đợt tích hợp này, các chức năng mới nên dùng branch riêng:

- `feature/xuan-anh-<ten-man-hinh>`
- `feature/van-hieu-<ten-data-backend>`
- `test/minh-hung-<ten-bo-test>`

## 4. Commit của Đặng Xuân Anh — UI/UX

Mỗi màn hình được tách thành một commit độc lập. Nếu màn hình chưa hoàn thành hoặc file không xuất hiện trong `git status`, bỏ qua commit đó; không sửa file chỉ để tạo thêm commit.

### XA-01 — Thành phần hiển thị trạng thái bất đồng bộ

Mục tiêu: tạo trạng thái dùng chung Loading/Success/Empty/Error và nút Retry.

```powershell
git add app/app/src/main/java/com/moneycare/app/ui/state/LoadState.kt
git add app/app/src/main/java/com/moneycare/app/ui/components/ErrorRetryContent.kt
git commit -m "feat(ui-state): add reusable loading empty and error states"
```

### XA-02 — Màn hình Home

Mục tiêu: hiển thị dữ liệu trang chủ qua ViewModel, xử lý loading/error và tải lại.

```powershell
git add app/app/src/main/java/com/moneycare/app/ui/home/HomeScreen.kt
git add app/app/src/main/java/com/moneycare/app/ui/home/HomeViewModel.kt
git commit -m "fix(home): render asynchronous dashboard states"
```

### XA-03 — Danh sách giao dịch cá nhân

Mục tiêu: hoàn thiện loading, empty, error, retry và state nền dùng cho nhóm màn hình giao dịch.

```powershell
git add app/app/src/main/java/com/moneycare/app/ui/personal/TransactionListScreen.kt
git add app/app/src/main/java/com/moneycare/app/ui/personal/PersonalTransactionViewModel.kt
git commit -m "fix(transactions): handle list loading empty and retry states"
```

### XA-04 — Chi tiết giao dịch

Mục tiêu: hiển thị trạng thái tải chi tiết, lỗi không tìm thấy và lưu trạng thái hộp thoại xóa khi xoay màn hình.

```powershell
git add app/app/src/main/java/com/moneycare/app/ui/personal/TransactionDetailScreen.kt
git commit -m "fix(transaction-detail): preserve delete dialog and error state"
```

### XA-05 — Biểu mẫu thêm/sửa giao dịch

Mục tiêu: giữ tiêu đề, số tiền, loại, danh mục, ví, ghi chú và thời gian sau khi Activity được tái tạo.

```powershell
git add app/app/src/main/java/com/moneycare/app/ui/personal/TransactionFormScreen.kt
git commit -m "fix(transaction-form): preserve input across configuration changes"
```

### XA-06 — Quản lý ví

Mục tiêu: xử lý đủ trạng thái dữ liệu và giữ form/hộp thoại qua xoay màn hình.

```powershell
git add app/app/src/main/java/com/moneycare/app/ui/personal/WalletManagementScreen.kt
git add app/app/src/main/java/com/moneycare/app/ui/personal/WalletManagementViewModel.kt
git commit -m "fix(wallets): preserve wallet form and asynchronous states"
```

### XA-07 — Mục tiêu tiết kiệm

Mục tiêu: giữ form, mục tiêu đang chọn và thao tác đóng/rút tiền qua tái tạo Activity.

```powershell
git add app/app/src/main/java/com/moneycare/app/ui/personal/SavingsGoalsScreen.kt
git add app/app/src/main/java/com/moneycare/app/ui/personal/PersonalInsightsViewModel.kt
git commit -m "fix(savings): preserve goal and contribution form state"
```

### XA-08 — Thống kê và ngân sách

Mục tiêu: xử lý trạng thái thống kê và giữ hộp thoại ngân sách.

```powershell
git add app/app/src/main/java/com/moneycare/app/ui/personal/StatisticsScreen.kt
git commit -m "fix(statistics): preserve budget dialog state"
```

### XA-09 — Thông báo

Mục tiêu: hiển thị Loading/Empty/Error và tải lại thông báo qua ViewModel.

```powershell
git add app/app/src/main/java/com/moneycare/app/ui/personal/NotificationScreen.kt
git add app/app/src/main/java/com/moneycare/app/ui/personal/NotificationViewModel.kt
git commit -m "fix(notifications): add empty error and retry states"
```

### XA-10 — Màn hình chi tiết nhóm

Mục tiêu: giữ trạng thái dialog sửa nhóm, ngân sách, thêm/sửa/xóa thành viên và dùng ID thay vì giữ object UI không save được.

```powershell
git add app/app/src/main/java/com/moneycare/app/ui/group/GroupDetailScreen.kt
git add app/app/src/main/java/com/moneycare/app/ui/group/GroupDialogs.kt
git commit -m "fix(group-detail): preserve dialogs and selected member state"
```

### XA-11 — Tạo nhóm

Mục tiêu: giữ nội dung form, thành viên đã chọn và URI ảnh; không mất dữ liệu khi xoay màn hình.

```powershell
git add app/app/src/main/java/com/moneycare/app/ui/group/create/CreateGroupScreen.kt
git add app/app/src/main/java/com/moneycare/app/ui/group/create/CreateGroupViewModel.kt
git add app/app/src/main/java/com/moneycare/app/ui/components/MemberSelector.kt
git commit -m "fix(create-group): preserve form members and selected image"
```

### XA-12 — Thêm/sửa khoản chi nhóm

Mục tiêu: giữ form khoản chi và danh sách chia tiền tùy chỉnh; khôi phục nhóm khi mở route trực tiếp.

```powershell
git add app/app/src/main/java/com/moneycare/app/ui/group/expense/AddExpenseScreen.kt
git commit -m "fix(group-expense-form): preserve expense and split inputs"
```

### XA-13 — Chi tiết khoản chi nhóm

Mục tiêu: xử lý khoản chi không tồn tại và giữ hộp thoại xác nhận xóa.

```powershell
git add app/app/src/main/java/com/moneycare/app/ui/group/expense/ExpenseDetailScreen.kt
git commit -m "fix(group-expense-detail): show missing state and preserve confirmation"
```

### XA-14 — Chi tiết thành viên

Mục tiêu: xử lý thành viên không tồn tại và giữ dialog sửa/xóa.

```powershell
git add app/app/src/main/java/com/moneycare/app/ui/group/member/MemberDetailScreen.kt
git commit -m "fix(group-member-detail): handle missing member and dialogs"
```

### XA-15 — Thêm/sửa công việc nhóm

Mục tiêu: giữ tiêu đề, người thực hiện, hạn hoàn thành và mô tả qua tái tạo Activity.

```powershell
git add app/app/src/main/java/com/moneycare/app/ui/group/task/AddTaskScreen.kt
git commit -m "fix(group-task-form): preserve task input and assignee state"
```

### XA-16 — Quyết toán nhóm

Mục tiêu: hiển thị giao dịch quyết toán đã lưu, giữ ví đang chọn và tính lại danh sách nợ khi settlement đổi.

```powershell
git add app/app/src/main/java/com/moneycare/app/ui/group/GroupSettlementContent.kt
git commit -m "feat(group-settlement-ui): show settlements and wallet sync flow"
```

### XA-17 — Báo cáo nhóm

Mục tiêu: hiển thị báo cáo từ ViewModel, trạng thái lỗi/retry và luồng xuất CSV.

```powershell
git add app/app/src/main/java/com/moneycare/app/ui/group/GroupReportScreen.kt
git commit -m "fix(group-report): render report states and export feedback"
```

### XA-18 — Navigation của các route con trong nhóm

Mục tiêu: truyền `groupId` vào route khoản chi/thành viên/công việc, khôi phục dữ liệu nhóm và không trả về màn hình trắng.

```powershell
git add app/app/src/main/java/com/moneycare/app/ui/navigation/AppNavigation.kt
git add app/app/src/main/java/com/moneycare/app/ui/group/GroupRouteStateScreen.kt
git commit -m "fix(navigation): restore group context for child routes"
```

### XA-19 — Đồng bộ tài liệu giao diện

Chỉ commit nội dung thực sự đã đối chiếu với ứng dụng. Không thay commit hash trong bảng Contribution bằng giá trị tự nghĩ.

```powershell
git add docs/ui-design.md docs/proposal.md
git commit -m "docs(ui): align screen states and offline UX with the app"
```

`README.md` nên để tới commit cuối cùng sau khi đã có hash thật của các commit và cả ba thành viên tự xác nhận bảng Contribution.

### XA-20 — Hướng dẫn chia commit của nhóm

Đặng Xuân Anh commit tài liệu này sau khi cả ba thành viên đã đọc, sửa lại phạm vi nếu cần và đồng ý với thứ tự thực hiện.

```powershell
git add COMMIT_GUIDE.md
git commit -m "docs(workflow): define per-screen and per-capability commits"
```

## 5. Commit của Nguyễn Văn Hiếu — Data, nghiệp vụ và backend

Các commit data/backend được tách theo capability thay vì gom toàn bộ `backend/src` vào một lần.

### VH-01 — Models cho settlement và đồng bộ ví

```powershell
git add app/app/src/main/java/com/moneycare/app/data/model/GroupModels.kt
git add app/app/src/main/java/com/moneycare/app/data/model/group/GroupDtos.kt
git add app/app/src/main/java/com/moneycare/app/data/model/personal/PersonalFinanceDtos.kt
git commit -m "feat(models): define settlement and group sync contracts"
```

### VH-02 — Dependency Room/KSP

Mục tiêu: khai báo Room, KSP và thư viện test Room. Commit này chỉ chứa cấu hình dependency.

```powershell
git add app/build.gradle.kts app/gradle/libs.versions.toml app/app/build.gradle.kts
git commit -m "chore(room): configure Room persistence and KSP"
```

### VH-03 — Cơ sở dữ liệu cache Room

Mục tiêu: tạo entity, DAO, database và local data source; `CacheMarkerEntity` phân biệt cache rỗng hợp lệ với chưa từng cache.

```powershell
git add app/app/src/main/java/com/moneycare/app/data/local
git commit -m "feat(cache): add per-user Room snapshots and cache markers"
```

### VH-04 — Repository tài chính cá nhân có offline fallback

Mục tiêu: network-first cho dashboard/giao dịch/ví; khi lỗi mạng đọc snapshot theo user; không coi danh sách rỗng là cache bị thiếu.

```powershell
git add app/app/src/main/java/com/moneycare/app/data/repository/PersonalFinanceRepository.kt
git commit -m "feat(personal-data): add network-first Room cache fallback"
```

### VH-05 — Repository nhóm và khoản chi

Mục tiêu: ánh xạ settlement, báo cáo, công việc và đồng bộ khoản chi nhóm qua data layer.

```powershell
git add app/app/src/main/java/com/moneycare/app/data/repository/RealGroupRepository.kt
git add app/app/src/main/java/com/moneycare/app/data/repository/HybridExpenseRepository.kt
git commit -m "feat(group-data): integrate settlements reports and wallet sync"
```

### VH-06 — Thuật toán rút gọn nợ

Mục tiêu: loại các khoản đã quyết toán khỏi số dư còn lại và giữ invariant tổng số dư bằng 0.

```powershell
git add app/app/src/main/java/com/moneycare/app/util/DebtSimplifier.kt
git commit -m "feat(debt): apply persisted settlements to remaining balances"
```

### VH-07 — Nghiệp vụ chi tiết nhóm

Mục tiêu: tải lại nhóm cho route con, xử lý item không tồn tại, settlement và sync vào ví qua repository.

```powershell
git add app/app/src/main/java/com/moneycare/app/viewmodel/GroupDetailViewModel.kt
git commit -m "fix(group-state): restore detail data and settlement actions"
```

### VH-08 — Nghiệp vụ báo cáo nhóm

```powershell
git add app/app/src/main/java/com/moneycare/app/viewmodel/GroupReportViewModel.kt
git commit -m "fix(group-report): expose loading empty and error states"
```

### VH-09 — Backend lưu giao dịch quyết toán

Mục tiêu: thêm entity/repository/service/controller settlement và migration V8.

```powershell
git add backend/src/main/java/com/dev/expensemanagement/controller/GroupSettlementController.java
git add backend/src/main/java/com/dev/expensemanagement/dto/request/CreateGroupSettlementRequest.java
git add backend/src/main/java/com/dev/expensemanagement/dto/response/GroupSettlementResponse.java
git add backend/src/main/java/com/dev/expensemanagement/entity/GroupSettlement.java
git add backend/src/main/java/com/dev/expensemanagement/repository/GroupSettlementRepository.java
git add backend/src/main/java/com/dev/expensemanagement/service/impl/GroupSettlementService.java
git add backend/src/main/resources/db/migration/V8__group_settlements.sql
git commit -m "feat(backend-settlement): persist group debt payments"
```

### VH-10 — Backend đồng bộ khoản chi nhóm vào ví

Mục tiêu: một personal transaction cho mỗi group/user, khóa các bản ghi liên quan, hoàn tác tác động cũ và chỉ áp dụng số tiền mới nhất; migration V9 thêm nguồn nhóm duy nhất.

```powershell
git add backend/src/main/java/com/dev/expensemanagement/controller/PersonalTransactionController.java
git add backend/src/main/java/com/dev/expensemanagement/dto/request/SyncGroupExpenseRequest.java
git add backend/src/main/java/com/dev/expensemanagement/dto/response/PersonalTransactionResponse.java
git add backend/src/main/java/com/dev/expensemanagement/entity/PersonalTransaction.java
git add backend/src/main/java/com/dev/expensemanagement/repository/GroupMemberRepository.java
git add backend/src/main/java/com/dev/expensemanagement/repository/PersonalTransactionRepository.java
git add backend/src/main/java/com/dev/expensemanagement/security/GroupAuthorizationService.java
git add backend/src/main/java/com/dev/expensemanagement/service/impl/PersonalTransactionService.java
git add backend/src/main/resources/db/migration/V9__idempotent_group_wallet_sync.sql
git commit -m "feat(group-wallet-sync): update personal balance idempotently"
```

### VH-11 — Backend báo cáo nhóm

```powershell
git add backend/src/main/java/com/dev/expensemanagement/service/impl/GroupReportService.java
git commit -m "fix(group-report): calculate member totals from persisted data"
```

### VH-12 — Tài liệu kiến trúc

```powershell
git add docs/architecture.md
git commit -m "docs(architecture): document Room cache and settlement sync"
```

## 6. Commit của Nguyễn Minh Hùng — Network, security và QA

### MH-01 — API contract phía Android

Commit sau khi `VH-01` đã có các DTO cần thiết.

```powershell
git add app/app/src/main/java/com/moneycare/app/data/api/ApiService.kt
git commit -m "feat(api): add settlement and group wallet sync endpoints"
```

### MH-02 — Repository xác thực

Mục tiêu: chuyển Retrofit và lưu session ra khỏi Login/Register ViewModel.

```powershell
git add app/app/src/main/java/com/moneycare/app/data/repository/AuthRepository.kt
git commit -m "refactor(auth): move API and session persistence to repository"
```

### MH-03 — Manual dependency container

Commit sau các commit repository của Hiếu để container không tham chiếu lớp chưa tồn tại.

```powershell
git add app/app/src/main/java/com/moneycare/app/di/AppContainer.kt
git add app/app/src/main/java/com/moneycare/app/MoneyCareApp.kt
git commit -m "refactor(di): centralize repositories in application container"
```

### MH-04 — Login/Register ViewModel dùng AuthRepository

```powershell
git add app/app/src/main/java/com/moneycare/app/ui/auth/login/LoginViewModel.kt
git add app/app/src/main/java/com/moneycare/app/ui/auth/register/RegisterViewModel.kt
git commit -m "refactor(auth-ui): consume authentication repository"
```

### MH-05 — Dọn module Android mẫu

Mục tiêu: chỉ giữ module `:app`; không xóa module chính.

```powershell
git add app/settings.gradle.kts
git add -A -- app/myapplication
git commit -m "chore(android): remove unused sample application module"
```

### MH-06 — Unit test thuật toán chia nợ

```powershell
git add app/app/src/test/java/com/moneycare/app/util
git commit -m "test(debt): cover conservation and persisted settlements"
```

### MH-07 — Unit test offline repository

Mục tiêu: kiểm tra cached-empty, cached wallets và trường hợp chưa có cache.

```powershell
git add app/app/src/test/java/com/moneycare/app/data/repository/PersonalFinanceRepositoryOfflineTest.kt
git commit -m "test(cache): cover empty snapshots and missing cache fallback"
```

### MH-08 — Backend test settlement

```powershell
git add backend/src/test/java/com/dev/expensemanagement/service/GroupSettlementServiceTest.java
git commit -m "test(settlement): validate group payment authorization and totals"
```

### MH-09 — Backend test đồng bộ ví và báo cáo

Mục tiêu: kiểm tra sync lặp không nhân đôi và khi số tiền đổi chỉ áp dụng chênh lệch mới nhất.

```powershell
git add backend/src/test/java/com/dev/expensemanagement/service/PersonalTransactionServiceTest.java
git add backend/src/test/java/com/dev/expensemanagement/service/GroupReportServiceTest.java
git commit -m "test(group-finance): cover resync differences and reports"
```

### MH-10 — Kiểm thử migration MySQL

```powershell
git add backend/src/test/java/com/dev/expensemanagement/MySqlMigrationIntegrationTest.java
git commit -m "test(migrations): validate the latest Flyway schema"
```

Không ghi test MySQL là “passed” nếu Docker/Testcontainers không chạy; lần kiểm tra gần nhất test này đang bị skip.

### MH-11 — Tài liệu API

```powershell
git add docs/api-integration.md
git commit -m "docs(api): document settlement and idempotent wallet sync"
```

### MH-12 — Bằng chứng kiểm thử

Chỉ ghi số liệu lấy từ XML/report sau khi thực sự chạy lệnh test.

```powershell
git add docs/testing-guide.md
git commit -m "docs(testing): record automated results and pending device checks"
```

## 7. Thứ tự thực hiện để các commit có thể biên dịch

Thứ tự khuyến nghị:

1. `VH-01` — models/DTO.
2. `MH-01` — API interface.
3. `VH-02`, `VH-03`, `VH-04`, `VH-05` — Room và repositories.
4. `MH-02`, `MH-03`, `MH-04` — auth repository, container và auth ViewModels.
5. `VH-06`, `VH-07`, `VH-08` — thuật toán và ViewModels nhóm.
6. `XA-01` đến `XA-18` — từng màn hình và navigation.
7. `VH-09`, `VH-10`, `VH-11` — backend settlement, sync và report.
8. `MH-06` đến `MH-10` — các bộ test.
9. `XA-19`, `XA-20`, `VH-12`, `MH-11`, `MH-12` — tài liệu chuyên môn và quy trình Git.
10. `MH-05` có thể thực hiện bất kỳ lúc nào, nhưng nên làm trước lần build cuối.
11. Cả nhóm cập nhật AI log và README sau khi có hash commit thật.

Sau mỗi cụm Android, chạy:

```powershell
cd app
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug --no-daemon
cd ..
```

Sau mỗi cụm backend, chạy:

```powershell
cd backend
.\gradlew.bat test --no-daemon
cd ..
```

## 8. AI log — mỗi thành viên tự ghi và tự xác nhận

Sau thay đổi đáng kể, mỗi thành viên thêm **entry của chính mình** vào `docs/ai-log.md`. Không để một người nhận toàn bộ công việc AI log của nhóm.

Mẫu:

```text
Ngày: YYYY-MM-DD
Người thực hiện: <họ tên thật>
Công cụ/model: Codex / Claude / công cụ thực tế đã dùng
Nhiệm vụ: <vấn đề đã nhờ AI hỗ trợ>
AI tạo/đề xuất: <mã, test hoặc tài liệu cụ thể>
File liên quan: <danh sách file>
Những gì tôi giữ lại / sửa / tự kiểm chứng: <thành viên tự viết>
```

Commit AI log riêng của từng người:

```powershell
git add docs/ai-log.md
git commit -m "docs(ai-log): record Dang Xuan Anh UI assistance"
```

```powershell
git add docs/ai-log.md
git commit -m "docs(ai-log): record Nguyen Van Hieu data assistance"
```

```powershell
git add docs/ai-log.md
git commit -m "docs(ai-log): record Nguyen Minh Hung QA assistance"
```

Chỉ người có tên trong entry mới thực hiện commit tương ứng sau khi tự điền phần “giữ lại / sửa / tự kiểm chứng”.

## 9. Commit README cuối cùng

`README.md` đang chứa các mã minh họa như `fe-ui-init`, `data-room-init`, `debt-algo`, `sec-keystore` và `test-guide`. Đây không phải hash commit hiện có. Sau khi hoàn thành toàn bộ commit:

```powershell
git log --oneline --all
```

Cả ba thành viên cùng đối chiếu lịch sử, sau đó một thành viên được nhóm thống nhất cập nhật:

- hash/PR thật;
- phạm vi thực sự đã làm;
- kết quả test thực tế;
- mức độ AI assistance thực tế;
- bảng Contribution chỉ khi cả ba đã xác nhận.

```powershell
git add README.md
git commit -m "docs(readme): align features tests and contribution evidence"
```

Không dùng commit này để tự nhận phần việc chưa thực hiện.

## 10. Checklist trước khi push

```powershell
git status --short
git log --oneline --decorate -40
git diff main...HEAD --check
```

Checklist của nhóm:

- [ ] Không còn file vô tình nằm trong sai commit.
- [ ] Mỗi commit chỉ giải quyết một màn hình hoặc một capability.
- [ ] Tác giả commit là người thực sự review, hiểu và nhận trách nhiệm phần đó.
- [ ] `INSTRUCTION.md` không bị sửa.
- [ ] Không có secret, token, mật khẩu hoặc `.env` thật trong Git.
- [ ] Android unit test và lint đã chạy; số liệu ghi đúng report.
- [ ] Backend test đã chạy; test bị skip được ghi rõ lý do.
- [ ] Các kịch bản chưa chạy trên emulator/thiết bị không bị ghi là passed.
- [ ] Mỗi thành viên đã cập nhật entry AI log của mình.
- [ ] README chỉ dùng hash/PR có thật.

Sau khi đạt checklist:

```powershell
git push -u origin integration/finalize-moneycare
```

Tạo Pull Request vào `main`, để ít nhất một thành viên khác review. Không force-push lên `main` và không squash toàn bộ thành một commit, vì lịch sử theo từng màn hình/capability là bằng chứng tốt hơn cho quá trình phát triển và vấn đáp.
