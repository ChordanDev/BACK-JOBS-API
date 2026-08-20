package com.uap.proiv.jobs.service.impl;

import com.uap.proiv.jobs.dto.AssignedResponse;
import com.uap.proiv.jobs.dto.Job;
import com.uap.proiv.jobs.dto.User;
import com.uap.proiv.jobs.dto.UserApiResponse;
import com.uap.proiv.jobs.dto.UserJobAssigned;
import com.uap.proiv.jobs.service.JobService;
import com.uap.proiv.jobs.service.UserService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserJobAssignedServiceImplSpyTest {

    @Mock
    private JobService jobService;

    @Mock
    private UserService userService;

    @Spy
    private AssignedServiceImpl assignedService = new AssignedServiceImpl();

    @InjectMocks
    private UserJobAssignedServiceImpl userJobAssignedService;

    @Test
    @DisplayName("assign returns assignment groups when the assigned service is a spy")
    void assignsUsersUsingAnAssignedServiceSpy() {
        List<Job> jobs = List.of(job(1, "Developer"), job(2, "Designer"));
        UserApiResponse users = singlePage(user(10, "Juan"), user(20, "Diana"));
        List<AssignedResponse> assignments = List.of(
                new AssignedResponse(1, 10),
                new AssignedResponse(2, 20));
        when(jobService.getAllJobs()).thenReturn(jobs);
        when(userService.search(1)).thenReturn(users);
        doReturn(assignments).when(assignedService).create(jobs, List.of(10, 20));

        List<UserJobAssigned> result = userJobAssignedService.assign();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getJob().getId());
        assertEquals(10, result.get(0).getUsers().getFirst().getId());
        assertEquals(2, result.get(1).getJob().getId());
        assertEquals(20, result.get(1).getUsers().getFirst().getId());
        verify(jobService).getAllJobs();
        verify(userService).search(1);
        verify(assignedService).create(jobs, List.of(10, 20));
    }

    private Job job(int id, String name) {
        Job job = new Job();
        job.setId(id);
        job.setName(name);
        return job;
    }

    private User user(int id, String firstName) {
        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        return user;
    }

    private UserApiResponse singlePage(User... users) {
        UserApiResponse response = new UserApiResponse();
        response.setPage(1);
        response.setPerPage(users.length);
        response.setTotal(users.length);
        response.setTotalPages(1);
        response.setData(List.of(users));
        return response;
    }
}
