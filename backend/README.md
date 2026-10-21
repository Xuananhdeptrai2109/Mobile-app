# 🔧 Expense Management Backend

Spring Boot REST API cho ứng dụng quản lý chi tiêu.

## 🚀 Quick Start

### 1️⃣ Setup Environment

```bash
# Copy template environment
cp .env.example .env

# Update .env với credentials của bạn
# DB_USERNAME=root
# DB_PASSWORD=your_password
```

### 2️⃣ Create Database

```bash
mysql -u root -p
CREATE DATABASE moneycare_db;
```

### 3️⃣ Run Backend

```bash
./gradlew bootRun
```

✅ API sẽ chạy tại: `http://localhost:8081/api`

---

## 📋 Environment Variables

| Biến | Mô Tả | Mặc Định |
|------|-------|---------|
| `DB_HOST` | MySQL host | localhost |
| `DB_PORT` | MySQL port | 3306 |
| `DB_NAME` | Database name | moneycare_db |
| `DB_USERNAME` | MySQL user | root |
| `DB_PASSWORD` | MySQL password | - |
| `SERVER_PORT` | Backend port | 8081 |
| `JPA_DDL_AUTO` | Hibernate strategy | validate |
| `FLYWAY_BASELINE_ON_MIGRATE` | Baseline DB cũ trước khi migrate | true |
| `LOG_LEVEL_ROOT` | Log level | DEBUG |

---

## 🖼️ Ảnh nhóm

Khi gọi `POST /api/groups`, có thể truyền thêm `imageBase64`: chuỗi Base64 của ảnh (có hoặc không có tiền tố `data:image/...;base64,`). Ảnh được lưu trong cột `LONGTEXT` của MySQL; backend giới hạn chuỗi ở 2.8 MB (xấp xỉ ảnh gốc 2 MB). Danh sách nhóm không trả ảnh để tránh tải các chuỗi lớn vào bộ nhớ; lấy chi tiết nhóm (`GET /api/groups/{groupId}`) sẽ trả `imageBase64`.

Flyway quản lý thay đổi schema; Hibernate chỉ `validate` ở cả dev và production.

---

## 💸 Chi tiêu nhóm và AI

Tạo chi tiêu: `POST /api/groups/{groupId}/expenses`. `paidByMemberId` là một thành viên trả tiền; `splitMemberIds` là một hoặc nhiều thành viên được chia. Backend kiểm tra tất cả các ID này đều thuộc nhóm và chia đều số tiền (phần lẻ được cộng vào người cuối).

```json
{
  "name": "Ăn lẩu nướng",
  "amount": 1000000,
  "paidByMemberId": 12,
  "splitMemberIds": [12, 15, 18],
  "expenseDate": "2026-10-02",
  "note": "Ăn tối cùng nhóm"
}
```

Hai API AI dùng `multipart/form-data`, tối đa 10 MB, để điền trước `name` và `amount` cho form (người dùng vẫn nên xem và xác nhận trước khi tạo chi tiêu):

- `POST /api/ai/expenses/extract-from-audio`, field file: `audio` (MP3, M4A/MP4, WAV, OGG, WebM, AAC)
- `POST /api/ai/expenses/extract-from-receipt`, field file: `image` (JPEG, PNG, WebP, HEIC)

Cả hai trả về `name`, `amount`, `note`, `expenseDate`, `merchantName` và `rawText`. Cần đặt `GEMINI_API_KEY` hợp lệ trong `.env`.

---

## 💳 Chi tiêu cá nhân

Backend hỗ trợ ví mặc định, danh mục hệ thống, CRUD giao dịch cá nhân và dashboard theo tháng. Khi tạo, sửa hoặc xóa giao dịch, số dư ví được cập nhật trong cùng database transaction. Tất cả ví và giao dịch đều được giới hạn theo `userId` lấy từ JWT.

Hợp đồng nghiệp vụ, danh sách endpoint và request mẫu: [docs/personal-finance-v1.md](docs/personal-finance-v1.md).

---

## 🔐 Security

`POST /api/auth/register` và `POST /api/auth/login` là hai endpoint public. Khi đăng nhập thành công, gửi token trong các request còn lại:

```http
Authorization: Bearer <token>
```

Backend lấy `userId` từ JWT. Tạo nhóm không còn nhận `creatorUserId`, và danh sách nhóm của tài khoản hiện tại được lấy qua `GET /api/groups/me`.

Với database đã tồn tại từ trước, hãy sao lưu dữ liệu rồi khởi động ứng dụng. Flyway sẽ đánh dấu schema hiện tại ở baseline version 1 và tự chạy các migration tiếp theo trong `src/main/resources/db/migration`. Database trống sẽ chạy toàn bộ lịch sử migration.

Mô hình thành viên nhóm:

- Chỉ chủ nhóm cần có tài khoản và đăng nhập. Tài khoản tạo nhóm được lưu là thành viên `OWNER`.
- Các thành viên còn lại là hồ sơ nội bộ theo tên, không cần đăng ký, đăng nhập, email hoặc số điện thoại.
- Chủ nhóm dùng các hồ sơ tên này để chọn người trả tiền, chia tiền và giao nhiệm vụ.

Các thao tác quản lý nhóm đã hỗ trợ:

- `PATCH /api/groups/{groupId}` và `DELETE /api/groups/{groupId}`: cập nhật hoặc xóa nhóm (chủ nhóm).
- `POST /api/groups/{groupId}/members` với body `{ "name": "Lan" }`: thêm thành viên chỉ bằng tên.
- `PATCH /api/groups/{groupId}/members/{memberId}`: đổi tên thành viên.
- `DELETE /api/groups/{groupId}/members/{memberId}`: xóa thành viên chưa có lịch sử chi tiêu.
- `PUT`/`DELETE /api/groups/{groupId}/expenses/{expenseId}`: cập nhật hoặc xóa chi tiêu và dữ liệu chia tiền trong cùng transaction.
- `PATCH /api/groups/{groupId}/expenses/{expenseId}/splits/{splitId}/toggle-payment`: chủ nhóm đánh dấu đã/chưa thanh toán cho từng phần chia.

⚠️ **IMPORTANT**:
- Never commit `.env` file
- Use `.env.example` for configuration template
- Update database password in production
- Production must provide a random `JWT_SECRET` containing at least 32 bytes

---

## 📦 Dependencies

- Spring Boot 4.1.0
- Spring Data JPA
- Spring Security
- Flyway
- MySQL Connector
- Lombok

---

## 🐛 Troubleshooting

**Port already in use:**
```bash
netstat -an | grep 8081
# Kill the process or change SERVER_PORT in .env
```

**Database connection error:**
```bash
mysql -u root -p -h localhost
# Verify credentials in .env
```

**JPA/Hibernate error:**
```
Check logs and verify DB_NAME exists
```

---

**Version**: 1.0  
**Last Updated**: 2026

## Personal insights

The personal-finance module includes monthly statistics, monthly spending limits, and savings-goal tracking. Savings contributions track goal progress only and do not automatically change wallet balances. See [docs/personal-finance-v1.md](docs/personal-finance-v1.md) for the API contract.

## Thông báo

- `GET /api/notifications`: danh sách thông báo của tài khoản hiện tại.
- `GET /api/notifications/unread-count`: số thông báo chưa đọc.
- `PATCH /api/notifications/{notificationId}/read`: đánh dấu một thông báo đã đọc.
- `PATCH /api/notifications/read-all`: đánh dấu tất cả đã đọc.
- Khi đăng ký, hệ thống tạo một thông báo chào mừng. Schema được quản lý bởi Flyway migration `V6__user_notifications.sql`.

## Chạy ổn định trong IntelliJ IDEA

- Mở trực tiếp thư mục `Expense_management_be`.
- Chọn run configuration dùng chung `MoneyCare Backend`; cấu hình này đặt working directory về đúng project để nạp `.env`.
- Flyway là nguồn duy nhất thay đổi schema; giữ `JPA_DDL_AUTO=validate`.
- Log mặc định không in SQL parameter hay condition report. Chỉ bật `SPRING_BOOT_DEBUG=true` tạm thời khi cần chẩn đoán.
- Trước lần migrate một database cũ, luôn tạo bản sao lưu MySQL.
