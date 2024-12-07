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
        // Admin felhasználók
        jdbcTemplate.update("INSERT OR IGNORE INTO admins (username, password) VALUES (?, ?)", "ugyintezo1", "ugyintezojelszo1");
        jdbcTemplate.update("INSERT OR IGNORE INTO admins (username, password) VALUES (?, ?)", "ugyintezo2", "ugyintezojelszo2");
        jdbcTemplate.update("INSERT OR IGNORE INTO admins (username, password) VALUES (?, ?)", "ugyintezo3", "ugyintezojelszo3");

        // Diák felhasználók
        jdbcTemplate.update("INSERT OR IGNORE INTO users (username, password) VALUES (?, ?)", "diak1", "jelszo1");
        jdbcTemplate.update("INSERT OR IGNORE INTO users (username, password) VALUES (?, ?)", "diak2", "jelszo2");
        jdbcTemplate.update("INSERT OR IGNORE INTO users (username, password) VALUES (?, ?)", "diak3", "jelszo3");
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

    @GetMapping("/admin/enrollments")
    public List<Map<String, Object>> getAllEnrollments() {
        return jdbcTemplate.queryForList(
                "SELECT e.id AS enrollmentId, u.username AS studentName, c.name AS courseName " +
                        "FROM enrollments e " +
                        "JOIN users u ON e.user_id = u.id " +
                        "JOIN courses c ON e.course_id = c.id"
        );
    }

    @DeleteMapping("/admin/unenroll/{enrollmentId}")
    public Map<String, String> deleteEnrollment(@PathVariable int enrollmentId) {
        int affectedRows = jdbcTemplate.update(
                "DELETE FROM enrollments WHERE id = ?",
                enrollmentId
        );

        if (affectedRows > 0) {
            return Map.of("message", "A beiratkozás sikeresen törölve.");
        } else {
            throw new RuntimeException("Nem található ilyen beiratkozás.");
        }
    }

    @GetMapping("/courses")
    public List<Map<String, Object>> getAvailableCourses(@RequestParam int userId) {
        return jdbcTemplate.queryForList(
                "SELECT c.id, c.name FROM courses c " +
                        "WHERE c.id NOT IN (SELECT e.course_id FROM enrollments e WHERE e.user_id = ?)",
                userId
        );
    }

    @PostMapping("/enroll")
    public Map<String, String> enroll(@RequestBody Map<String, String> request) {
        int userId = Integer.parseInt(request.get("userId"));
        int courseId = Integer.parseInt(request.get("courseId"));

        jdbcTemplate.update("INSERT INTO enrollments (user_id, course_id) VALUES (?, ?)", userId, courseId);
        return Map.of("message", "Sikeres jelentkezés!");
    }

    @GetMapping("/my-courses")
    public List<Map<String, Object>> getMyCourses(@RequestParam int userId) {
        return jdbcTemplate.queryForList(
                "SELECT c.id, c.name FROM enrollments e " +
                        "JOIN courses c ON e.course_id = c.id WHERE e.user_id = ?",
                userId
        );
    }

    @PostMapping("/unenroll")
    public Map<String, String> unenroll(@RequestBody Map<String, String> request) {
        int userId = Integer.parseInt(request.get("userId"));
        int courseId = Integer.parseInt(request.get("courseId"));

        jdbcTemplate.update("DELETE FROM enrollments WHERE user_id = ? AND course_id = ?", userId, courseId);
        return Map.of("message", "Kurzus törölve a felvett kurzusok közül!");
    }
}
