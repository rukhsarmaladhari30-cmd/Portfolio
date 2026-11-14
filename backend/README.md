# Online Library Management System — Java Full Stack

> **Author**: Rukhsar Maladhari (Java Full Stack Developer)  
> **Tech Stack**: Java 17 LTS, Spring Boot 3.2, Spring Security, Spring Data JPA / Hibernate, MySQL 8+, React.js / Vanilla ES6+, CSS3 Modern Grid & Flexbox.

---

## 🏛 Architecture Overview

```
Frontend (React / Modern Web Dashboard)
       │
       │ HTTP / REST APIs (JSON + JWT Bearer Auth)
       ▼
Spring Boot REST Controllers (/api/auth, /api/books, /api/borrow, /api/admin)
       │
Spring Security & Role-Based Access Control (ROLE_STUDENT, ROLE_ADMIN)
       │
Service Layer (BookService, BorrowService, AdminService, UserService)
       │
Spring Data JPA Repositories
       │
Hibernate ORM
       │
MySQL Relational Database (library_db)
```

---

## 🚀 Quick Setup & Execution

### 1. MySQL Database Initialization
Ensure your MySQL Server is running (default port `3306`). Execute the schema script:

```bash
mysql -u root -p < schema.sql
```
This script creates the `library_db` database, tables (`users`, `categories`, `books`, `borrow_records`, `book_requests`, `fines`), constraints, indexes, and initial seeds.

### 2. Configure Database Credentials
Edit `src/main/resources/application.properties` to set your MySQL password:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### 3. Build & Run with Maven
```bash
# Package the JAR file
mvn clean package -DskipTests

# Run the Spring Boot Application
mvn spring-boot:run
```
Alternatively, run the packaged JAR:
```bash
java -jar target/online-library-system-1.0.0.jar
```
The API starts on: **`http://localhost:8080/api`**.

---

## 🔑 Default Credentials & Role Accounts

| Role | Email | Password | Student ID / Code | Permissions |
| :--- | :--- | :--- | :--- | :--- |
| **Admin** | `admin@library.edu` | `Admin@2026` | `ADM-101` | Full CRUD on books, approve requests, monitor analytics, manage students, clear fines |
| **Student** | `rukhsar@student.edu` | `Student@2026` | `STU-2025-084` | Browse catalog, search books, borrow books, view due dates & fines, return books |
| **Student** | `alex@student.edu` | `Student@2026` | `STU-2025-102` | Issue/Return, reservations, borrow history |

---

## 📡 REST API Reference

### Authentication
- `POST /api/auth/register` — Register a new student or admin.
- `POST /api/auth/login` — Authenticate and receive JWT token.

### Book Management
- `GET /api/books` — Retrieve all catalog books.
- `GET /api/books/{id}` — Fetch specific book details.
- `GET /api/books/search?q={query}` — Search books by title, author, category, or ISBN.
- `POST /api/books` — (Admin) Add new book with copy count.
- `PUT /api/books/{id}` — (Admin) Update book metadata & copies.
- `DELETE /api/books/{id}` — (Admin) Remove book from catalog.

### Issue & Return Workflow
- `POST /api/borrow/issue` — Issue book copy to student (sets 14-day due date, decrements copies).
- `POST /api/borrow/return/{borrowId}` — Return book copy, auto-calculate overdue days & fines at ₹5/day, increment available copies.
- `GET /api/borrow/user/{userId}/active` — View active borrowings for student.
- `GET /api/borrow/user/{userId}` — View complete borrowing history.
- `GET /api/borrow/overdue` — (Admin) View overdue loans requiring attention.

### Admin Dashboard & Analytics
- `GET /api/admin/stats` — Real-time analytics (total books, registered students, active loans, overdue, fines, category distribution).
- `GET /api/admin/requests` — View pending student book requests.
- `POST /api/admin/requests/{requestId}/action?adminId={id}&approve={true/false}` — Approve or reject requests.
- `GET /api/admin/students` — List all registered students and their standing.

---

## 🧪 Testing with cURL

```bash
# 1. Login as Admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@library.edu","password":"Admin@2026"}'

# 2. Search Books
curl -X GET "http://localhost:8080/api/books/search?q=Algorithms"

# 3. Issue Book to Student
curl -X POST http://localhost:8080/api/borrow/issue \
  -H "Content-Type: application/json" \
  -d '{"userId":2,"bookId":1}'

# 4. Return Book
curl -X POST http://localhost:8080/api/borrow/return/1
```

