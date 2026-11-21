# ERP Management System

A learning-focused ERP application with a Spring Boot REST API and a static HTML/CSS/JavaScript frontend. It manages users, products, suppliers and inventory using JDBC.

## Features

- Dashboard counts for users, products, suppliers and inventory records
- User creation, update, deletion and demo login
- Product management with category, price, reorder level and optional supplier
- Supplier contact management
- Inventory quantities linked to products
- Parameterized JDBC queries, input validation and BCrypt password hashes
- Persistent H2 database by default; MySQL can be configured for local use

## Tech stack

Java 17, Spring Boot 3.2, Spring Web, Spring JDBC, H2/MySQL, HTML, CSS and vanilla JavaScript.

## Requirements

- JDK 17+
- Maven 3.9+
- Python 3 (to serve the static frontend) or any static HTTP server

## Run locally

### 1. Start the backend

From the repository root:

```powershell
mvn spring-boot:run
```

or:

```bash
mvn spring-boot:run
```

The backend listens on http://localhost:8080. The default H2 database is stored in the local `data/` folder and schema tables are created on first launch. H2 console (local development only): http://localhost:8080/h2-console.

### 2. Optional demo admin

The application does not contain a hard-coded default password. To create the first admin account, set both environment variables before the first backend start. The app hashes the password with BCrypt and creates the account only when that email does not already exist.

**PowerShell**
```powershell
$env:ERP_ADMIN_EMAIL = "admin@example.local"
$env:ERP_ADMIN_PASSWORD = "Choose-A-Private-Password-123"
mvn spring-boot:run
```

**macOS / Linux**
```bash
export ERP_ADMIN_EMAIL="admin@example.local"
export ERP_ADMIN_PASSWORD="Choose-A-Private-Password-123"
mvn spring-boot:run
```

Use the same email and password in the demo login page.

### 3. Serve the frontend

Open a second terminal:

```powershell
cd frontend
py -m http.server 8081
```

On macOS/Linux use `python3 -m http.server 8081`. Open http://localhost:8081/modules/users/login.html.

## MySQL configuration

To use MySQL instead of the default H2 file database:

1. Create the database and schema using `database.sql`.
2. Set the datasource variables when starting Spring Boot:

**PowerShell**
```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://localhost:3306/ERP_Management_System"
$env:SPRING_DATASOURCE_USERNAME = "root"
$env:SPRING_DATASOURCE_PASSWORD = "your_local_password"
$env:SPRING_DATASOURCE_DRIVER = "com.mysql.cj.jdbc.Driver"
$env:SPRING_SQL_INIT_MODE = "never"
mvn spring-boot:run
```

The MySQL schema is imported separately; schema auto-initialization is disabled for this mode.

## API endpoints

| Resource | Methods | Endpoint |
|---|---|---|
| Health | GET | `/api/health` |
| Users | GET, POST | `/api/users` |
| User | PUT, DELETE | `/api/users/{id}` |
| Demo login | POST | `/api/users/login` |
| User count | GET | `/api/users/count` |
| Products | GET, POST | `/api/products` |
| Product | PUT, DELETE | `/api/products/{id}` |
| Product count | GET | `/api/products/count` |
| Suppliers | GET, POST | `/api/suppliers` |
| Supplier | PUT, DELETE | `/api/suppliers/{id}` |
| Supplier count | GET | `/api/suppliers/count` |
| Inventory | GET, POST | `/api/inventory` |
| Inventory row | PUT, DELETE | `/api/inventory/{id}` |
| Inventory count | GET | `/api/inventory/count` |

## Project structure

```
src/main/java/com/erp/management/ErpManagementSystemApplication.java
src/main/resources/application.properties
src/main/resources/schema.sql
frontend/
  dashboard.html
  assets/css/style.css
  assets/js/app.js
  modules/users/
  modules/products/
  modules/suppliers/
  modules/inventory/
database.sql
pom.xml
```

## Security limitations

The login flow is a demonstration only: the frontend stores a user marker in localStorage and the CRUD API does not enforce authenticated server-side sessions or role-based authorization. Do not expose this prototype to the public internet or use it for real business records without adding Spring Security, server-side sessions or signed tokens, CSRF protection, HTTPS, security headers, audit logs and production credential management. The H2 console should also remain disabled outside local development.
