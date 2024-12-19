package org.example.onlineegyetemirendszer;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PostConstruct;
import java.util.*;

@RestController
@RequestMapping("/api")
public class BackendManagement {

    private final JdbcTemplate jdbcTemplate;

    public BackendManagement(@Lazy JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initializeDatabase() {

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS admins (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL UNIQUE, password TEXT NOT NULL)");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL UNIQUE, password TEXT NOT NULL)");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS exams (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE)");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS registrations (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, exam_id INTEGER NOT NULL, FOREIGN KEY (user_id) REFERENCES users (id), FOREIGN KEY (exam_id) REFERENCES exams (id))");


        jdbcTemplate.update("INSERT OR IGNORE INTO admins (username, password) VALUES (?, ?)", "ugyintezo1", "ugyintezojelszo1");
        jdbcTemplate.update("INSERT OR IGNORE INTO admins (username, password) VALUES (?, ?)", "ugyintezo2", "ugyintezojelszo2");
        jdbcTemplate.update("INSERT OR IGNORE INTO admins (username, password) VALUES (?, ?)", "ugyintezo3", "ugyintezojelszo3");

        jdbcTemplate.update("INSERT OR IGNORE INTO users (username, password) VALUES (?, ?)", "diak1", "jelszo1");
        jdbcTemplate.update("INSERT OR IGNORE INTO users (username, password) VALUES (?, ?)", "diak2", "jelszo2");
        jdbcTemplate.update("INSERT OR IGNORE INTO users (username, password) VALUES (?, ?)", "diak3", "jelszo3");

        jdbcTemplate.update("INSERT OR IGNORE INTO exams (name) VALUES (?)", "Analízis");
        jdbcTemplate.update("INSERT OR IGNORE INTO exams (name) VALUES (?)", "Programozás");
        jdbcTemplate.update("INSERT OR IGNORE INTO exams (name) VALUES (?)", "Fizika");
        jdbcTemplate.update("INSERT OR IGNORE INTO exams (name) VALUES (?)", "Kémia");
        jdbcTemplate.update("INSERT OR IGNORE INTO exams (name) VALUES (?)", "Angol");
    }

    @PostMapping("/admin/login")
    public Map<String, String> adminLogin(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        try {
            Map<String, Object> admin = jdbcTemplate.queryForMap(
                    "SELECT id, password FROM admins WHERE username = ?",
                    username
            );

            if (password.equals(admin.get("password"))) {
                return Map.of("message", "Sikeres bejelentkezés!", "adminId", admin.get("id").toString());
            } else {
                throw new RuntimeException("Hibás jelszó!");
            }
        } catch (Exception e) {
            throw new RuntimeException("Felhasználó nem található!");
        }
    }

    @PostMapping("/login")
    public Map<String, String> userLogin(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        try {
            Map<String, Object> user = jdbcTemplate.queryForMap(
                    "SELECT id, password FROM users WHERE username = ?",
                    username
            );

            if (password.equals(user.get("password"))) {
                return Map.of("message", "Sikeres bejelentkezés!", "userId", user.get("id").toString());
            } else {
                throw new RuntimeException("Hibás jelszó!");
            }
        } catch (Exception e) {
            throw new RuntimeException("Felhasználó nem található!");
        }
    }

    @GetMapping("/admin/registrations")
    public List<Map<String, Object>> getAllRegistrations() {
        return jdbcTemplate.queryForList(
                "SELECT r.id AS registrationId, u.username AS studentName, e.name AS examName " +
                        "FROM registrations r " +
                        "JOIN users u ON r.user_id = u.id " +
                        "JOIN exams e ON r.exam_id = e.id"
        );
    }

    @DeleteMapping("/admin/unregister/{registrationId}")
    public Map<String, String> deleteRegistration(@PathVariable int registrationId) {
        int affectedRows = jdbcTemplate.update(
                "DELETE FROM registrations WHERE id = ?",
                registrationId
        );

        if (affectedRows > 0) {
            return Map.of("message", "A vizsgajelentkezés sikeresen törölve.");
        } else {
            throw new RuntimeException("Nem található ilyen vizsgajelentkezés.");
        }
    }

    @PostMapping("/admin/create-exam")
    public Map<String, String> createExam(@RequestBody Map<String, String> request) {
        String examName = request.get("name");
        if (examName == null || examName.isEmpty()) {
            throw new RuntimeException("A vizsga neve nem lehet üres.");
        }

        jdbcTemplate.update("INSERT INTO exams (name) VALUES (?)", examName);
        return Map.of("message", "A vizsga sikeresen létrehozva!", "examName", examName);
    }

    @GetMapping("/exams")
    public List<Map<String, Object>> getAvailableExams(@RequestParam int userId) {
        return jdbcTemplate.queryForList(
                "SELECT e.id, e.name FROM exams e " +
                        "WHERE e.id NOT IN (SELECT r.exam_id FROM registrations r WHERE r.user_id = ?)",
                userId
        );
    }

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody Map<String, String> request) {
        int userId = Integer.parseInt(request.get("userId"));
        int examId = Integer.parseInt(request.get("examId"));

        jdbcTemplate.update("INSERT INTO registrations (user_id, exam_id) VALUES (?, ?)", userId, examId);
        return Map.of("message", "Sikeres jelentkezés a vizsgára!");
    }

    @GetMapping("/my-exams")
    public List<Map<String, Object>> getMyExams(@RequestParam int userId) {
        return jdbcTemplate.queryForList(
                "SELECT e.id, e.name FROM registrations r " +
                        "JOIN exams e ON r.exam_id = e.id WHERE r.user_id = ?",
                userId
        );
    }

    @PostMapping("/unregister")
    public Map<String, String> unregister(@RequestBody Map<String, String> request) {
        int userId = Integer.parseInt(request.get("userId"));
        int examId = Integer.parseInt(request.get("examId"));

        jdbcTemplate.update("DELETE FROM registrations WHERE user_id = ? AND exam_id = ?", userId, examId);
        return Map.of("message", "Vizsgajelentkezés törölve!");
    }
}
