# UniGear Tracker - Backend

Spring Boot backend service for the UniGear Tracker System.

## Technology Stack

- Java 19
- Spring Boot 3.2.0
- Spring Data JPA
- Supabase (PostgreSQL)
- BCrypt for password hashing
- Maven

## Database Setup

### Supabase PostgreSQL

1. **Create Supabase Project**
   - Go to [supabase.com](https://supabase.com)
   - Sign up/Login
   - Click "New Project"
   - Enter project name: `unigear-tracker`
   - Create a strong database password
   - Select a region close to you
   - Click "Create new project"

2. **Get Database Credentials**
   - Go to **Settings** > **Database**
   - Scroll to **Connection String** section
   - Copy the **URI** connection string
   - Note your database password

3. **Configure Application**
   Edit `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://db.YOUR_PROJECT_REF.supabase.co:5432/postgres
   spring.datasource.username=postgres
   spring.datasource.password=YOUR_SUPABASE_PASSWORD
   ```
   
   Replace:
   - `YOUR_PROJECT_REF` with your Supabase project reference
   - `YOUR_SUPABASE_PASSWORD` with your database password

## Running the Application

### Prerequisites
- Java 19 or higher
- Maven
- Supabase account and project

### Steps

1. **Configure Supabase Connection**
   Edit `src/main/resources/application.properties` with your Supabase credentials:
   ```properties
   spring.datasource.url=jdbc:postgresql://db.YOUR_PROJECT_REF.supabase.co:5432/postgres
   spring.datasource.username=postgres
   spring.datasource.password=YOUR_SUPABASE_PASSWORD
   ```

2. **Build the Project**
   ```bash
   mvn clean install
   ```

3. **Run the Application**
   ```bash
   mvn spring-boot:run
   ```

   Or run directly:
   ```bash
   java -jar target/tracker-backend-1.0.0.jar
   ```

4. **Application will start on**: `http://localhost:8080`

5. **Verify Database**
   - Tables will be auto-created by Hibernate
   - Check Supabase Dashboard > **Table Editor** to see the `users` table

## API Endpoints

### Authentication

#### Register User
- **Endpoint**: `POST /api/auth/register`
- **Body**:
  ```json
  {
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123"
  }
  ```
- **Response**: User object with authentication details

#### Login User
- **Endpoint**: `POST /api/auth/login`
- **Body**:
  ```json
  {
    "email": "john@example.com",
    "password": "password123"
  }
  ```
- **Response**: User object with authentication details

## Database Schema

### users Table
| Column     | Type         | Constraints           |
|------------|--------------|----------------------|
| id         | BIGINT       | PRIMARY KEY, AUTO_INCREMENT |
| name       | VARCHAR(255) | NOT NULL             |
| email      | VARCHAR(255) | NOT NULL, UNIQUE     |
| password   | VARCHAR(255) | NOT NULL (hashed)    |
| created_at | TIMESTAMP    | NOT NULL, DEFAULT NOW |

## Security Features

- Passwords are hashed using BCrypt
- Email uniqueness validation
- Input validation on all endpoints
- CORS enabled for frontend communication
