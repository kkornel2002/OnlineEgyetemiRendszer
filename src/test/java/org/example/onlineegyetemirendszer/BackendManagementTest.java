package org.example.onlineegyetemirendszer;

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

    BackendManagementTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void adminLogin_ValidCredentials_ReturnsSuccess() {

        String username = "ugyintezo1";
        String password = "ugyintezojelszo1";
        when(jdbcTemplate.queryForMap(anyString(), eq(username)))
                .thenReturn(Map.of("id", 1, "password", password));


        Map<String, String> response = backendManagement.adminLogin(Map.of("username", username, "password", password));


        assertEquals("Sikeres bejelentkezés!", response.get("message"));
        assertEquals("1", response.get("adminId"));
    }

    @Test
    void adminLogin_InvalidPassword_ThrowsException() {

        String username = "ugyintezo1";
        when(jdbcTemplate.queryForMap(anyString(), eq(username)))
                .thenReturn(Map.of("id", 1, "password", "wrongpassword"));


        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                backendManagement.adminLogin(Map.of("username", username, "password", "ugyintezojelszo1")));
        assertEquals("Hibás jelszó!", exception.getMessage());
    }

    @Test
    void getAllEnrollments_ReturnsEnrollments() {

        when(jdbcTemplate.queryForList(anyString())).thenReturn(
                List.of(
                        Map.of("enrollmentId", 1, "studentName", "diak1", "courseName", "Matematika I."),
                        Map.of("enrollmentId", 2, "studentName", "diak2", "courseName", "Fizika II.")
                )
        );


        var enrollments = backendManagement.getAllEnrollments();


        assertEquals(2, enrollments.size());
        assertEquals("diak1", enrollments.get(0).get("studentName"));
    }
}
