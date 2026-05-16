# Flash-Cart - Smart Collaborative Expense Splitting

Flash-Cart is a premium, modern web application built for friends, roommates, and couples to collaboratively manage shared expenses. It features intelligent bill splitting, real-time expense tracking, and automatic settlement calculations.

## Project Overview

### Key Features
- **Group Management**: Create shared expense groups and invite members
- **Smart Expense Logging**: Add itemized expenses with multi-person splits
- **Real-Time Dashboard**: View your financial balances across all groups
- **Accurate Calculations**: Uses BigDecimal arithmetic to prevent rounding errors
- **Settlement Engine**: Automatic calculation of minimum transactions needed to settle group debts

### Architecture

```
Flash-Cart/
├── frontend/                          # Vercel-deployed SPA
│   ├── public/
│   │   ├── index.html                # Main HTML entry point
│   │   ├── style.css                 # Complete CSS design system
│   │   └── app.js                    # Vanilla JS application logic
│   └── vercel.json                   # Vercel deployment config
│
└── backend/                          # Spring Boot REST API
    ├── src/main/java/com/flashcart/
    │   ├── FlashCartApplication.java  # Spring Boot entry point
    │   ├── config/
    │   │   └── CorsConfig.java        # CORS configuration for Vercel
    │   ├── entities/
    │   │   ├── User.java              # User entity with JPA
    │   │   ├── Group.java             # Group entity
    │   │   ├── Expense.java           # Expense entity
    │   │   └── ExpenseSplit.java      # Expense split entity
    │   ├── service/
    │   │   ├── UserService.java       # User business logic
    │   │   ├── GroupService.java      # Group business logic
    │   │   └── ExpenseService.java    # Expense & splitting logic (BigDecimal)
    │   ├── controller/
    │   │   ├── UserController.java    # User REST endpoints
    │   │   ├── GroupController.java   # Group REST endpoints
    │   │   ├── ExpenseController.java # Expense REST endpoints
    │   │   ├── DashboardController.java # Dashboard metrics
    │   │   └── SettlementController.java # Settlement calculations
    │   └── repository/
    │       ├── UserRepository.java    # User persistence
    │       ├── GroupRepository.java   # Group persistence
    │       ├── ExpenseRepository.java # Expense persistence
    │       └── ExpenseSplitRepository.java # Split persistence
    ├── src/main/resources/
    │   └── application.properties     # Configuration
    └── pom.xml                        # Maven dependencies
```

---

## Technology Stack

### Frontend
- **HTML5** - Semantic markup
- **CSS3** - Modern design system with CSS variables
- **Vanilla JavaScript (ES6+)** - No frameworks, lightweight & performant
- **Fetch API** - Asynchronous HTTP communication

### Backend
- **Java 17** - Latest LTS version
- **Spring Boot 3.2** - Modern Spring framework
- **Spring Data JPA** - Database abstraction
- **PostgreSQL** - Relational database
- **Maven** - Build tool
- **Lombok** - Reduce boilerplate

### Deployment
- **Frontend**: Vercel (serverless deployment)
- **Backend**: Render or Railway (containerized Java)
- **Database**: PostgreSQL (managed service)

---

## Local Development Setup

### Prerequisites
- Java 17+ ([download](https://www.oracle.com/java/technologies/downloads/))
- Maven 3.8+ ([download](https://maven.apache.org/download.cgi))
- PostgreSQL 12+ ([download](https://www.postgresql.org/download/))
- Node.js 16+ (for frontend only if using dev server)

### Backend Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Flash-Cart/backend
   ```

2. **Create PostgreSQL database**
   ```bash
   createdb flash_cart_db
   ```

3. **Configure application.properties**
   Edit `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/flash_cart_db
   spring.datasource.username=postgres
   spring.datasource.password=your_password
   ```

4. **Build and run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

   The backend will start on `http://localhost:8080`

5. **Verify backend is running**
   ```bash
   curl http://localhost:8080/api/users/exists/test@example.com
   ```

### Frontend Setup

1. **Navigate to frontend directory**
   ```bash
   cd Flash-Cart/frontend
   ```

2. **Serve with a local server**
   ```bash
   # Using Python 3
   python -m http.server 8000 --directory public
   
   # Using Node.js http-server
   npx http-server public -p 8000
   ```

3. **Access the application**
   Open `http://localhost:8000` in your browser

---

## API Endpoints

### User Management
```
POST   /api/users                              Create user account
GET    /api/users/by-email/{email}             Get user by email
GET    /api/users/exists/{email}               Check if user exists
```

### Group Management
```
POST   /api/groups                             Create new group
GET    /api/groups                             Get all user groups
GET    /api/groups/{id}                        Get specific group
POST   /api/groups/{id}/members                Add member to group
```

### Expense Management
```
POST   /api/expenses                           Create expense with splits
GET    /api/expenses/group/{groupId}           Get group expenses
GET    /api/expenses/recent                    Get recent expenses
```

### Dashboard & Settlements
```
GET    /api/dashboard                          Get dashboard metrics
GET    /api/settlements                        Get settlement transactions
GET    /api/settlements/group/{groupId}        Get group settlements
```

### Headers Required
```
X-User-Email: user@example.com                (required for most endpoints)
```

---

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Groups Table
```sql
CREATE TABLE groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_by_id BIGINT NOT NULL REFERENCES users(id),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE group_members (
    group_id BIGINT REFERENCES groups(id),
    user_id BIGINT REFERENCES users(id),
    PRIMARY KEY (group_id, user_id)
);
```

### Expenses Table
```sql
CREATE TABLE expenses (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    total_amount NUMERIC(19,2) NOT NULL,
    group_id BIGINT NOT NULL REFERENCES groups(id),
    paid_by_id BIGINT NOT NULL REFERENCES users(id),
    expense_date TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_expense_group ON expenses(group_id);
CREATE INDEX idx_expense_paid_by ON expenses(paid_by_id);
CREATE INDEX idx_expense_date ON expenses(expense_date);
```

### Expense Splits Table
```sql
CREATE TABLE expense_splits (
    id BIGSERIAL PRIMARY KEY,
    expense_id BIGINT NOT NULL REFERENCES expenses(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    owe_amount NUMERIC(19,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_split_expense ON expense_splits(expense_id);
CREATE INDEX idx_split_user ON expense_splits(user_id);
```

---

## Financial Accuracy & BigDecimal Implementation

Flash-Cart uses **BigDecimal** with **RoundingMode.HALF_UP** for all financial calculations to prevent floating-point errors.

### Example Split Calculation
When splitting $10.00 among 3 people:
- Base amount: $10.00 ÷ 3 = $3.33 (rounded down)
- Distributed: $3.33 + $3.33 + $3.33 = $9.99
- Remainder: $0.01 (added to first person)
- Final split: $3.34, $3.33, $3.33 (total = $10.00 exactly)

Code reference: `ExpenseService.calculateExpenseSplits()`

---

## Deployment

### Deploy Backend to Render

1. **Sign up on Render** ([render.com](https://render.com))

2. **Create Web Service**
   - Select GitHub repository
   - Build command: `mvn clean install`
   - Start command: `java -jar target/*.jar`

3. **Set Environment Variables**
   ```
   DATABASE_URL=postgresql://user:password@host:port/database
   JAVA_VERSION=17
   ```

4. **Deploy**
   Push to main branch, Render auto-deploys

### Deploy Frontend to Vercel

1. **Sign up on Vercel** ([vercel.com](https://vercel.com))

2. **Connect GitHub Repository**
   - Select Flash-Cart repository
   - Root Directory: `frontend`

3. **Add Environment Variable**
   ```
   API_BASE_URL=https://your-backend-url.com/api
   ```

4. **Deploy**
   - Deployment automatic on push to main
   - `vercel.json` handles SPA routing

### Cloud PostgreSQL Setup

**Option 1: Render Postgres**
- Create managed database on Render
- Get connection string
- Use as DATABASE_URL

**Option 2: Railway Postgres**
- Create Postgres plugin on Railway
- Auto-generates DATABASE_URL

**Option 3: Heroku Postgres** (Legacy)
- `heroku addons:create heroku-postgresql:standard-0`

---

## Environment Variables

### Backend (.env or system variables)
```
DATABASE_URL=postgresql://user:password@host:port/database
DATABASE_USER=postgres_user
DATABASE_PASSWORD=postgres_password
```

### Frontend (vercel.json)
```
API_BASE_URL=https://api.flashcart.com
```

---

## Design System

### Color Palette
- **Primary Accent**: #6366f1 (Indigo)
- **Positive**: #10b981 (Emerald - "You are owed")
- **Negative**: #f43f5e (Rose - "You owe")
- **Text Primary**: #1e293b (Dark slate)
- **Background**: #f8fafc (Off-white)

### Typography
- Font Family: System UI (Inter/Segoe UI fallback)
- Headings: Bold 600-700 weights
- Body: Regular 400 weight
- Monospace: Code/amounts in Monaco

### Spacing
- Grid: 8px base unit
- Radius: 12px cards, 8px inputs, 4px badges

---

## Testing

### Test Backend
```bash
cd backend
mvn test
```

### Manual API Testing
```bash
# Create user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@example.com","password":"pass123"}'

# Create group
curl -X POST http://localhost:8080/api/groups \
  -H "Content-Type: application/json" \
  -H "X-User-Email: john@example.com" \
  -d '{"name":"Roommates","memberEmails":["alice@example.com"]}'

# Get dashboard
curl http://localhost:8080/api/dashboard \
  -H "X-User-Email: john@example.com"
```

---

## Security Notes

⚠️ **Important for Production:**

1. **Password Hashing**: Implement BCrypt before production
   ```java
   new BCryptPasswordEncoder().encode(password)
   ```

2. **Authentication**: Add JWT or OAuth2 for real deployments

3. **HTTPS**: Ensure all traffic is encrypted

4. **SQL Injection**: Already prevented by JPA/Parameterized Queries

5. **CORS**: Configured for specific Vercel domains

6. **Input Validation**: Add @Valid annotations to all requests

---

## Performance Optimizations

1. **Database Indexes**: Created on foreign keys and frequently queried columns
2. **Connection Pooling**: HikariCP configured with 10 max connections
3. **Query Optimization**: Lazy loading to prevent N+1 queries
4. **Caching**: Can be added with Spring Cache or Redis
5. **Frontend**: Vanilla JS is faster than frameworks

---

## Troubleshooting

### "Connection refused" to database
- Ensure PostgreSQL is running
- Check DATABASE_URL in application.properties
- Verify database exists: `psql -l | grep flash_cart`

### CORS errors in browser
- Verify backend CORS config includes frontend URL
- Check X-User-Email header is being sent
- Ensure backend is running on correct port

### BigDecimal precision issues
- Always use `new BigDecimal()` with string constructor
- Never use `new BigDecimal(double)` for financial data
- Use `RoundingMode.HALF_UP` for consistent results

### Frontend not connecting to backend
- Check API_BASE_URL environment variable
- Verify backend is accessible from frontend domain
- Check browser console for CORS/network errors

---

## Future Enhancements

- [ ] Authentication with JWT/OAuth2
- [ ] Password hashing with BCrypt
- [ ] Email notifications for settlements
- [ ] Expense receipt image uploads
- [ ] Mobile app (React Native)
- [ ] Recurring expenses
- [ ] Budget tracking
- [ ] Export/reporting features
- [ ] Real-time updates with WebSocket
- [ ] Optimized settlement algorithm (graph-based)

---

## Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Submit pull request

---

## License

MIT License - See LICENSE file for details

---

## Support

For issues or questions:
1. Check the Troubleshooting section
2. Review existing GitHub issues
3. Create a new issue with detailed information

---

**Built with ⚡ by Flash-Cart Team**
