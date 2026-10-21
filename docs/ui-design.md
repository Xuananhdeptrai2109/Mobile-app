# UI/UX Design

> Complete this document before building screens (Milestone M2) and keep it in sync with the app.
> It is graded under **A2 — UI/UX Design**. The examples are illustrations — replace them with your own design.

---

## 1. Users & Goals

### 1.1 Product goal

- **Problem solved:** Users struggle to manage personal budgets while simultaneously handling complex group expenses (like trips or shared apartments) across multiple disconnected apps.
- **Value delivered:** A unified, offline-first experience where tracking daily expenses, hitting saving goals, and splitting group bills all happen in one place, with smart syncing between group debts and personal wallets.

### 1.2 Personas

| | Persona 1 (The Meticulous Saver) | Persona 2 (The Social Organizer) |
|---|---|---|
| **Who** | College student or young professional managing a strict monthly budget. | Frequent traveler or someone living in a shared apartment. |
| **Context of use** | Daily, mostly offline on the go (bus, cafe), tracking quick purchases. | During trips or end-of-month, online/offline, coordinating with friends. |
| **Needs / pain points** | Needs quick input for personal expenses and clear visualization of saving goals. | Hates doing math for group bills; needs a clear, transparent way to show who owes whom what without arguments. |

---

## 2. User Flows

```mermaid
flowchart TD
    OPEN["Open app"] --> HOME["Home Screen"]
    HOME -->|Toggle| PERSONAL["Personal Tab"]
    HOME -->|Toggle| GROUP["Group Tab"]
    
    GROUP --> LIST["Select a Group (e.g., Đà Nẵng Trip)"]
    LIST --> OVERVIEW["Group Overview"]
    
    OVERVIEW --> ADD_EXP["Tap '+' to Add Expense"]
    ADD_EXP --> SPLIT["Input Details & Split Logic"]
    SPLIT --> SAVE["Save Expense"]
    SAVE --> OVERVIEW
```

---

## 3. Screen Inventory

| ID | Screen | Purpose | Main components | Data source |
|:--:|--------|---------|-----------------|-------------|
| `SCR-01` | **Home (Personal)** | Dashboard for individual finance. | Balance card, Income/Expense summary, Saving goal progress, Recent transactions. | Local DB (SQLite) |
| `SCR-02` | **Home (Group)** | List of active shared groups and tasks. | Group cards (members, budget), Task list summary. | API / Local Cache |
| `SCR-03` | **Group Overview** | Main hub for a specific group. | Tab bar (Overview, Expenses, Tasks, Members), Budget progress bar, Quick stats. | API / Local Cache |
| `SCR-04` | **Add Expense** | Form to log a shared bill. | Amount input, Category picker, Payer selection, Split distribution (equal/custom). | User Input -> API |
| `SCR-05` | **Expense Detail** | View breakdown of a specific bill. | Total amount, Payer, List of members with calculated owed amounts. | API / Local Cache |
| `SCR-06` | **Member Detail** | Show financial standing of one person. | Total paid, Total owed, Received amounts, Related transactions. | API / Local Cache |

---

## 4. Wireframes

The UI design is based on the provided high-fidelity mockups.

### 4.1 Personal and Group Home

![Personal and group home overview](asset/wireframes/home-personal-group-overview.png)

*Figure 1. Overview of the Personal and Group home dashboards.*

### 4.2 Group Expense Management Flow

![Group expense management flow details](asset/wireframes/group-expense-flow-details.png)

*Figure 2. Detailed screens for the group expense management flow.*

---

## 5. Design Tokens

### 5.1 Colors

| Token | Light | Dark | Usage |
|-------|-------|------|-------|
| `primary` | `#22C55E` (Green) | `#16A34A` | Main buttons, progress bars, positive numbers (Income), active tabs. |
| `secondary` | `#FEF3C7` (Soft Yellow) | `#D97706` | Warning states, saving goal accents, secondary icons. |
| `background` | `#F9FAFB` (Off-white) | `#1F2937` | App background, grouping containers. |
| `surface` | `#FFFFFF` | `#374151` | Cards, bottom navigation, input fields. |
| `text-primary` | `#111827` | `#F9FAFB` | Main headings, transaction names. |
| `error` | `#EF4444` (Red) | `#F87171` | Expenses, debt ("Cần trả"), delete actions. |
| `success` | `#10B981` | `#34D399` | Settled debts ("Đã thanh toán"), completed tasks. |

### 5.2 Typography & spacing

| Token | Value | Usage |
|-------|-------|-------|
| `title` | 20sp Bold | App bar titles, large balance displays. |
| `body` | 14sp Regular | Transaction names, standard text. |
| `caption` | 12sp Medium | Dates, small tags, sub-labels. |
| `spacing-unit` | 8dp | Base unit for padding/margins (e.g., 16dp for screen edges). |

---

## 6. UI States

For the **Group Expenses List** screen:

| Screen | Loading | Success | Empty | Error (with retry) |
|--------|---------|---------|-------|--------------------|
| Group Expenses | Skeleton list items pulsing gently. | Shows list of transactions categorized by date. | Illustration of empty wallet + "No expenses yet. Tap + to add." | "Unable to sync group data." + Retry button. |

---

## 7. Accessibility & Adaptivity

- **Minimum touch target:** All buttons and list items are at least 48x48dp.
- **Contrast:** Red (expenses) and Green (income) are adjusted to pass WCAG AA contrast ratios against the white background.
- **Small screens:** The bottom navigation bar adapts by hiding text labels on very small devices.
- **Dark mode:** Uses a dark gray surface (`#374151`) rather than pure black to reduce eye strain, with pastel variations of the primary colors.

---

## 8. Design Decisions

| Decision | Alternatives considered | Why we chose this |
|----------|------------------------|-------------------|
| **Top-level Toggle (Personal/Group)** | Using the bottom navigation bar or a side menu (hamburger) for navigation between modes. | A top toggle clearly divides the app's two main contexts without burying the feature. It allows the bottom nav to remain context-sensitive (e.g., showing 'Statistics' only in Personal mode). |
| **Floating Action Button (FAB) at Bottom Center** | Putting an 'Add' button in the top right corner of every screen. | The central FAB is easily reachable with one hand and acts as a universal "quick add" contextually aware of the current tab (adds personal expense if in Personal, adds group expense if in Group). |