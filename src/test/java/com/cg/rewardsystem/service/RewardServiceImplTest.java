package com.cg.rewardsystem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.cg.rewardsystem.entity.Transaction;
import com.cg.rewardsystem.exception.DataNotFoundException;
import com.cg.rewardsystem.model.CustomerData;
import com.cg.rewardsystem.repository.TranscationRepository;

/**
 * Unit test for {@link RewardServiceImpl#getRewardByCustomerResponse(Long)}.
 * <p>
 * This test stubs the repository to return deterministic transactions for the
 * three rolling month windows [start, end) computed by the service from "today".
 */
@ExtendWith(MockitoExtension.class)
class RewardServiceImplTest {

    @Mock
    private TranscationRepository transcationRepository;
    // If your service implements an interface IRewardService,
    // the concrete class is likely in serviceImpl package.
    @InjectMocks
    private RewardServiceImpl service;
    private Long customerId;
    
    // Helper record for readability
    private record Window(LocalDate start, LocalDate end, String label) {}
    
    @BeforeEach
    void setUp() {
        customerId = 1L;
    }

    /** Builds the three rolling windows oldest -> newest anchored to "today" (half-open [start, end)). */
    private List<Window> rollingWindows(LocalDate today) {
        List<Window> out = new ArrayList<>(3);
        for (int i = 3; i >= 1; i--) {
            LocalDate start = today.minusMonths(i);
            LocalDate end   = today.minusMonths(i - 1);
            String label = String.format("%02d-%02d-%04d to %02d-%02d-%04d",
                    start.getDayOfMonth(), start.getMonthValue(), start.getYear(),
                    end.getDayOfMonth(),   end.getMonthValue(),   end.getYear());
            out.add(new Window(start, end, label));
        }
        return out;
    }

    /** Pick a safe date strictly inside the half-open interval [start, end). */
    private LocalDate inside(Window w) {
        LocalDate candidate = w.start().plusDays(1);
        return candidate.isBefore(w.end()) ? candidate : w.start();
    }

    @Test
    void getRewardByCustomerResponse_returnsChronologicalWindows_andCorrectPointsAndTotal() {
        // Given: today (anchor). We’ll mirror the service’s window computation.
        LocalDate today = LocalDate.now();
        List<Window> windows = rollingWindows(today);

        // Arrange stubbed transactions for each window:
        //  - Oldest: amount 49.99 -> truncates to 49 -> 0 points
        //  - Middle: amount 100.99 -> truncates to 100 -> 50 points
        //  - Newest: amount 120.00 -> 2*(20) + 50 = 90 points
        LocalDate w1Date = inside(windows.get(0));
        LocalDate w2Date = inside(windows.get(1));
        LocalDate w3Date = inside(windows.get(2));

        // Stub repository method: findByCustomerIdAndDateHalfOpen(customerId, start, end)
        when(transcationRepository.findByCustomerIdAndDateBetween(
                argThat(id -> id.equals(customerId)),
                argThat(d -> d.equals(windows.get(0).start())),
                argThat(d -> d.equals(windows.get(0).end()))
        )).thenReturn(List.of(new Transaction(10L,customerId,49.99,w1Date )));

        when(transcationRepository.findByCustomerIdAndDateBetween(
                 customerId,
                windows.get(1).start(),
                windows.get(1).end())
        		).thenReturn(List.of(new Transaction(
                11L, customerId, 100.99, w2Date
        )));

        when(transcationRepository.findByCustomerIdAndDateBetween(
                 customerId,
                windows.get(2).start(),
                windows.get(2).end()
        )).thenReturn(List.of(new Transaction(
                12L, customerId, 120.00, w3Date
        )));

        // When
        ResponseEntity<CustomerData> response = service.getRewardByCustomerResponse(customerId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        CustomerData body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCustomerId()).isEqualTo(customerId);

        // Verify month_points ordering (LinkedHashMap preserves insertion order, and we added oldest -> newest)
        Map<String, Integer> monthPoints = body.getMonth_points();
        assertThat(monthPoints).isInstanceOf(LinkedHashMap.class);
        List<String> expectedLabels = windows.stream().map(Window::label).toList();
        assertThat(monthPoints.keySet()).containsExactlyElementsOf(expectedLabels);
        // Verify per-window points
        assertThat(monthPoints.get(expectedLabels.get(0))).isEqualTo(0);   // 49.99 -> 0
        assertThat(monthPoints.get(expectedLabels.get(1))).isEqualTo(50);  // 100.99 -> 50
        assertThat(monthPoints.get(expectedLabels.get(2))).isEqualTo(90);  // 120 -> 90
        // Verify total
        assertThat(body.getTotal()).isEqualTo(140);
    }

    @Test
    void getRewardByCustomerResponse_whenRepositoryThrows_wrapsIntoDataNotFound() {
        // Given: make repo throw once to test the catch-block path
        LocalDate today = LocalDate.now();
        List<Window> windows = rollingWindows(today);

        when(transcationRepository.findByCustomerIdAndDateBetween(
                org.mockito.ArgumentMatchers.eq(customerId),
                org.mockito.ArgumentMatchers.eq(windows.get(0).start()),
                org.mockito.ArgumentMatchers.eq(windows.get(0).end())
        )).thenThrow(new RuntimeException("DB down"));
        // When / Then
        try {
            service.getRewardByCustomerResponse(customerId);
        } catch (DataNotFoundException ex) {
            assertThat(ex.getMessage()).contains("Data is not found for mentioned customer id.");
            return;
        }
        throw new AssertionError("Expected DataNotFoundExcepition to be thrown");
    }
}
