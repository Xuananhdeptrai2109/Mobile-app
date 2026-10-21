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
| Manual Wallet & Category management | Multi-currency for trips | Linking real bank accounts |
| Income/Expense logging | Export reports to PDF/Excel | In-app bank transfers |
| Saving goals with progress tracker | Push notifications for new bills | OCR Receipt Scanning |
| Create Trip & Add members | | |
| Log shared expenses (who paid for who) | | |
| Debt simplification algorithm (Settlement)| | |

## 5. Users & Key Challenge

- **Target users:** Travelers, flatmates, students on a budget. They use the app on the go, right after buying a coffee or paying for a taxi.
- **Why mobile?** Tracking expenses must be done instantly at the point of sale. Splitting bills happens in real-time during trips.
- **Hardest part:** 
  1. **Algorithm:** Designing the debt simplification algorithm (e.g., A owes B $10, B owes C $10 -> A owes C $10).
  2. **Data Structure:** Handling complex splitting scenarios without actual banking data.
  3. **Sync:** Flexibly transferring calculated debts from the group trip into the local offline personal wallet.

## 6. Initial Design Sketch

- **Main user flows:**
  - *Flow 1 (Personal):* Home -> Add Transaction -> Input amount, category, note -> Save -> Update Wallet & Saving Goal progress.
  - *Flow 2 (Group):* Trips -> Create "Da Lat Trip" -> Add Members -> Add Bill -> Select Payer & Beneficiaries -> View Settlement -> End Trip -> Sync to Personal Wallet.
- **Data:** 
  - Entities: `User`, `Wallet`, `Category`, `Transaction`, `Goal`, `Trip`, `TripMember`, `Bill`, `BillSplit`.
  - Source: Local SQLite for personal data, Mock API/Backend for trip data (to share with other users).
- **Offline:** Personal transactions, wallet balances, and saving goals must be 100% available without network.

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
| Handling offline/online sync conflicts for group trips. | For M3, make group trips online-first (require network to sync bills), while keeping personal tracking strictly offline-first. |

## 10. Instructor Feedback

*(Filled in after M1 review.)*
