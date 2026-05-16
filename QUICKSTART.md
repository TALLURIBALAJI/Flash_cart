# Quick Start Guide - Flash-Cart

## Start Development Immediately

### Backend (30 seconds)

#### Option 1: Using Maven Wrapper (Easiest - No Installation Needed)
```bash
# 1. Create database
createdb flash_cart_db

# 2. Run Spring Boot with Maven wrapper
cd backend
.\mvnw.cmd spring-boot:run
```

#### Option 2: Using Maven (Requires Maven 3.8+)
```bash
# 1. Create database
createdb flash_cart_db

# 2. Run Spring Boot
cd backend
mvn spring-boot:run
```

#### Option 3: Using Docker (Alternative)
```bash
# Run PostgreSQL in Docker
docker run --name flashcart-postgres \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=flash_cart_db \
  -p 5432:5432 \
  postgres:15

# In another terminal, run backend
cd backend
mvn spring-boot:run
```

✅ Backend running on `http://localhost:8080`

**⚠️ First Time Setup:**
- If you get "mvn not found", use `.\mvnw.cmd spring-boot:run` (Windows Maven Wrapper - already set up)
- If that doesn't work, [install Maven 3.8+](https://maven.apache.org/download.cgi)
- Java 17+ is required ([download here](https://www.oracle.com/java/technologies/downloads/))

### Frontend (30 seconds)

```bash
# In a new terminal
cd frontend

# Python 3
python -m http.server 8000 --directory public

# OR Node.js
npx http-server public -p 8000
```

✅ Frontend running on `http://localhost:8000`

---

## First Time Usage

### 1. Create Your Account
- Open http://localhost:8000
- Click "Create Account" in modal
- Fill: Username, Email, Password
- ✅ Account created

### 2. Create a Group
- Click "Groups" in sidebar
- Click "Create New Group"
- Name: "My First Group"
- Members: alice@example.com
- ✅ Group created

### 3. Add an Expense
- Click "Add Expense" in sidebar
- Select your group
- Description: "Groceries"
- Amount: $50.00
- Paid by: Your name
- Check both members for split
- ✅ Expense added

### 4. View Dashboard
- Click "Dashboard" in sidebar
- See your financial metrics
- View recent expenses
- ✅ Everything working!

---

## API Testing with cURL

### Create User
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "email": "john@example.com",
    "password": "password123"
  }'
```

### Create Group
```bash
curl -X POST http://localhost:8080/api/groups \
  -H "Content-Type: application/json" \
  -H "X-User-Email: john@example.com" \
  -d '{
    "name": "Apartment",
    "memberEmails": ["alice@example.com"]
  }'
```

### Add Expense
```bash
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -H "X-User-Email: john@example.com" \
  -d '{
    "groupId": 1,
    "description": "Groceries",
    "totalAmount": 50.00,
    "paidById": 1,
    "splitUserIds": [1, 2]
  }'
```

### Get Dashboard
```bash
curl http://localhost:8080/api/dashboard \
  -H "X-User-Email: john@example.com"
```

---

## File Structure Overview

```
flash-cart/
├── frontend/                 # Vercel-ready SPA
│   ├── public/
│   │   ├── index.html       # Main page
│   │   ├── style.css        # Design system
│   │   └── app.js           # Application logic
│   └── vercel.json          # Vercel config
│
├── backend/                  # Spring Boot API
│   ├── src/main/java/com/flashcart/
│   │   ├── entities/        # Database models
│   │   ├── service/         # Business logic
│   │   ├── controller/      # API endpoints
│   │   └── repository/      # Database access
│   ├── pom.xml             # Dependencies
│   └── src/main/resources/  # Config & database
│
├── README.md               # Full documentation
├── DEPLOYMENT.md           # Deployment guide
└── .gitignore             # Git ignore rules
```

---

## Key Features Implemented

✅ **User Management**
- Create accounts
- User email/username validation
- Local storage for session

✅ **Group Management**
- Create shared expense groups
- Add/remove members
- View group members

✅ **Expense Splitting**
- Add expenses to groups
- Split among members with checkboxes
- **BigDecimal precision** - no rounding errors
- Automatic settlement calculation

✅ **Dashboard**
- Real-time metrics (You Owe / You Are Owed)
- Group statistics
- Recent expense history
- Settlement transactions

✅ **UI/UX**
- Premium SaaS design
- Responsive layout (desktop/mobile)
- Smooth animations
- Color-coded balances (red for owe, green for owed)
- Toast notifications for feedback

---

## Technology Highlights

### Frontend
- **Vanilla HTML5/CSS3/JS** - No frameworks, ultra-lightweight
- **Fetch API** - Modern async/await communication
- **CSS Variables** - Complete design system
- **Responsive** - Mobile to desktop

### Backend
- **Spring Boot 3.2** - Latest framework
- **JPA/Hibernate** - ORM database access
- **PostgreSQL** - Reliable RDBMS
- **BigDecimal** - Precise financial math
- **CORS enabled** - Vercel ready

### Architecture
- **Decoupled** - Frontend and backend independent
- **Scalable** - Easy to deploy on Vercel/Render
- **Testable** - Clear separation of concerns
- **Secure** - JPA prevents SQL injection

---

## Database Auto-Setup

On first `mvn spring-boot:run`:
1. ✅ Connects to PostgreSQL
2. ✅ Creates all tables automatically (Hibernate DDL)
3. ✅ Creates indexes for performance
4. ✅ Ready to use!

Check tables:
```bash
psql flash_cart_db
\dt                    # List tables
\d users               # Describe table
```

---

## Production Deployment

### Backend → Render
```bash
git push origin main
# Render auto-deploys from pom.xml
```

### Frontend → Vercel
```bash
git push origin main
# Vercel auto-deploys from vercel.json
```

### See DEPLOYMENT.md for full instructions

---

## Troubleshooting

### "mvn: The term 'mvn' is not recognized..."
**Solution 1 - Use Maven Wrapper (Windows 10/11):**
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

**Solution 2 - Install Maven:**
1. Download [Maven 3.8.7](https://maven.apache.org/download.cgi)
2. Extract to `C:\Program Files\Apache\maven`
3. Add to system PATH: `C:\Program Files\Apache\maven\bin`
4. Restart terminal
5. Run `mvn spring-boot:run`

**Solution 3 - Check Java Installation:**
```bash
# Verify Java is installed
java -version

# If not, download Java 17+ from:
# https://www.oracle.com/java/technologies/downloads/
```

### Backend won't start
```bash
# Check PostgreSQL is running
psql -l | grep flash_cart_db

# If database missing
createdb flash_cart_db

# Rebuild with Maven Wrapper
.\mvnw.cmd clean install
```

### Frontend can't reach backend
- Verify backend is running on http://localhost:8080
- Check API_BASE_URL in app.js
- Look for CORS errors in browser console

### BigDecimal errors
- All amounts use `String` constructors
- RoundingMode.HALF_UP ensures accuracy
- Never use floating point for money

---

## Next Steps

1. **Add Authentication** - JWT tokens for real security
2. **Hash Passwords** - Use BCrypt before production
3. **Add Tests** - Unit tests for edge cases
4. **Deploy** - Follow DEPLOYMENT.md
5. **Monitor** - Set up error tracking (Sentry)
6. **Optimize** - Add caching, optimize queries

---

**You're ready to go! 🚀**

Start with backend & frontend servers and open http://localhost:8000
