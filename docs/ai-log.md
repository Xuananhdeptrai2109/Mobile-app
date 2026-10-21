# AI Usage Log

> **Mandatory** — see [`INSTRUCTION.md` §7](../INSTRUCTION.md#7-ai-usage-policy).
> Record the AI interactions that **mattered** — ones that produced or shaped design, code, tests or documentation
> that ended up in this repository. A few lines each is enough; you do not need to log every prompt.
>
> Write entries **as you go** (right after the session). The AI assistant configuration in this repo asks your
> assistant to remind you and to suggest a draft entry — but the "what we kept / verified" column must be yours.

---

## Log

| # | Date | Member | Tool / model | Task — what you asked | What AI produced | What you kept, changed or rejected — and how you verified it | Files |
|:-:|------|--------|--------------|-----------------------|------------------|---------------------------------------------------------------|-------|
| 1 | 2026-10-18 | Đặng Xuân Anh | Claude (Agent) | Xây dựng khung Jetpack Compose UI, luồng Navigation và đồng bộ Proposal với README | Mã nguồn các màn hình Compose (Home, Group Detail, Add Expense), cấu hình tuyến đường trong `AppNavigation` và bảng phạm vi Milestone M3 | Giữ kiến trúc UI StateFlow; tinh chỉnh layout chuẩn Material 3; kiểm tra giao diện trên máy ảo và đồng bộ tài liệu | `app/.../ui/`, `README.md`, `docs/ui-design.md` |
| 2 | 2026-10-19 | Nguyễn Văn Hiếu | Claude (Agent) | Thiết kế thuật toán rút gọn nợ nhóm (Debt Simplification) & cấu trúc tầng Repository | Thuật toán Greedy đồ thị bù trừ nợ giữa các thành viên và kiến trúc Repository kết nối dữ liệu | Áp dụng thuật toán Greedy; sửa lỗi sai số làm tròn tiền tệ; xác thực qua các bộ test case chia nợ thực tế | `app/.../data/repository/`, `docs/architecture.md` |
| 3 | 2026-10-20 | Nguyễn Minh Hùng | Claude (Agent) | Cấu hình Retrofit Interceptor, bảo mật session token và kịch bản test offline | Khung cấu hình OkHttp/Retrofit, Interceptor tự động gắn Bearer Token và danh sách kịch bản test lỗi mạng | Loại bỏ đề xuất lưu token bằng SharedPreferences dạng plaintext; thay bằng Android Keystore an toàn; test chế độ máy bay | `app/.../data/api/`, `docs/api-integration.md`, `docs/testing-guide.md` |
| 4 | 2026-10-22 | Đặng Xuân Anh, Nguyễn Văn Hiếu | Claude (Agent) | Hoàn thiện tạo ví động trong form, giao diện Pastel, tab Quyết toán nợ và đồng bộ tài liệu ảnh | Code `DebtSimplifier.kt`, `GroupSettlementContent.kt`, cập nhật `TransactionFormScreen.kt`, bảng 10 màu pastel ví và nhúng 10 screenshots vào tài liệu | Giữ cấu trúc thuật toán Greedy trên số nguyên Long; tích hợp nút đồng bộ ví cá nhân; kiểm tra biên dịch Gradle thành công và đồng bộ toàn bộ tài liệu | `app/.../ui/`, `app/.../util/DebtSimplifier.kt`, `README.md`, `docs/ui-design.md`, `docs/architecture.md` |

---

## Notable Interactions

Describe **2–5 interactions that shaped the project** (a design decision, a hard bug, a rejected suggestion).
These are likely oral-defense topics.

### N1. Thuật toán rút gọn công nợ nhóm (Greedy Debt Simplification)

- **Member / date / tool:** Nguyễn Văn Hiếu / 2026-10-19 / Claude (Agent)
- **Prompt (key excerpt):** "Viết thuật toán tối ưu hóa các khoản nợ giữa các thành viên trong nhóm sau chuyến đi để số giao dịch thanh toán là ít nhất."
- **What the AI suggested:** Thuật toán Greedy chia thành 2 danh sách (con nợ - debtors và chủ nợ - creditors), sắp xếp và lần lượt bù trừ số tiền lớn nhất của 2 bên.
- **What we decided and why:** Đồng ý áp dụng thuật toán Greedy vì trực quan, dễ bảo vệ, độ phức tạp nhỏ ($O(N \log N)$), giảm đáng kể số giao dịch chuyển khoản so với cách ghi nhận ma trận nợ từng cặp.
- **What the AI got wrong or missed (if anything):** AI ban đầu xử lý số tiền theo kiểu số thực (`Double`), dẫn đến lỗi sai số thập phân khi chia tiền. Nhóm đã chủ động sửa lại sử dụng kiểu số nguyên (`Long`) theo đơn vị tiền tệ VNĐ và xử lý phần dư bằng cách gán cho người trả bill.

### N2. Bảo mật Session Token (Android Keystore vs SharedPreferences)

- **Member / date / tool:** Nguyễn Minh Hùng / 2026-10-20 / Claude (Agent)
- **Prompt (key excerpt):** "Lưu trữ Access Token và Refresh Token JWT an toàn trên thiết bị Android sau khi đăng nhập thành công."
- **What the AI suggested:** Lưu chuỗi JWT trực tiếp vào `SharedPreferences` thông thường để nhanh chóng lấy ra khi gửi request qua Retrofit Interceptor.
- **What we decided and why:** Từ chối cách làm này vì token lưu dạng plaintext trong file XML rất dễ bị trích xuất nếu thiết bị bị can thiệp (root) hoặc sao lưu ứng dụng. Nhóm quyết định triển khai `EncryptedSharedPreferences` kết hợp `Android Keystore` với khóa mã hóa bảo vệ bởi phần cứng.
- **What the AI got wrong or missed (if anything):** AI đưa ra giải pháp tiện lợi nhất để chạy được code nhưng bỏ qua yêu cầu an toàn thông tin bắt buộc trong ứng dụng tài chính cá nhân.

### N3. Tạo ví động "+ Ví khác..." trực tiếp trong form giao dịch

- **Member / date / tool:** Đặng Xuân Anh / 2026-10-22 / Claude (Agent)
- **Prompt (key excerpt):** "Khi người dùng thêm thu/chi muốn chọn ví khác thì cho nhập tên ví mới (như Ví MoMo), tự động thêm vào quản lý ví và tự chọn luôn trên form mà không cần thoát ra màn hình Quản lý ví."
- **What the AI suggested:** Tạo thêm một Dialog trên màn hình form, khi lưu thì gọi `createWalletAndSelect` trong `PersonalTransactionViewModel` để thêm ví vào repository và tự động đặt `selectedWalletId` thành ID của ví vừa tạo.
- **What we decided and why:** Đồng ý triển khai giải pháp này vì tăng cường trải nghiệm người dùng tối đa (Zero-friction UX). Người dùng không bị mất dữ liệu form đang nhập dở dang.
- **What the AI got wrong or missed (if anything):** Đề xuất ban đầu của AI không phân biệt màu sắc giữa các ví mới tạo, hiển thị đơn điệu. Nhóm đã yêu cầu bổ sung bảng 10 mã màu Pastel nhẹ nhàng và gán màu xác định theo `walletId % 10` để giao diện luôn thanh lịch, đồng bộ với phong cách chung của ứng dụng.
