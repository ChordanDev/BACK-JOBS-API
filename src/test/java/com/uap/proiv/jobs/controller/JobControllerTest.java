package com.uap.proiv.jobs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uap.proiv.jobs.dto.AssignRequest;
import com.uap.proiv.jobs.dto.Job;
import com.uap.proiv.jobs.dto.User;
import com.uap.proiv.jobs.dto.UserApiResponse;
import com.uap.proiv.jobs.dto.UserJobAssigned;
import com.uap.proiv.jobs.service.JobService;
import com.uap.proiv.jobs.service.UserJobAssignedService;
import com.uap.proiv.jobs.service.UserService;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class JobControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserJobAssignedService userJobAssignedService;

    @Mock
    private JobService jobService;

    @InjectMocks
    private JobController jobController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UserApiResponse userResponse;
    private ArrayList<UserJobAssigned> assignments;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(jobController).build();
        objectMapper = new ObjectMapper();

        User user = new User();
        user.setId(10);
        user.setFirstName("Juan");

        ArrayList<User> users = new ArrayList<>();
        users.add(user);

        userResponse = new UserApiResponse();
        userResponse.setPage(1);
        userResponse.setPerPage(1);
        userResponse.setTotal(1);
        userResponse.setTotalPages(1);
        userResponse.setData(users);

        Job job = new Job();
        job.setId(7);

        assignments = new ArrayList<>();
        assignments.add(new UserJobAssigned(users, job));
    }

    @Test
    @DisplayName("GET /api/job/users/1 responde 200, 5xx y 200 en llamadas secuenciales")
    void getAllUsersPageOneSequentialResponses() throws Exception {
        when(userService.search(1)).thenReturn(userResponse).thenThrow(new RuntimeException("boom")).thenReturn(userResponse);

        mockMvc.perform(get("/api/job/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(get("/api/job/users/1"))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$").value("boom"));

        mockMvc.perform(get("/api/job/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_pages").value(1));

        verify(userService, times(3)).search(1);
    }

    @Test
    @DisplayName("GET /api/job/users/2 devuelve 5xx y el mensaje de error")
    void getAllUsersPageTwoErrorResponse() throws Exception {
        when(userService.search(2)).thenThrow(new RuntimeException("fail"));

        mockMvc.perform(get("/api/job/users/2"))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$").value("fail"));

        verify(userService).search(2);
    }

    @Test
    @DisplayName("POST /api/job/assign devuelve la asignación")
    void assignReturnsControllerContract() throws Exception {
        when(userJobAssignedService.assign()).thenReturn(assignments);

        AssignRequest request = new AssignRequest();
        request.setRequestNumber(123);
        request.setClientName("ACME");

        mockMvc.perform(post("/api/job/assign")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Client").value("ACME"))
                .andExpect(jsonPath("$.Request_Number").value(123))
                .andExpect(jsonPath("$.Assign.length()").value(1))
                .andExpect(jsonPath("$.Assign[0].job.id").value(7))
                .andExpect(jsonPath("$.Assign[0].users[0].id").value(10));

        verify(userJobAssignedService).assign();
    }
}
