# 📱 Expense Management Frontend (Android)

Android app sử dụng Jetpack Compose để quản lý chi tiêu.

## 🚀 Quick Start

### 1️⃣ Setup Environment

```bash
# Copy template environment
cp .env.example .env

# Update API URL nếu cần
# API_BASE_URL=http://10.0.2.2:8081/api/  (cho Android Emulator)
# API_BASE_URL_RELEASE=https://api.example.com/api/ (bắt buộc HTTPS cho release)
# hoặc
# API_BASE_URL=http://your_machine_ip:8081/api/  (cho Physical Device)
```

### 2️⃣ Open in Android Studio

- File → Open → Chọn folder `Expense_management_fe`
- Chờ Gradle sync

### 3️⃣ Run App

**Option 1: Android Emulator**
```
Device Manager → Create/Select Device → Run App
```

**Option 2: Physical Device**
```bash
adb devices  # Verify device connected
./gradlew installDebug
```

---

## 📋 Environment Variables

| Biến | Mô Tả | Mặc Định |
|------|-------|---------|
| `API_BASE_URL` | Backend API URL | http://10.0.2.2:8081/api/ |
| `API_BASE_URL_RELEASE` | Backend release, bắt buộc HTTPS và dấu `/` cuối | https://example.invalid/api/ |
| `API_TIMEOUT` | Request timeout (s) | 30 |
| `API_LOG_LEVEL` | HTTP logging level | BODY |
| `DEBUG_MODE` | Debug mode | true |
| `ENABLE_LOGGING` | Network logging | true |

---

## 🔑 Important Notes

### Tính năng nhóm

- Tạo, chỉnh sửa, quản lý ngân sách và xóa nhóm bằng tài khoản chủ nhóm.
- Thêm thành viên chỉ bằng tên, không yêu cầu thành viên có tài khoản; có thể sửa tên hoặc xóa khi chưa có lịch sử chi tiêu.
- Tạo, sửa, xóa chi tiêu; chia tiền theo tên thành viên; chủ nhóm đánh dấu đã/chưa thanh toán cho từng người; trích xuất hóa đơn hoặc giọng nói bằng AI.
- Tạo, sửa, hoàn thành và xóa nhiệm vụ theo quyền nhóm.

### Tính năng cá nhân

- CRUD giao dịch, thống kê theo tháng, ngân sách và mục tiêu tiết kiệm.
- Tạo, đổi tên, đặt mặc định và xóa ví chưa có giao dịch từ màn hình `Cài đặt`.
- Thông báo được đồng bộ từ backend; hỗ trợ đánh dấu một hoặc toàn bộ thông báo là đã đọc.
- Đăng nhập mạng xã hội và khôi phục mật khẩu được ẩn cho tới khi OAuth/email service được cấu hình thật.

### Android Emulator vs Physical Device

**Android Emulator:**
- Use `10.0.2.2` để trỏ tới localhost
- URL: `http://10.0.2.2:8081/api/`

**Physical Device:**
- Use machine IP (ví dụ: 192.168.x.x)
- URL: `http://192.168.x.x:8081/api/`

### Build Configuration

File: `app/build.gradle.kts`
```kotlin
buildTypes {
    debug {
        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8081/api/\"")
    }
    release { /* dùng API_BASE_URL_RELEASE và tắt cleartext HTTP */ }
}
```

---

## 🔐 Security

⚠️ **IMPORTANTE**:
- Never commit `.env` file
- Use `.env.example` for configuration template
- Update API URL for production
- Token phiên được mã hóa bằng Android Keystore và bị xóa khi đăng xuất hoặc khi API trả `401`
- HTTP body logging chỉ bật ở debug; header `Authorization` luôn được che

---

## 📦 Dependencies

- Jetpack Compose
- Retrofit 2.9.0
- OkHttp 4.11.0
- Coroutines 1.7.1
- Material3

---

## 🐛 Troubleshooting

**App không kết nối API:**
```
1. Verify Backend đang chạy
2. Check API_BASE_URL đúng
3. Xem Logcat: adb logcat | grep API
```

**Build error:**
```bash
# Clean build
./gradlew clean build

# Invalidate cache
File → Invalidate Caches → Restart
```

**Emulator không connect localhost:**
```
Verify API_BASE_URL = http://10.0.2.2:8081/api/
```

---

**Version**: 1.0  
**Last Updated**: 2026

## Cấu hình build ổn định

- Dùng JDK 17 hoặc JBR đi kèm Android Studio; dự án không ép Gradle tải một JDK riêng qua mạng.
- Chạy local bằng build variant `debug`; Android Emulator dùng `API_BASE_URL=http://10.0.2.2:8081/api/`.
- Điện thoại thật phải thay `10.0.2.2` bằng IP LAN của máy chạy backend.
- App hỗ trợ từ API 24; `java.time` đã được backport bằng core-library desugaring.
- Release build sẽ dừng sớm nếu thiếu HTTPS production URL hoặc bốn biến ký APK: `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`.
- Không dùng debug keystore để phát hành lên Play Console.
