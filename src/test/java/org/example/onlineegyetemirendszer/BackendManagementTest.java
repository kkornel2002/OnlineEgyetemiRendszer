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
    void getAllEnrollments_ReturnsEnrollmentList() {
        when(jdbcTemplate.queryForList(anyString()))
                .thenReturn(List.of(
                        Map.of("enrollmentId", 1, "studentName", "diak1", "courseName", "Matematika I."),
                        Map.of("enrollmentId", 2, "studentName", "diak2", "courseName", "Fizika II.")
                ));

        List<Map<String, Object>> enrollments = backendManagement.getAllEnrollments();

        assertEquals(2, enrollments.size());
        assertEquals("diak1", enrollments.get(0).get("studentName"));
        assertEquals("Matematika I.", enrollments.get(0).get("courseName"));
    }

    @Test
    void deleteEnrollment_ValidEnrollmentId_ReturnsSuccessMessage() {
        int enrollmentId = 1;

        when(jdbcTemplate.update("DELETE FROM enrollments WHERE id = ?", enrollmentId)).thenReturn(1);

        Map<String, String> response = backendManagement.deleteEnrollment(enrollmentId);

        assertEquals("A beiratkozás sikeresen törölve.", response.get("message"));
    }

    @Test
    void deleteEnrollment_InvalidEnrollmentId_ThrowsException() {
        int enrollmentId = 999;

        when(jdbcTemplate.update("DELETE FROM enrollments WHERE id = ?", enrollmentId)).thenReturn(0);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                backendManagement.deleteEnrollment(enrollmentId));

        assertEquals("Nem található ilyen beiratkozás.", exception.getMessage());
    }

    @Test
    void enroll_ValidRequest_ReturnsSuccessMessage() {
        int userId = 1;
        int courseId = 2;


        when(jdbcTemplate.update("INSERT INTO enrollments (user_id, course_id) VALUES (?, ?)", userId, courseId))
                .thenReturn(1);


        Map<String, String> response = backendManagement.enroll(Map.of("userId", String.valueOf(userId), "courseId", String.valueOf(courseId)));


        assertEquals("Sikeres jelentkezés!", response.get("message"));
    }


    @Test
    void unenroll_ValidRequest_ReturnsSuccessMessage() {
        int userId = 1;
        int courseId = 2;

        when(jdbcTemplate.update("DELETE FROM enrollments WHERE user_id = ? AND course_id = ?", userId, courseId)).thenReturn(1);

        Map<String, String> response = backendManagement.unenroll(Map.of("userId", String.valueOf(userId), "courseId", String.valueOf(courseId)));

        assertEquals("Kurzus törölve a felvett kurzusok közül!", response.get("message"));
    }

    @Test
    void getAvailableCourses_ReturnsCourseList() {
        int userId = 1;

        when(jdbcTemplate.queryForList(anyString(), eq(userId)))
                .thenReturn(List.of(
                        Map.of("id", 101, "name", "Analízis I."),
                        Map.of("id", 102, "name", "Programozás Alapjai")
                ));

        List<Map<String, Object>> courses = backendManagement.getAvailableCourses(userId);

        assertEquals(2, courses.size());
        assertEquals("Analízis I.", courses.get(0).get("name"));
        assertEquals("Programozás Alapjai", courses.get(1).get("name"));
    }
}
