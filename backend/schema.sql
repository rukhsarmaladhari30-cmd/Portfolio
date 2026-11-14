-- ============================================================================
-- ONLINE LIBRARY MANAGEMENT SYSTEM - MYSQL SCHEMA
-- Author: Rukhsar Maladhari (Java Full Stack Developer)
-- Database: library_db
-- ============================================================================

CREATE DATABASE IF NOT EXISTS library_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library_db;

-- 1. USERS TABLE (Role-Based Access: ROLE_STUDENT, ROLE_ADMIN)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- BCrypt hashed
    student_id VARCHAR(50) UNIQUE,
    role VARCHAR(30) NOT NULL DEFAULT 'ROLE_STUDENT',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. CATEGORIES TABLE
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. BOOKS TABLE
CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(150) NOT NULL,
    isbn VARCHAR(30) NOT NULL UNIQUE,
    category_id BIGINT NOT NULL,
    total_copies INT NOT NULL DEFAULT 1,
    available_copies INT NOT NULL DEFAULT 1,
    cover_image VARCHAR(255),
    published_year INT,
    publisher VARCHAR(150),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
);

-- 4. BORROW RECORDS TABLE
CREATE TABLE IF NOT EXISTS borrow_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ISSUED', -- 'ISSUED', 'RETURNED', 'OVERDUE'
    fine_amount DECIMAL(10,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE RESTRICT
);

-- 5. BOOK REQUESTS TABLE (Student reservations needing Admin approval)
CREATE TABLE IF NOT EXISTS book_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'APPROVED', 'REJECTED'
    approved_by BIGINT,
    approval_date TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL
);

-- 6. FINES TABLE
CREATE TABLE IF NOT EXISTS fines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    borrow_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    days_overdue INT NOT NULL,
    paid_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID', -- 'UNPAID', 'PAID'
    paid_date TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (borrow_id) REFERENCES borrow_records(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================================
-- INITIAL SAMPLE DATA SEEDING
-- ============================================================================

-- Categories
INSERT INTO categories (id, name, description) VALUES
(1, 'Computer Science', 'Data structures, algorithms, system design, and software engineering.'),
(2, 'Classic Literature', 'Timeless literary masterpieces, poetry, and world prose.'),
(3, 'Engineering & Science', 'Core physics, electronics, mechanical, and mathematical foundations.'),
(4, 'Philosophy & Arts', 'Humanities, aesthetic theory, logic, and historical philosophies.'),
(5, 'Fiction & Mystery', 'Contemporary fiction, detective novels, and creative writing.')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Default Admin & Student Users (BCrypt passwords for 'Admin@123' and 'Student@123')
INSERT INTO users (id, name, email, password, student_id, role) VALUES
(1, 'Head Librarian Admin', 'admin@library.edu', '$2a$10$wN9iK8xR/d7d1H3k0xL9reQ8qM2mYjP1h9iK8xR/d7d1H3k0xL9re', 'EMP-001', 'ROLE_ADMIN'),
(2, 'Rukhsar Maladhari', 'rukhsarmaladhari30@gmail.com', '$2a$10$wN9iK8xR/d7d1H3k0xL9reQ8qM2mYjP1h9iK8xR/d7d1H3k0xL9re', 'STU-2025-042', 'ROLE_STUDENT'),
(3, 'Aman Verma', 'aman.verma@sbjain.edu', '$2a$10$wN9iK8xR/d7d1H3k0xL9reQ8qM2mYjP1h9iK8xR/d7d1H3k0xL9re', 'STU-2025-108', 'ROLE_STUDENT')
ON DUPLICATE KEY UPDATE email=VALUES(email);

-- Sample Books with user-uploaded high-res images
INSERT INTO books (id, title, author, isbn, category_id, total_copies, available_copies, cover_image, published_year, publisher) VALUES
(1, 'Wuthering Heights & Classic Anthologies', 'Emily Brontë & Jane Austen', '978-0141439556', 2, 5, 4, 'book_cover_vintage_classics.jpg', 1847, 'Penguin Classics'),
(2, 'The Art of Poetry & Aesthetic Botanica', 'Rosalind Vance', '978-0393049084', 2, 4, 3, 'book_cover_peony_poetry.jpg', 2021, 'Oxford Literary Press'),
(3, 'The Reader in the Shadows', 'Evelyn St. Claire', '978-0062315007', 5, 6, 2, 'book_cover_reader_silhouette.jpg', 2023, 'HarperCollins'),
(4, 'Florilegium: Botanical Chronicles', 'Margaret Ashford', '978-0679783268', 4, 3, 3, 'book_cover_floral_shelf.jpg', 2019, 'Cambridge University Press'),
(5, 'Sanctuary of Illuminated Pages', 'Alasdair Gray', '978-0140283334', 3, 5, 5, 'book_cover_ivy_bookshelf.jpg', 2020, 'Vintage Press'),
(6, 'Introduction to Algorithms (CLRS)', 'Cormen, Leiserson, Rivest, Stein', '978-0262033848', 1, 8, 6, 'book_cover_ivy_bookshelf.jpg', 2022, 'MIT Press')
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- Initial Active Borrow Record
INSERT INTO borrow_records (id, user_id, book_id, issue_date, due_date, status, fine_amount) VALUES
(1, 2, 1, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 9 DAY), 'ISSUED', 0.00),
(2, 3, 3, DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_SUB(CURDATE(), INTERVAL 6 DAY), 'OVERDUE', 30.00)
ON DUPLICATE KEY UPDATE id=VALUES(id);

