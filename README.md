# ERP_management-system
# 🏢 ERP Management System

A full-stack **Enterprise Resource Planning (ERP)** web application built with **Spring Boot** (backend) and **HTML/CSS/JavaScript** (frontend). Manage users, products, suppliers, and inventory — all from a clean, browser-based dashboard.

---

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
- [Default Login Credentials](#-default-login-credentials)
- [API Endpoints](#-api-endpoints)
- [Database](#-database)
- [Running on Another Device](#-running-on-another-device)
- [Screenshots](#-screenshots)

---

## ✨ Features

- 🔐 **User Authentication** – Login/Register with role-based access (Admin / User)
- 👥 **User Management** – Create, update, and delete system users (Admin only)
- 📦 **Product Management** – Add and manage products with category, price, and reorder level
- 🏭 **Supplier Management** – Track supplier contact information and addresses
- 🗃️ **Inventory Management** – Monitor stock quantities and warehouse levels
- 📊 **Dashboard** – Live count of users, products, suppliers, and inventory records
- 🌐 **REST API** – Full CRUD API for all modules with CORS support

---

## 🛠️ Tech Stack

| Layer     | Technology                          |
|-----------|-------------------------------------|
| Backend   | Java 17, Spring Boot 3.2.0          |
| ORM       | Spring Data JPA, Hibernate          |
| Database  | H2 (in-memory, default) / MySQL 8   |
| Frontend  | HTML5, CSS3, Vanilla JavaScript     |
| Build     | Apache Maven 3.9+                   |
| Server    | Embedded Apache Tomcat (port 8080)  |

---

## 📁 Project Structure

```
erp-management-system/
├── src/
│   └── main/
│       ├── java/com/erp/management/
│       │   ├── controller/         # REST API controllers
│       │   │   ├── UserController.java
│       │   │   ├── ProductController.java
│       │   │   ├── SupplierController.java
│       │   │   └── InventoryController.java
│       │   ├── entity/             # JPA entity classes
│       │   ├── repository/         # Spring Data JPA repositories
│       │   ├── service/            # Business logic services
│       │   ├── DatabaseInitializer.java   # Seeds default admin user
│       │   └── ErpManagementSystemApplication.java
│       └── resources/
│           └── application.properties    # App configuration
├── frontend/
│   ├── dashboard.html
│   ├── assets/
│   │   ├── css/style.css
│   │   └── js/                     # JavaScript modules
│   └── modules/
│       ├── users/      (login.html, register.html, user.html)
│       ├── products/   (product.html)
│       ├── suppliers/  (supplier.html)
│       └── inventory/  (inventory.html)
├── database.sql            # Optional: MySQL schema reference
├── run-backend.ps1         # Script to start the backend (Windows)
├── run-frontend.ps1        # Script to start the frontend (Windows)
└── pom.xml
```

---

## ✅ Prerequisites

| Requirement       | Version  | Notes                              |
|-------------------|----------|------------------------------------|
| Java JDK          | 17+      | Required to run Spring Boot        |
| Maven             | 3.6+     | Auto-downloaded by startup script  |
| Python or Node.js | Any      | Required to serve frontend files   |

> **Install Java JDK 17 on Windows (one command):**
> ```powershell
> winget install EclipseAdoptium.Temurin.17.JDK
> ```

---

## 🚀 Getting Started

### Option A – Using the Startup Scripts (Recommended)

**Step 1: Start the Backend**

Open a PowerShell terminal in the project root and run:
```powershell
powershell -ExecutionPolicy Bypass -File run-backend.ps1
```
- Downloads a portable Apache Maven automatically (no installation needed)
- Builds and starts the Spring Boot server on **http://localhost:8080**
- Creates a default Admin user on first launch

**Step 2: Start the Frontend**

Open a **second** PowerShell terminal in the same folder:
```powershell
powershell -ExecutionPolicy Bypass -File run-frontend.ps1
```
- Starts a local HTTP server for the frontend on **http://localhost:8081**
- Automatically opens the login page in your browser

---

### Option B – Manual Commands

**Start Backend:**
```powershell
# Refresh PATH to pick up Java after fresh install
$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")

mvn spring-boot:run
```

**Start Frontend (Python):**
```bash
cd frontend
python -m http.server 8081
```

**Start Frontend (Node.js):**
```bash
cd frontend
npx http-server -p 8081
```

Then open: **http://localhost:8081/modules/users/login.html**

---

## 🔑 Default Login Credentials

On first startup, a default admin account is automatically created:

| Field    | Value             |
|----------|-------------------|
| Email    | `admin@erp.com`   |
| Password | `admin123`        |
| Role     | `ADMIN`           |

> **Note:** Admin users can create, edit, and delete records. Regular users have view-only access.

---

## 🌐 API Endpoints

### Users – `/api/users`
| Method | Endpoint             | Description            |
|--------|----------------------|------------------------|
| GET    | `/api/users`         | Get all users          |
| POST   | `/api/users`         | Create a new user      |
| PUT    | `/api/users/{id}`    | Update a user          |
| DELETE | `/api/users/{id}`    | Delete a user          |
| POST   | `/api/users/login`   | Authenticate user      |
| GET    | `/api/users/count`   | Get total user count   |

### Products – `/api/products`
| Method | Endpoint               | Description             |
|--------|------------------------|-------------------------|
| GET    | `/api/products`        | Get all products        |
| POST   | `/api/products`        | Create a new product    |
| PUT    | `/api/products/{id}`   | Update a product        |
| DELETE | `/api/products/{id}`   | Delete a product        |
| GET    | `/api/products/count`  | Get total product count |

### Suppliers – `/api/suppliers`
| Method | Endpoint                | Description              |
|--------|-------------------------|--------------------------|
| GET    | `/api/suppliers`        | Get all suppliers        |
| POST   | `/api/suppliers`        | Create a new supplier    |
| PUT    | `/api/suppliers/{id}`   | Update a supplier        |
| DELETE | `/api/suppliers/{id}`   | Delete a supplier        |
| GET    | `/api/suppliers/count`  | Get total supplier count |

### Inventory – `/api/inventory`
| Method | Endpoint                | Description               |
|--------|-------------------------|---------------------------|
| GET    | `/api/inventory`        | Get all inventory records |
| POST   | `/api/inventory`        | Create an inventory entry |
| PUT    | `/api/inventory/{id}`   | Update an entry           |
| DELETE | `/api/inventory/{id}`   | Delete an entry           |
| GET    | `/api/inventory/count`  | Get total entry count     |

---

## 🗄️ Database

### Default: H2 In-Memory Database
The application uses an **H2 in-memory database** by default — no installation required. Tables are auto-created at startup.

- **H2 Web Console:** http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:erp_db`
  - Username: `sa`
  - Password: *(leave blank)*

> ⚠️ Data is **not persisted** between restarts when using in-memory mode. To persist data, change the datasource URL in `application.properties` to:
> ```properties
> spring.datasource.url=jdbc:h2:file:./erp_db;DB_CLOSE_DELAY=-1
> ```

### Optional: Switch to MySQL
1. Install MySQL 8 and create the database:
   ```sql
   CREATE DATABASE ERP_Management_System;
   ```
   Or run the included `database.sql` schema file.

2. Update `application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/ERP_Management_System
   spring.datasource.username=root
   spring.datasource.password=your_password
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
   ```

---

## 💻 Running on Another Device

Copy the entire project folder to the target machine and ensure:

1. **Java JDK 17+** is installed
2. **Python** or **Node.js** is available (for the frontend server)
3. Run `run-backend.ps1` and `run-frontend.ps1` as described above

Maven dependencies are downloaded automatically on first run (internet required).

---

## 📄 License

This project is open-source and available for educational and personal use.
