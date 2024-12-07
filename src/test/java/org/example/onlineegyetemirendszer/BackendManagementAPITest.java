package org.example.onlineegyetemirendszer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


class BackendManagementAPITest {

    private MockMvc mockMvc;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private BackendManagement backendManagement;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(backendManagement).build();
    }

    @Test
    void adminLogin_ValidCredentials_ReturnsSuccess() throws Exception {
        
        String username = "ugyintezo1";
        String password = "ugyintezojelszo1";
        when(jdbcTemplate.queryForMap(anyString(), eq(username)))
                .thenReturn(Map.of("id", 1, "password", password));

        
        mockMvc.perform(post("/api/admin/login")
                        .contentType("application/json")
                        .content("{\"username\":\"ugyintezo1\",\"password\":\"ugyintezojelszo1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sikeres bejelentkezés!"))
                .andExpect(jsonPath("$.adminId").value("1"));
    }

    @Test
    <T>
    void getAllEnrollments_ReturnsEnrollments() throws Exception {

        OngoingStubbing<List<Map<String, Object>>> tOngoingStubbing = when(jdbcTemplate.queryForList(anyString())).thenReturn(
                List.of(
                        Map.of("enrollmentId", 1, "studentName", "diak1", "courseName", "Matematika I."),
                        Map.of("enrollmentId", 2, "studentName", "diak2", "courseName", "Fizika II.")
                )
        );


        mockMvc.perform(get("/api/admin/enrollments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentName").value("diak1"))
                .andExpect(jsonPath("$[1].courseName").value("Fizika II."));
    }
}
