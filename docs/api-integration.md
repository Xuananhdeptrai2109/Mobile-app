# API Integration

> Documents the API your app uses and how the data layer handles it. Keep it in sync with `backend/db.json`
> (or your own backend).

---

## 1. Client Configuration

- **Base URL source:** `API_BASE_URL` / `API_BASE_URL_RELEASE` from `app/.env` or process environment, validated and injected into `BuildConfig.BASE_URL` by `app/app/build.gradle.kts`.
- **Timeouts:** `API_TIMEOUT` configures both connect and read timeout; the default is 30 seconds.
- **Shared HTTP client:** `RetrofitClient` creates one `ApiService`; repositories receive it through `AppContainer`. Composables do not create repositories or call Retrofit.
- **Authentication:** the OkHttp interceptor adds `Authorization: Bearer <JWT>`. A 401 clears the invalid local session.
- **Logging:** the Authorization header is redacted. HTTP logging is enabled only for debug builds and uses `API_LOG_LEVEL`.
- **Transport:** debug may use HTTP for emulator/LAN development. Release configuration requires HTTPS and a non-placeholder URL.

---

## 2. Endpoints Used

The running app uses the Spring Boot API at `/api`. The port-3000 `json-server` remains a simple assignment/demo backend, but it is not a drop-in replacement for authentication or custom aggregate endpoints such as `/groups/me`.

| Method | Path | Used by (screen / repository) | Request | Response |
|:------:|------|-------------------------------|---------|----------|
| POST | `/auth/login`, `/auth/register` | `AuthRepository` | Credentials/profile | JWT and user summary |
| GET/POST/PATCH/DELETE | `/wallets` | `PersonalFinanceRepository` | Wallet DTO | Wallet DTO/list |
| GET/POST/PUT/DELETE | `/transactions` | `PersonalFinanceRepository` | Filters or transaction DTO | Paged transactions / transaction DTO |
| GET | `/personal/dashboard`, `/personal/statistics` | Home and Statistics ViewModels through repository | Month | Dashboard/statistics DTO |
| GET/POST/PUT/DELETE | `/savings-goals` | `PersonalInsightsViewModel` through repository | Goal/contribution DTO | Goal DTO/list |
| GET/POST/PATCH/DELETE | `/groups` and nested members/tasks/expenses | `RealGroupRepository` | Group domain DTOs | Group/detail DTOs |
| GET/POST | `/groups/{id}/settlements` | `RealGroupRepository` | From/to member and amount | Persisted settlement/list |
| POST | `/groups/{id}/sync-personal` | `PersonalFinanceRepository` | Wallet and expense category IDs | One transaction per user/group; repeated sync returns it unchanged or updates it to the latest server-calculated share |
| GET | `/groups/{id}/activities`, `/report`, `/report.csv` | Group detail/report ViewModels | Group ID | Activity/report/CSV |
| POST multipart | `/ai/expenses/extract-from-receipt`, `/extract-from-audio` | `RealGroupRepository` | Image/audio, maximum 10 MB | Extracted expense fields |

---

## 3. Data Models

| Model | Fields | Source (endpoint / local) | Notes (mapping, validation) |
|-------|--------|---------------------------|-----------------------------|
| `Transaction`, `WalletResponse`, `PersonalDashboard` | IDs, money as `Long` VND, category/wallet and timestamps | Spring API + per-user Room cache | API DTOs are mapped to UI models; Room cache is used only if the network read fails |
| `ExpenseGroup` | Members, expenses, tasks and settlements | Spring API | `RealGroupRepository` combines the nested endpoint responses |
| `GroupSettlement` | From member, to member, amount, settled time | `/groups/{id}/settlements` | Stored separately from expense splits so a simplified transfer does not rewrite bill history |
| JWT session | Encrypted token, user ID/name | Auth API + local session storage | JWT ciphertext uses AES/GCM; the AES key remains in Android Keystore |

`sync-personal` never accepts an amount from the mobile client. The backend locks the user's membership and existing source transaction, recalculates the share from group splits, reverses the previous wallet effect, and applies the latest amount. Repeating the same request therefore has no balance effect, while syncing after a bill edit applies only the difference.

---

## 4. Error Handling

| Situation | How it is detected | What the user sees |
|-----------|-------------------|--------------------|
| No network | OkHttp throws an I/O exception | Dashboard, transaction history and wallets use Room cache when available; otherwise Error + Retry |
| Timeout | Configured connect/read deadline expires | Existing content stays visible with a Snackbar, or a full Error + Retry appears if no data exists |
| 400/404/409 | Retrofit `HttpException`; JSON `message` is parsed when available | Vietnamese validation/conflict message; input remains available for correction |
| 401 | Auth interceptor detects response status | Session is cleared and navigation returns to Welcome |
| 403 | Backend group authorization rejects the request | Permission message; no optimistic success is shown |
| 5xx or malformed response | Retrofit conversion/request failure | Generic user-facing message and Retry; raw stack traces are not rendered |

Never show raw technical errors (e.g., `SocketTimeoutException: failed to connect to /10.0.2.2`) to users —
map them to clear messages with a way to retry.
