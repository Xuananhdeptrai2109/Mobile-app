# Personal Finance V1 Contract

## Scope

- Currency is `VND` and transaction amounts are positive whole numbers.
- Every registered user owns a default wallet named `Ví chính`.
- Wallet balance is derived and maintained by personal transactions.
- Expense transactions subtract from a wallet; income transactions add to it.
- Negative wallet balances are allowed.
- Updating a transaction reverses its old balance effect before applying the new effect.
- Deleting a transaction reverses its balance effect.
- System categories are read-only in V1.
- Wallets and transactions are always scoped to the authenticated JWT user.

## API

All endpoints require `Authorization: Bearer <token>`.

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/categories?type=EXPENSE` | List active system categories |
| `GET` | `/api/wallets` | List the current user's wallets |
| `POST` | `/api/wallets` | Create a zero-balance wallet |
| `PATCH` | `/api/wallets/{walletId}` | Rename or make a wallet default |
| `DELETE` | `/api/wallets/{walletId}` | Delete an unused non-default wallet |
| `GET` | `/api/transactions` | Paginated monthly transaction search |
| `POST` | `/api/transactions` | Create a transaction and update wallet balance |
| `GET` | `/api/transactions/{transactionId}` | Get an owned transaction |
| `PUT` | `/api/transactions/{transactionId}` | Replace an owned transaction |
| `DELETE` | `/api/transactions/{transactionId}` | Delete a transaction and restore wallet balance |
| `GET` | `/api/personal/dashboard?month=2026-10` | Balance, monthly totals and five recent transactions |

Transaction filters: `month=yyyy-MM`, `type`, `categoryId`, `query`, `page`, and `size` (maximum 100).

## Transaction request

```json
{
  "title": "Ăn trưa",
  "amount": 50000,
  "type": "EXPENSE",
  "categoryId": 1,
  "walletId": 1,
  "note": "Cơm văn phòng",
  "receiptImageUrl": null,
  "transactionTime": "2026-10-14T12:30:00"
}
```

The category type must equal the transaction type. The wallet must belong to the current user.

## Insights, monthly budget and savings goals

All resources below are scoped to the authenticated JWT user.

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/personal/statistics?month=2026-10` | Monthly totals, comparison, category breakdown and daily expense trend |
| `GET` | `/api/personal/budgets/{yyyy-MM}` | Read the monthly budget and actual usage |
| `PUT` | `/api/personal/budgets/{yyyy-MM}` | Create or replace a monthly budget with `{ "amount": 5000000 }` |
| `DELETE` | `/api/personal/budgets/{yyyy-MM}` | Remove a monthly budget |
| `GET` | `/api/savings-goals?includeCompleted=false` | List savings goals |
| `POST` | `/api/savings-goals` | Create a savings goal |
| `GET` | `/api/savings-goals/{goalId}` | Read an owned savings goal |
| `PUT` | `/api/savings-goals/{goalId}` | Replace goal metadata |
| `POST` | `/api/savings-goals/{goalId}/contributions` | Track a deposit or withdrawal |
| `DELETE` | `/api/savings-goals/{goalId}` | Delete a goal |

Savings progress is independent from wallet balances in this version. A contribution body is
`{ "amount": 500000, "operation": "DEPOSIT" }`; use `WITHDRAW` to reduce tracked progress.
