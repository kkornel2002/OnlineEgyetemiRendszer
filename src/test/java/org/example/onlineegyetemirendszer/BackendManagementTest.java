package org.example.onlineegyetemirendszer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BackendManagementTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private BackendManagement backendManagement;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void adminLogin_ValidCredentials_ReturnsSuccess() {
        String username = "ugyintezo1";
        String password = "ugyintezojelszo1";

        when(jdbcTemplate.queryForMap("SELECT id, password FROM admins WHERE username = ?", username))
                .thenReturn(Map.of("id", 1, "password", password));

        Map<String, String> response = backendManagement.adminLogin(Map.of("username", username, "password", password));

        assertEquals("Sikeres bejelentkezés!", response.get("message"));
        assertEquals("1", response.get("adminId"));
    }

    @Test
    void adminLogin_InvalidPassword_ThrowsException() {
        String username = "ugyintezo1";

        when(jdbcTemplate.queryForMap("SELECT id, password FROM admins WHERE username = ?", username))
                .thenReturn(Map.of("id", 1, "password", "wrongpassword"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                backendManagement.adminLogin(Map.of("username", username, "password", "ugyintezojelszo1")));

        assertEquals("Felhasználó nem található!", exception.getMessage());
    }

    @Test
    void adminLogin_UserNotFound_ThrowsException() {
        String username = "nemletezik";

        when(jdbcTemplate.queryForMap("SELECT id, password FROM admins WHERE username = ?", username))
                .thenThrow(new RuntimeException("Felhasználó nem található!"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                backendManagement.adminLogin(Map.of("username", username, "password", "valamijelszo")));

        assertEquals("Felhasználó nem található!", exception.getMessage());
    }

    @Test
    void getAllRegistrations_ReturnsRegistrationList() {
        when(jdbcTemplate.queryForList(anyString()))
                .thenReturn(List.of(
                        Map.of("registrationId", 1, "studentName", "diak1", "examName", "Matematika"),
                        Map.of("registrationId", 2, "studentName", "diak2", "examName", "Fizika")
                ));

        List<Map<String, Object>> registrations = backendManagement.getAllRegistrations();

        assertEquals(2, registrations.size());
        assertEquals("diak1", registrations.get(0).get("studentName"));
        assertEquals("Matematika", registrations.get(0).get("examName"));
    }

    @Test
    void deleteRegistration_ValidRegistrationId_ReturnsSuccessMessage() {
        int registrationId = 1;

        when(jdbcTemplate.update("DELETE FROM registrations WHERE id = ?", registrationId)).thenReturn(1);

        Map<String, String> response = backendManagement.deleteRegistration(registrationId);

        assertEquals("A vizsgajelentkezés sikeresen törölve.", response.get("message"));
    }

    @Test
    void deleteRegistration_InvalidRegistrationId_ThrowsException() {
        int registrationId = 999;

        when(jdbcTemplate.update("DELETE FROM registrations WHERE id = ?", registrationId)).thenReturn(0);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                backendManagement.deleteRegistration(registrationId));

        assertEquals("Nem található ilyen vizsgajelentkezés.", exception.getMessage());
    }

    @Test
    void register_ValidRequest_ReturnsSuccessMessage() {
        int userId = 1;
        int examId = 2;

        when(jdbcTemplate.update("INSERT INTO registrations (user_id, exam_id) VALUES (?, ?)", userId, examId))
                .thenReturn(1);

        Map<String, String> response = backendManagement.register(Map.of("userId", String.valueOf(userId), "examId", String.valueOf(examId)));

        assertEquals("Sikeres jelentkezés a vizsgára!", response.get("message"));
    }

    @Test
    void unregister_ValidRequest_ReturnsSuccessMessage() {
        int userId = 1;
        int examId = 2;

        when(jdbcTemplate.update("DELETE FROM registrations WHERE user_id = ? AND exam_id = ?", userId, examId)).thenReturn(1);

        Map<String, String> response = backendManagement.unregister(Map.of("userId", String.valueOf(userId), "examId", String.valueOf(examId)));

        assertEquals("Vizsgajelentkezés törölve!", response.get("message"));
    }

    @Test
    void getAvailableExams_ReturnsExamList() {
        int userId = 1;

        when(jdbcTemplate.queryForList(anyString(), eq(userId)))
                .thenReturn(List.of(
                        Map.of("id", 101, "name", "Analízis"),
                        Map.of("id", 102, "name", "Programozás")
                ));

        List<Map<String, Object>> exams = backendManagement.getAvailableExams(userId);

        assertEquals(2, exams.size());
        assertEquals("Analízis", exams.get(0).get("name"));
        assertEquals("Programozás", exams.get(1).get("name"));
    }
}
