# Environment Configuration Guide

## Backend Setup

1. **Create `.env` file in backend folder:**
   ```bash
   cd backend
   ```

2. **Add the following to `.env` file with your credentials:**
   ```properties
   # Database Configuration (from Supabase)
   DB_URL=jdbc:postgresql://your-host:6543/postgres
   DB_USERNAME=your-username
   DB_PASSWORD=your-password
   
   # Server Configuration
   SERVER_PORT=8080
   
   # CORS Configuration
   CORS_ALLOWED_ORIGINS=http://localhost:3000
   
   # Application Configuration
   APP_NAME=unigear-tracker
   ```

3. **Get your Supabase credentials:**
   - Go to [supabase.com](https://supabase.com)
   - Select your project
   - Navigate to Settings → Database → Connection String
   - Choose "Transaction pooler" mode
   - Copy the connection details

## Frontend Setup

1. **Create `.env` file in web folder:**
   ```bash
   cd web
   ```

2. **Add the following to `.env` file:**
   ```properties
   REACT_APP_API_BASE_URL=http://localhost:8080/api
   REACT_APP_NAME=UniGear Tracker
   REACT_APP_VERSION=1.0.0
   ```

3. **If your backend runs on a different port, update `REACT_APP_API_BASE_URL`**

## Important Notes

- ⚠️ **Never commit `.env` files to version control**
- ✅ `.env` files are already in `.gitignore`
- 🔒 Keep your database passwords secure

## Dependencies Added

### Backend (pom.xml)
- `spring-dotenv` - For loading environment variables from .env files

### Usage
The application automatically loads variables from `.env` files using the spring-dotenv library.
