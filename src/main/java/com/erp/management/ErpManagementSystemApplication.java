package com.erp.management;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@SpringBootApplication
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:8081", "http://127.0.0.1:8081", "http://localhost:5500", "http://127.0.0.1:5500"})
public class ErpManagementSystemApplication implements CommandLineRunner {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    @Value("${ERP_ADMIN_EMAIL:}")
    private String adminEmail;

    @Value("${ERP_ADMIN_PASSWORD:}")
    private String adminPassword;

    public ErpManagementSystemApplication(JdbcTemplate jdbc, PasswordEncoder passwordEncoder) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
    }

    public static void main(String[] args) {
        SpringApplication.run(ErpManagementSystemApplication.class, args);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void run(String... args) {
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            System.out.println("ERP demo admin is not seeded. Set ERP_ADMIN_EMAIL and ERP_ADMIN_PASSWORD to create it.");
            return;
        }
        if (adminPassword.length() < 10) {
            throw new IllegalStateException("ERP_ADMIN_PASSWORD must contain at least 10 characters.");
        }
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM users_erp WHERE email = ?", Integer.class, adminEmail);
        if (count == null || count == 0) {
            String username = adminEmail.substring(0, adminEmail.indexOf('@'));
            jdbc.update(
                "INSERT INTO users_erp (username, email, password_hash, role) VALUES (?, ?, ?, ?)",
                username, adminEmail.toLowerCase(), passwordEncoder.encode(adminPassword), "ADMIN"
            );
            System.out.println("ERP admin account was created from environment configuration.");
        }
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Integer result = jdbc.queryForObject("SELECT 1", Integer.class);
        return Map.of("status", "ok", "database", result != null ? "connected" : "unknown");
    }

    @PostMapping("/users/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> body) {
        String email = text(body, "email").toLowerCase();
        String password = text(body, "password");
        if (!EMAIL.matcher(email).matches() || password.isBlank()) {
            throw badRequest("Enter a valid email and password.");
        }
        List<Map<String, Object>> rows = jdbc.queryForList(
            "SELECT user_id, username, email, password_hash, role FROM users_erp WHERE email = ?",
            email
        );
        if (rows.isEmpty() || !passwordEncoder.matches(password, String.valueOf(rows.get(0).get("password_hash")))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }
        Map<String, Object> row = rows.get(0);
        return Map.of(
            "success", true,
            "user", Map.of(
                "id", row.get("user_id"),
                "username", row.get("username"),
                "email", row.get("email"),
                "role", row.get("role")
            )
        );
    }

    @GetMapping("/users")
    public List<Map<String, Object>> users() {
        return jdbc.queryForList("SELECT user_id AS id, username, email, role, created_at FROM users_erp ORDER BY user_id");
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createUser(@RequestBody Map<String, Object> body) {
        String username = required(body, "username");
        String email = text(body, "email").toLowerCase();
        String password = required(body, "password");
        String role = text(body, "role").isBlank() ? "USER" : text(body, "role").toUpperCase();
        if (!EMAIL.matcher(email).matches() || password.length() < 8 || !List.of("ADMIN", "USER").contains(role)) {
            throw badRequest("Enter a valid email, password (8+ characters) and role (ADMIN or USER).");
        }
        jdbc.update(
            "INSERT INTO users_erp (username, email, password_hash, role) VALUES (?, ?, ?, ?)",
            username, email, passwordEncoder.encode(password), role
        );
        return Map.of("success", true);
    }

    @PutMapping("/users/{id}")
    public Map<String, Object> updateUser(@PathVariable long id, @RequestBody Map<String, Object> body) {
        String username = required(body, "username");
        String email = text(body, "email").toLowerCase();
        String role = text(body, "role").toUpperCase();
        if (!EMAIL.matcher(email).matches() || !List.of("ADMIN", "USER").contains(role)) {
            throw badRequest("Enter a valid email and role (ADMIN or USER).");
        }
        String password = text(body, "password");
        int changed;
        if (password.isBlank()) {
            changed = jdbc.update("UPDATE users_erp SET username=?, email=?, role=? WHERE user_id=?", username, email, role, id);
        } else {
            if (password.length() < 8) throw badRequest("Password must contain at least 8 characters.");
            changed = jdbc.update("UPDATE users_erp SET username=?, email=?, role=?, password_hash=? WHERE user_id=?",
                username, email, role, passwordEncoder.encode(password), id);
        }
        ensureUpdated(changed, "User");
        return Map.of("success", true);
    }

    @DeleteMapping("/users/{id}")
    public Map<String, Object> deleteUser(@PathVariable long id) {
        ensureUpdated(jdbc.update("DELETE FROM users_erp WHERE user_id=?", id), "User");
        return Map.of("success", true);
    }

    @GetMapping("/users/count")
    public Map<String, Object> userCount() {
        return count("users_erp");
    }

    @GetMapping("/products")
    public List<Map<String, Object>> products() {
        return jdbc.queryForList(
            "SELECT p.product_id AS id, p.product_name AS name, p.category, p.description, p.price, p.reorder_level, p.supplier_id, s.supplier_name " +
            "FROM products_erp p LEFT JOIN suppliers_erp s ON s.supplier_id=p.supplier_id ORDER BY p.product_id"
        );
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createProduct(@RequestBody Map<String, Object> body) {
        BigDecimal price = decimal(body, "price");
        int reorderLevel = integer(body, "reorder_level", 0);
        if (price.signum() < 0 || reorderLevel < 0) throw badRequest("Price and reorder level cannot be negative.");
        Long supplierId = optionalLong(body, "supplier_id");
        jdbc.update(
            "INSERT INTO products_erp (product_name, category, description, price, reorder_level, supplier_id) VALUES (?, ?, ?, ?, ?, ?)",
            required(body, "name"), text(body, "category"), text(body, "description"), price, reorderLevel, supplierId
        );
        return Map.of("success", true);
    }

    @PutMapping("/products/{id}")
    public Map<String, Object> updateProduct(@PathVariable long id, @RequestBody Map<String, Object> body) {
        BigDecimal price = decimal(body, "price");
        int reorderLevel = integer(body, "reorder_level", 0);
        if (price.signum() < 0 || reorderLevel < 0) throw badRequest("Price and reorder level cannot be negative.");
        Long supplierId = optionalLong(body, "supplier_id");
        int changed = jdbc.update(
            "UPDATE products_erp SET product_name=?, category=?, description=?, price=?, reorder_level=?, supplier_id=? WHERE product_id=?",
            required(body, "name"), text(body, "category"), text(body, "description"), price, reorderLevel, supplierId, id
        );
        ensureUpdated(changed, "Product");
        return Map.of("success", true);
    }

    @DeleteMapping("/products/{id}")
    public Map<String, Object> deleteProduct(@PathVariable long id) {
        ensureUpdated(jdbc.update("DELETE FROM products_erp WHERE product_id=?", id), "Product");
        return Map.of("success", true);
    }

    @GetMapping("/products/count")
    public Map<String, Object> productCount() {
        return count("products_erp");
    }

    @GetMapping("/suppliers")
    public List<Map<String, Object>> suppliers() {
        return jdbc.queryForList(
            "SELECT supplier_id AS id, supplier_name AS name, contact_person, phone, email, address FROM suppliers_erp ORDER BY supplier_id"
        );
    }

    @PostMapping("/suppliers")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createSupplier(@RequestBody Map<String, Object> body) {
        jdbc.update(
            "INSERT INTO suppliers_erp (supplier_name, contact_person, phone, email, address) VALUES (?, ?, ?, ?, ?)",
            required(body, "name"), text(body, "contact_person"), text(body, "phone"),
            text(body, "email"), text(body, "address")
        );
        return Map.of("success", true);
    }

    @PutMapping("/suppliers/{id}")
    public Map<String, Object> updateSupplier(@PathVariable long id, @RequestBody Map<String, Object> body) {
        int changed = jdbc.update(
            "UPDATE suppliers_erp SET supplier_name=?, contact_person=?, phone=?, email=?, address=? WHERE supplier_id=?",
            required(body, "name"), text(body, "contact_person"), text(body, "phone"),
            text(body, "email"), text(body, "address"), id
        );
        ensureUpdated(changed, "Supplier");
        return Map.of("success", true);
    }

    @DeleteMapping("/suppliers/{id}")
    public Map<String, Object> deleteSupplier(@PathVariable long id) {
        ensureUpdated(jdbc.update("DELETE FROM suppliers_erp WHERE supplier_id=?", id), "Supplier");
        return Map.of("success", true);
    }

    @GetMapping("/suppliers/count")
    public Map<String, Object> supplierCount() {
        return count("suppliers_erp");
    }

    @GetMapping("/inventory")
    public List<Map<String, Object>> inventory() {
        return jdbc.queryForList(
            "SELECT i.inventory_id AS id, i.product_id, p.product_name, i.quantity, p.reorder_level, i.last_updated " +
            "FROM inventory_erp i INNER JOIN products_erp p ON p.product_id=i.product_id ORDER BY i.inventory_id"
        );
    }

    @PostMapping("/inventory")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createInventory(@RequestBody Map<String, Object> body) {
        long productId = positiveLong(body, "product_id");
        int quantity = integer(body, "quantity", -1);
        if (quantity < 0) throw badRequest("Quantity cannot be negative.");
        jdbc.update(
            "INSERT INTO inventory_erp (product_id, quantity) VALUES (?, ?) " +
            "ON DUPLICATE KEY UPDATE quantity=VALUES(quantity), last_updated=CURRENT_TIMESTAMP",
            productId, quantity
        );
        return Map.of("success", true);
    }

    @PutMapping("/inventory/{id}")
    public Map<String, Object> updateInventory(@PathVariable long id, @RequestBody Map<String, Object> body) {
        int quantity = integer(body, "quantity", -1);
        if (quantity < 0) throw badRequest("Quantity cannot be negative.");
        ensureUpdated(jdbc.update(
            "UPDATE inventory_erp SET quantity=?, last_updated=CURRENT_TIMESTAMP WHERE inventory_id=?",
            quantity, id
        ), "Inventory record");
        return Map.of("success", true);
    }

    @DeleteMapping("/inventory/{id}")
    public Map<String, Object> deleteInventory(@PathVariable long id) {
        ensureUpdated(jdbc.update("DELETE FROM inventory_erp WHERE inventory_id=?", id), "Inventory record");
        return Map.of("success", true);
    }

    @GetMapping("/inventory/count")
    public Map<String, Object> inventoryCount() {
        return count("inventory_erp");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> duplicateKey() {
        return Map.of("error", "A record with the same unique value already exists.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public org.springframework.http.ResponseEntity<Map<String, String>> handleStatus(ResponseStatusException exception) {
        return org.springframework.http.ResponseEntity.status(exception.getStatusCode())
            .body(Map.of("error", exception.getReason() == null ? "Request failed." : exception.getReason()));
    }

    private Map<String, Object> count(String table) {
        Integer value = jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
        return Map.of("count", value == null ? 0 : value);
    }

    private static String text(Map<String, Object> body, String key) {
        Object value = body.get(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static String required(Map<String, Object> body, String key) {
        String value = text(body, key);
        if (value.isBlank()) throw badRequest(key + " is required.");
        return value;
    }

    private static BigDecimal decimal(Map<String, Object> body, String key) {
        try {
            return new BigDecimal(required(body, key));
        } catch (NumberFormatException exception) {
            throw badRequest(key + " must be a valid number.");
        }
    }

    private static int integer(Map<String, Object> body, String key, int defaultValue) {
        String value = text(body, key);
        if (value.isBlank() && defaultValue >= 0) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw badRequest(key + " must be a whole number.");
        }
    }

    private static long positiveLong(Map<String, Object> body, String key) {
        try {
            long value = Long.parseLong(required(body, key));
            if (value <= 0) throw badRequest(key + " must be greater than zero.");
            return value;
        } catch (NumberFormatException exception) {
            throw badRequest(key + " must be a whole number.");
        }
    }

    private static Long optionalLong(Map<String, Object> body, String key) {
        String value = text(body, key);
        if (value.isBlank()) return null;
        try {
            long parsed = Long.parseLong(value);
            if (parsed <= 0) throw badRequest(key + " must be greater than zero.");
            return parsed;
        } catch (NumberFormatException exception) {
            throw badRequest(key + " must be a whole number.");
        }
    }

    private static void ensureUpdated(int changed, String entity) {
        if (changed == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, entity + " not found.");
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
