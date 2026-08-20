package com.uap.proiv.jobs.service.impl;

import com.uap.proiv.jobs.client.JobApiRepository;
import com.uap.proiv.jobs.dto.Job;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    @Mock
    private JobApiRepository jobApiRepository;

    @InjectMocks
    private JobServiceImpl jobService;

    @Test
    @DisplayName("getAllJobs returns the jobs supplied by the repository")
    void getAllJobsReturnsRepositoryJobs() {
        Job developer = job(1, "Developer");
        List<Job> expectedJobs = List.of(developer);
        when(jobApiRepository.getAllJobs()).thenReturn(expectedJobs);

        List<Job> actualJobs = jobService.getAllJobs();

        assertSame(expectedJobs, actualJobs);
        assertEquals("Developer", actualJobs.getFirst().getName());
        verify(jobApiRepository).getAllJobs();
    }

    @Test
    @DisplayName("getJobById throws when the repository has no jobs")
    void getJobByIdThrowsWhenRepositoryIsEmpty() {
        when(jobApiRepository.getAllJobs()).thenReturn(List.of());

        assertThrows(NoSuchElementException.class, () -> jobService.getJobById(1));

        verify(jobApiRepository).getAllJobs();
    }

    private Job job(int id, String name) {
        Job job = new Job();
        job.setId(id);
        job.setName(name);
        return job;
    }
}
