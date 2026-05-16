# Architecture & Design Document

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                     FLASH-CART SYSTEM                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │            FRONTEND (Vercel Deployment)                  │   │
│  │                                                           │   │
│  │  ┌────────────────────────────────────────────────────┐  │   │
│  │  │  index.html (Semantic HTML5)                       │  │   │
│  │  │  - Dashboard View                                  │  │   │
│  │  │  - Groups View                                     │  │   │
│  │  │  - Add Expense View                                │  │   │
│  │  │  - Settle Up View                                  │  │   │
│  │  │  - Modal Components                                │  │   │
│  │  └────────────────────────────────────────────────────┘  │   │
│  │                                                           │   │
│  │  ┌────────────────────────────────────────────────────┐  │   │
│  │  │  style.css (Design System)                         │  │   │
│  │  │  - CSS Variables (colors, spacing, radius)         │  │   │
│  │  │  - Utility Classes                                 │  │   │
│  │  │  - Responsive Grid System                          │  │   │
│  │  │  - Animation & Transitions                         │  │   │
│  │  └────────────────────────────────────────────────────┘  │   │
│  │                                                           │   │
│  │  ┌────────────────────────────────────────────────────┐  │   │
│  │  │  app.js (Vanilla JavaScript)                       │  │   │
│  │  │  - View Routing                                    │  │   │
│  │  │  - Fetch API Calls                                 │  │   │
│  │  │  - DOM Manipulation                                │  │   │
│  │  │  - State Management                                │  │   │
│  │  │  - Toast Notifications                             │  │   │
│  │  │  - Form Handling & Validation                      │  │   │
│  │  └────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────┘   │
│                              ↕                                    │
│                        HTTPS/FETCH                               │
│                              ↕                                    │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │         BACKEND (Render/Railway Deployment)              │   │
│  │                                                           │   │
│  │  ┌────────────────────────────────────────────────────┐  │   │
│  │  │  Controller Layer (REST Endpoints)                 │  │   │
│  │  │  - UserController           (/api/users)           │  │   │
│  │  │  - GroupController          (/api/groups)          │  │   │
│  │  │  - ExpenseController        (/api/expenses)        │  │   │
│  │  │  - DashboardController      (/api/dashboard)       │  │   │
│  │  │  - SettlementController     (/api/settlements)     │  │   │
│  │  │  - CorsConfig               (CORS headers)         │  │   │
│  │  └────────────────────────────────────────────────────┘  │   │
│  │                              ↕                             │   │
│  │  ┌────────────────────────────────────────────────────┐  │   │
│  │  │  Service Layer (Business Logic)                    │  │   │
│  │  │  - UserService              (User management)      │  │   │
│  │  │  - GroupService             (Group management)     │  │   │
│  │  │  - ExpenseService           (Expense splitting)    │  │   │
│  │  │                                                    │  │   │
│  │  │  ⚡ CRITICAL: BigDecimal Arithmetic               │  │   │
│  │  │  - Precise split calculation                      │  │   │
│  │  │  - RoundingMode.HALF_UP                           │  │   │
│  │  │  - No floating-point errors                       │  │   │
│  │  │  - Settement optimization                         │  │   │
│  │  └────────────────────────────────────────────────────┘  │   │
│  │                              ↕                             │   │
│  │  ┌────────────────────────────────────────────────────┐  │   │
│  │  │  Repository Layer (JPA)                           │  │   │
│  │  │  - UserRepository                                 │  │   │
│  │  │  - GroupRepository                                │  │   │
│  │  │  - ExpenseRepository                              │  │   │
│  │  │  - ExpenseSplitRepository                         │  │   │
│  │  └────────────────────────────────────────────────────┘  │   │
│  │                              ↕                             │   │
│  │  ┌────────────────────────────────────────────────────┐  │   │
│  │  │  Entity Layer (JPA Models)                         │  │   │
│  │  │  - User                 (id, username, email)      │  │   │
│  │  │  - Group                (id, name, members)        │  │   │
│  │  │  - Expense              (id, amount, group)        │  │   │
│  │  │  - ExpenseSplit         (id, oweAmount, user)      │  │   │
│  │  └────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────┘   │
│                              ↕                                    │
│                        SQL/JDBC                                  │
│                              ↕                                    │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              PostgreSQL Database                          │   │
│  │                                                           │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │   │
│  │  │ users        │  │ groups       │  │ expenses     │   │   │
│  │  ├──────────────┤  ├──────────────┤  ├──────────────┤   │   │
│  │  │ id           │  │ id           │  │ id           │   │   │
│  │  │ username     │  │ name         │  │ description  │   │   │
│  │  │ email        │  │ created_by_id│◄─│ group_id     │   │   │
│  │  │ password     │  │ created_at   │  │ total_amount │   │   │
│  │  │ created_at   │  └──────────────┘  │ paid_by_id ◄─┐   │   │
│  │  └──────────────┘         ↑           │ expense_date │   │   │
│  │         ↑                  │           └──────────────┘   │   │
│  │         │         group_members               │            │   │
│  │         │              table                  │            │   │
│  │         └──────────────────────────────┘      │            │   │
│  │                                               ↓            │   │
│  │                                    ┌──────────────────┐    │   │
│  │                                    │ expense_splits   │    │   │
│  │                                    ├──────────────────┤    │   │
│  │                                    │ id               │    │   │
│  │                                    │ expense_id ◄────————┘   │   │
│  │                                    │ user_id ◄────────┐     │   │
│  │                                    │ owe_amount       │     │   │
│  │                                    └──────────────────┘     │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Data Flow Diagram

### User Creates Expense Flow

```
User Actions                Frontend                Backend              Database
─────────────────────────────────────────────────────────────────────────────
1. Fill expense form
   ├─ Amount: $100
   ├─ Members: 3 people
   └─ Paid by: User A
                            
2. Submit form              POST /api/expenses
                            with JSON body ──→ ExpenseController
                                              
3. Validation                                  ├─ Validate input
                                              ├─ Check group exists
                                              └─ Check users exist
                                              
4. Calculate splits                          ExpenseService
                                            ├─ $100 ÷ 3 = $33.33
                                            ├─ Remainder: $0.01
                                            └─ Distribute: $33.34, $33.33, $33.33
                                            
5. Save to database                         ├─ Save Expense
                                            └─ Save 3 ExpenseSplit ──→ PostgreSQL
                                            
6. Return response          ← JSON response
   {
     id: 123,
     totalAmount: 100.00,
     splits: [{
       userId: 1,
       oweAmount: 33.34
     }, ...]
   }

7. Update UI                Update DOM
   ├─ Show success toast
   ├─ Add to expenses table
   └─ Refresh dashboard
```

---

## Expense Splitting Algorithm

### Problem
How to split $10.00 among 3 people without rounding errors?

### Traditional (Floating Point) - WRONG ❌
```java
double baseAmount = 10.00 / 3;  // 3.3333333...
// Results in: 3.33, 3.33, 3.34 = 10.00
// BUT: Floating point errors accumulate in real applications
```

### Flash-Cart Solution (BigDecimal) - CORRECT ✅
```java
BigDecimal total = new BigDecimal("10.00");
BigDecimal splitCount = new BigDecimal(3);

// 1. Calculate base amount (rounded down)
BigDecimal baseAmount = total.divide(
    splitCount,
    2,
    RoundingMode.DOWN
);  // Result: 3.33

// 2. Calculate total of base amounts
BigDecimal totalBase = baseAmount.multiply(splitCount);
// Result: 9.99

// 3. Calculate remainder
BigDecimal remainder = total.subtract(totalBase);
// Result: 0.01

// 4. Convert to cents and distribute
int remainderCents = remainder
    .multiply(new BigDecimal(100))
    .intValue();  // 1 cent
    
// 5. Assign remainder to first N users
splits = [
    3.34,  // First user gets extra cent
    3.33,
    3.33
]

// 6. Verify
verify(splits.sum() == 10.00);  // ✅ TRUE
```

### Code Location
`ExpenseService.calculateExpenseSplits()` (lines 140-175)

---

## Key Design Decisions

### 1. Decoupled Architecture
**Why**: Independent scaling and deployment
- Frontend can scale on Vercel (serverless)
- Backend can scale on Render/Railway (containers)
- Easy to replace either component

### 2. Vanilla JavaScript (No Framework)
**Why**: Performance and simplicity
- Vanilla JS: 50KB payload
- vs React: 200KB+ payload
- Real-time responsiveness
- Easier to understand and maintain
- Suitable for this use case (CRUD operations)

### 3. PostgreSQL with JPA
**Why**: Relational data integrity
- Users ↔ Groups (many-to-many)
- Groups ↔ Expenses (one-to-many)
- Expenses ↔ Splits (one-to-many)
- Foreign keys ensure consistency
- ACID guarantees for financial accuracy

### 4. BigDecimal for Finance
**Why**: Absolute precision required
- Money cannot have rounding errors
- BigDecimal eliminates floating-point issues
- RoundingMode.HALF_UP standard for currency
- Every penny accounted for

### 5. Service Layer Pattern
**Why**: Maintainability and testability
- Controllers: HTTP handling
- Services: Business logic
- Repositories: Data access
- Clear separation of concerns
- Easy to unit test services

---

## API Contract Examples

### Request: Create Expense
```json
POST /api/expenses
Header: X-User-Email: john@example.com
Content-Type: application/json

{
  "groupId": 1,
  "description": "Groceries",
  "totalAmount": 150.50,
  "paidById": 1,
  "splitUserIds": [1, 2, 3]
}
```

### Response: Expense Created
```json
HTTP/1.1 201 Created
Content-Type: application/json

{
  "id": 42,
  "description": "Groceries",
  "totalAmount": 150.50,
  "paidById": 1,
  "paidByName": "john",
  "groupId": 1,
  "groupName": "Apartment",
  "date": "2024-05-16T14:23:45",
  "splits": [
    {
      "userId": 1,
      "userEmail": "john@example.com",
      "oweAmount": 50.17
    },
    {
      "userId": 2,
      "userEmail": "alice@example.com",
      "oweAmount": 50.17
    },
    {
      "userId": 3,
      "userEmail": "bob@example.com",
      "oweAmount": 50.16
    }
  ]
}
```

---

## Error Handling Strategy

### Frontend Error Handling
```javascript
try {
  const response = await fetch(`${API_BASE_URL}/expenses`, {
    method: 'POST',
    headers: {...},
    body: JSON.stringify(...)
  });
  
  if (!response.ok) {
    throw new Error('Server error');
  }
  
  const data = await response.json();
  // Success processing
} catch (error) {
  // Display user-friendly toast
  showToast('Failed to add expense: ' + error.message, 'error');
}
```

### Backend Error Handling
```java
@PostMapping
public ResponseEntity<?> createExpense(
    @RequestBody CreateExpenseRequest request) {
  try {
    // Validation
    if (request.getTotalAmount() == null || 
        request.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
      return ResponseEntity.badRequest()
          .body(Map.of("error", "Amount must be positive"));
    }
    
    // Business logic
    Expense expense = expenseService.createAndSplitExpense(...);
    
    return ResponseEntity.status(201).body(expense);
  } catch (Exception e) {
    log.error("Failed to create expense", e);
    return ResponseEntity.badRequest()
        .body(Map.of("error", e.getMessage()));
  }
}
```

---

## Performance Considerations

### Database Indexes
```sql
-- Fast lookups by foreign key
CREATE INDEX idx_expense_group ON expenses(group_id);
CREATE INDEX idx_expense_paid_by ON expenses(paid_by_id);
CREATE INDEX idx_expense_date ON expenses(expense_date);

CREATE INDEX idx_split_expense ON expense_splits(expense_id);
CREATE INDEX idx_split_user ON expense_splits(user_id);
```

### Connection Pooling
```properties
# HikariCP settings in application.properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
```

### Frontend Optimization
- Vanilla JS = minimal parsing/execution time
- CSS optimized for layout (minimal reflows)
- Efficient DOM operations (batch updates)
- No unnecessary re-renders

---

## Security Considerations

### Current Implementation
✅ JPA Parameterized Queries (prevents SQL injection)
✅ CORS configured for allowed origins
✅ Input validation on backend
✅ Error messages don't expose internals

### TODO for Production
⚠️ Password hashing with BCrypt
⚠️ JWT/OAuth2 authentication
⚠️ Rate limiting on API endpoints
⚠️ HTTPS only (automatic on Vercel/Render)
⚠️ SQL injection prevention still applies
⚠️ CSRF tokens for state-changing operations
⚠️ User authorization checks (can't edit others' data)

---

## Testing Strategy

### Backend Testing
```java
@SpringBootTest
public class ExpenseServiceTest {
  
  @Test
  public void testAccurateSplitCalculation() {
    // Arrange
    BigDecimal total = new BigDecimal("10.00");
    List<User> users = Arrays.asList(user1, user2, user3);
    
    // Act
    List<ExpenseSplit> splits = 
        expenseService.calculateExpenseSplits(expense, total, users);
    
    // Assert
    assertEquals(3, splits.size());
    assertEquals(new BigDecimal("3.34"), splits.get(0).getOweAmount());
    assertEquals(new BigDecimal("3.33"), splits.get(1).getOweAmount());
    assertEquals(new BigDecimal("3.33"), splits.get(2).getOweAmount());
    
    BigDecimal sum = splits.stream()
        .map(ExpenseSplit::getOweAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    assertEquals(total, sum);  // No rounding errors!
  }
}
```

### Frontend Testing
- E2E testing with Playwright
- Visual regression testing
- Performance testing
- Mobile responsiveness testing

---

## Scalability Path

### Current Capacity
- Handles ~1000 users
- ~10k expenses
- Single Vercel/Render service

### Scaling Strategy (Future)
1. **Caching Layer** - Redis for dashboard queries
2. **Background Jobs** - Celery/Bull for large settlements
3. **Read Replicas** - Database read scaling
4. **Microservices** - Separate expense and settlement services
5. **Search** - Elasticsearch for expense search

---

## Deployment Architecture

```
GitHub Repository
    │
    ├─→ Vercel CI/CD ─→ Frontend Deploy
    │                     (Edge locations)
    │
    └─→ Render CI/CD ─→ Backend Deploy
                          (Docker container)
                          ↓
                      PostgreSQL
                      (Managed DB)
```

---

**System designed for clarity, accuracy, and scalability.** 🚀
