# Project Proposal — Milestone M1

> Keep it to **1–2 pages**. This document is the main evidence for rubric criterion **A1 — Problem & Idea**.
> AI may help you brainstorm or critique — log it in [`ai-log.md`](ai-log.md) — but the problem, the choices and
> the reasoning must be the team's own. Expect to be asked about this document in the oral defense.

---

## 1. Team & Topic

- **Team:** MoneyCare Team — Đặng Xuân Anh, Nguyễn Văn Hiếu, Nguyễn Minh Hùng
- **Topic:** Personal Finance Tracker combined with Group Bill Splitter.

## 2. Problem

- What problem are you solving, and **who** has it? 
  Young adults, students, and travelers frequently engage in group activities (trips, dining, sharing rent). They struggle to calculate exactly who owes whom at the end of the trip. Additionally, they have to use separate apps for their personal budgeting.
- How is it solved today (existing solutions)? Why is that not good enough for your scenario?
  Currently, users use Splitwise for group debts and MoneyLover/Sổ Thu Chi for personal tracking. This separation creates friction: users have to manually calculate their final share from Splitwise and re-enter it into their personal finance app to keep their budgets accurate.

## 3. Idea

- Your solution in 3–5 sentences.
  MoneyCare is a 2-in-1 mobile application that operates independently of real bank accounts. It features a complete personal finance module where users manually input balances, expenses, and saving goals. It also features a group trip module where a host can log shared bills. The app automatically calculates the final simplified debts and allows users to flexibly sync their total trip spent amount directly into their personal expense log.
- What is **interesting or new** about it compared with existing solutions?
  The seamless integration between Group Splitting and Personal Budgeting. Once a trip is settled, your personal share can be instantly recorded into your manual wallet, eliminating the need to type it out again.

## 4. Scope

| Must have (M3) | Nice to have | Out of scope |
|----------------|--------------|--------------|
| Manual Wallet & Category management (hỗ trợ tạo nhanh "+ Ví khác..." ngay tại form & giao diện pastel) | Multi-currency for trips | Linking real bank accounts |
| Income/Expense logging (phân loại danh mục, chọn ví, lọc thời gian) | Export reports to PDF/Excel | In-app bank transfers |
| Saving goals with progress tracker | Push notifications for new bills | OCR Receipt Scanning |
| Create Trip/Group & Add members | | |
| Log shared expenses (chia đều / chia theo số tiền cụ thể) | | |
| Debt simplification algorithm (Settlement tab — thuật toán tham lam tối giản số lượt chuyển tiền) | | |
| Sync group expense into personal wallet (đồng bộ chi phí nhóm vào ví cá nhân tự động) | | |

The receipt/audio extraction code added later is an experimental extension requiring a backend `GEMINI_API_KEY`; it is not part of the required M3 scope or the offline acceptance criteria.

## 5. Users & Key Challenge

- **Target users:** Travelers, flatmates, students on a budget. They use the app on the go, right after buying a coffee or paying for a taxi.
- **Why mobile?** Tracking expenses must be done instantly at the point of sale. Splitting bills happens in real-time during trips.
- **Hardest part:** 
  1. **Algorithm:** Designing the debt simplification algorithm (`DebtSimplifier.kt` — thuật toán tham lam bipartite matching trên số nguyên Long VNĐ tránh sai số dấu phẩy động: A nợ B, B nợ C $\rightarrow$ A nợ C).
  2. **Data Structure:** Handling complex splitting scenarios without actual banking data.
  3. **Sync:** Flexibly transferring calculated debts from the group trip into the local offline personal wallet (`GroupDetailViewModel.kt`).

## 6. Initial Design Sketch

- **Main user flows:**
  - *Flow 1 (Personal):* Home -> Add Transaction -> Chọn ví (hoặc chọn "+ Ví khác..." để nhập tên ví mới tức thì) -> Nhập số tiền, danh mục, ghi chú -> Lưu -> Tự động cập nhật số dư ví & tiến độ mục tiêu tiết kiệm.
  - *Flow 2 (Group):* Groups -> Chọn nhóm / Tạo nhóm mới -> Thêm chi tiêu chung (Người trả & danh sách người thụ hưởng) -> Chuyển sang Tab "Quyết toán" (Settlement) -> Xem phương án chuyển tiền tối giản -> Nhấn "Đồng bộ vào ví cá nhân" để ghi nhận phần chi tiêu vào sổ thu chi cá nhân.
- **Data:** 
  - Entities: `User`, `Wallet`, `Category`, `Transaction`, `Goal`, `Group`, `GroupMember`, `Expense`, `ExpenseSplit`.
  - Source: Local SQLite (Room DB) for personal data, Mock API/Backend for trip data (to share with other users).
- **Offline implementation scope (M3 decision):** the latest dashboard, personal transactions and wallets remain readable without network through a per-user Room cache. Writes and saving goals remain online-only so the Spring API stays the source of truth.

## 7. Alternatives Considered

| Option | Chosen? | Why / why not |
|--------|:-------:|---------------|
| Integrate with Real Banks | No | Poses security risks, requires complex APIs, and goes beyond the scope of a manageable student project. Manual entry is safer and fits the offline-first requirement. |
| Build two separate apps | No | Fails to solve the core problem of manual data syncing and fragmented user experience. |

## 8. Ownership Plan

| Member | Will own (modules / documents) |
|--------|--------------------------------|
| Đặng Xuân Anh | Mobile app development, UI/UX design, user flows, prototyping, and overall frontend styling. |
| Nguyễn Văn Hiếu | Mobile app development, Personal Expense tracking, Manual Wallets, Saving Goals, and Local SQLite database. |
| Nguyễn Minh Hùng | Mobile testing, security review, test-case preparation, test evidence, and quality assurance. |

## 9. Risks

| Risk | Mitigation |
|------|------------|
| Debt simplification algorithm is too complex to implement correctly. | Start with equal splits first. Research and adapt open-source graph/greedy algorithms for debt simplification. |
| Handling offline/online sync conflicts for group trips. | For M3, keep group trips and all writes online-first, while supporting cached offline reads for dashboard, personal transactions and wallets. This narrower scope avoids silently overwriting server data. |

## 10. Instructor Feedback

*(Filled in after M1 review.)*
