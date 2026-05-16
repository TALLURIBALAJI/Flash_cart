# Flash-Cart - Deployment Guide

## Vercel Frontend Deployment

### Prerequisites
- Vercel account ([vercel.com](https://vercel.com))
- GitHub account with Flash-Cart repository

### Step-by-Step Deployment

#### 1. Connect GitHub Repository
```bash
# Push your code to GitHub
git push origin main
```

#### 2. Import to Vercel
1. Go to [vercel.com/new](https://vercel.com/new)
2. Click "Continue with GitHub"
3. Select your Flash-Cart repository
4. Configure project:
   - **Project Name**: flash-cart
   - **Root Directory**: `frontend`
   - **Framework Preset**: Other (it's vanilla HTML/CSS/JS)

#### 3. Environment Variables
Add in Vercel project settings:
```
API_BASE_URL=https://your-backend-domain.com/api
NODE_ENV=production
```

#### 4. Deploy
- Click "Deploy"
- Vercel will automatically build and deploy
- Get your production URL (e.g., `https://flash-cart.vercel.app`)

#### 5. Custom Domain (Optional)
1. Go to Project Settings → Domains
2. Add your custom domain
3. Follow DNS configuration instructions

---

## Render Backend Deployment

### Prerequisites
- Render account ([render.com](https://render.com))
- PostgreSQL service or external database
- GitHub account with Flash-Cart repository

### Step-by-Step Deployment

#### 1. Create PostgreSQL Database

**Option A: Render-Managed Postgres**
1. Go to Render dashboard
2. Click "New" → "PostgreSQL"
3. Configure:
   - Name: `flash-cart-db`
   - Database: `flash_cart_db`
   - Choose region
4. Create and copy connection string

**Option B: External Database**
- Use Render Postgres, Heroku Postgres, or AWS RDS
- Get connection string

#### 2. Create Web Service

1. Go to Render dashboard
2. Click "New" → "Web Service"
3. Select your GitHub repository
4. Configure:
   - **Name**: `flash-cart-api`
   - **Runtime**: Java
   - **Build Command**: `mvn clean install`
   - **Start Command**: `java -jar target/*.jar`
   - **Region**: Choose closest to you
   - **Plan**: Free or Starter (depending on needs)

#### 3. Set Environment Variables

In Render Web Service Settings → Environment:
```
DATABASE_URL=postgresql://username:password@host:port/database
JAVA_VERSION=17
```

#### 4. Deploy

1. Click "Deploy"
2. Render will:
   - Clone repository
   - Run Maven build
   - Start Java application
   - Provide public URL

3. Monitor logs for errors

#### 5. Verify Deployment

```bash
# Test API endpoint
curl https://your-backend-url.render.com/api/users/exists/test@example.com
```

---

## Railway Backend Deployment

### Prerequisites
- Railway account ([railway.app](https://railway.app))
- GitHub account

### Step-by-Step Deployment

#### 1. Connect GitHub

1. Go to [railway.app](https://railway.app)
2. Click "New Project"
3. Select "Deploy from GitHub repo"
4. Authorize and select Flash-Cart repository

#### 2. Add PostgreSQL

1. In project, click "Add Service"
2. Select "PostgreSQL"
3. PostgreSQL will auto-generate a `DATABASE_URL`

#### 3. Configure Backend

1. In project, click "Add Service" → "GitHub Repo"
2. Select Flash-Cart backend
3. Railway auto-detects Java/Maven
4. Set up environment:
   - **Service Name**: flash-cart-backend
   - **Root Directory**: `backend`

#### 4. Deploy

- Railway automatically builds and deploys on push
- Monitor in "Logs" tab
- Public URL provided in "Settings" → "Domains"

---

## Environment Variables Summary

### Render/Railway DATABASE_URL Format

PostgreSQL connection string:
```
postgresql://[user]:[password]@[host]:[port]/[database]
```

Example:
```
postgresql://postgres:abc123@ep-mute-wave-123456.us-east-1.postgres.vercel-storage.com:5432/flash_cart_db
```

### Extract from URL:
```
DATABASE_USER=postgres
DATABASE_PASSWORD=abc123
DATABASE_URL=postgresql://postgres:abc123@host:5432/flash_cart_db
```

---

## GitHub Actions CI/CD (Optional)

Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy Flash-Cart

on:
  push:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up Java
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run tests
        run: cd backend && mvn test

  deploy-backend:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v3
      - name: Deploy to Render
        run: |
          curl -X POST ${{ secrets.RENDER_DEPLOY_HOOK }} \
            -H "Authorization: Bearer ${{ secrets.RENDER_API_KEY }}"
```

---

## Domain Configuration

### Production Domains

**Frontend (Vercel):**
- Primary: `flash-cart.vercel.app`
- Custom: `app.flashcart.com`

**Backend (Render/Railway):**
- Primary: `flash-cart-api.render.com`
- Custom: `api.flashcart.com`

### Update Frontend After Backend Deployment

Edit `frontend/public/app.js`:
```javascript
const API_BASE_URL = 'https://api.flashcart.com/api';
```

Or set via Vercel environment variable:
```
API_BASE_URL=https://api.flashcart.com/api
```

---

## Database Migrations

After deploying backend, migrations run automatically:
- `spring.jpa.hibernate.ddl-auto=update`

First deployment will create all tables.

To verify:
```bash
# Connect to remote database
psql postgresql://user:pass@host:port/database

# Check tables
\dt
\d expenses  # Describe table
```

---

## Monitoring & Logs

### Vercel
- Dashboard → Deployments
- Analytics → Performance metrics
- Logs → Function logs

### Render/Railway
- Project → Logs tab
- Monitor real-time deployment
- Check build output for errors

---

## Rollback Procedures

### Vercel
1. Go to Deployments
2. Select previous deployment
3. Click three dots → "Redeploy"

### Render
1. Go to "Deploys"
2. Select previous version
3. Click "Deploy"

---

## Common Issues

### Database Connection Timeout
```
PG::UnableToConnect
```
**Solution**: Verify DATABASE_URL and network access

### Java Out of Memory
```
java.lang.OutOfMemoryError
```
**Solution**: Increase heap size in start command:
```
java -Xmx512m -jar target/*.jar
```

### Static Assets 404
**Solution**: Ensure vercel.json rewrites to index.html

---

## Cost Estimation (2024)

- **Vercel Frontend**: Free ($0/month)
- **Render PostgreSQL**: $7+ /month (free for hobby tier)
- **Render Web Service**: $7+ /month (free for hobby tier)
- **Custom Domain**: $12/year

**Total**: Approximately $14-30/month for production

---

## Next Steps After Deployment

1. ✅ Test all endpoints from production domain
2. ✅ Update frontend API_BASE_URL if needed
3. ✅ Set up error monitoring (Sentry, Rollbar)
4. ✅ Configure automatic backups for database
5. ✅ Set up CI/CD for automated testing
6. ✅ Add authentication (JWT/OAuth2)
7. ✅ Implement rate limiting
8. ✅ Set up security headers

---

**Deployment Ready! 🚀**
