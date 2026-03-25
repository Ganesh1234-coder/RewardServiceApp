package com.cg.rewardsystem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

	    @InjectMocks
	    private RewardServiceImpl service;

	    private Long customerId;

	    private record Window(LocalDate start, LocalDate end, String label) {}

	    @BeforeEach
	    void setUp() {
	        customerId = 1L;
	    }

	    /** Produces the same rolling 3-month windows as the updated service. */
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

	    /** Picks any date INSIDE the half-open interval [start, end). */
	    private LocalDate inside(Window w) {
	        LocalDate candidate = w.start().plusDays(1);
	        return candidate.isBefore(w.end()) ? candidate : w.start();
	    }

	    @Test
	    void getRewardByCustomerResponse_returnsCorrectWindowsAndTotals() {

	        LocalDate today = LocalDate.now();
	        List<Window> windows = rollingWindows(today);

	        // Create example transactions for each month window:
	        LocalDate w1Date = inside(windows.get(0)); // oldest
	        LocalDate w2Date = inside(windows.get(1));
	        LocalDate w3Date = inside(windows.get(2)); // newest

	        // 49.99 → 0 points
	        when(transcationRepository.findByCustomerIdAndDateBetween(customerId,windows.get(0).start(),windows.get(0).end())).thenReturn(List.of(new Transaction(10L, customerId, 49.99, w1Date)));
	        // 100.99 → 50 points
	        when(transcationRepository.findByCustomerIdAndDateBetween(customerId, windows.get(1).start(), windows.get(1).end())).thenReturn(List.of(new Transaction(11L, customerId, 100.99, w2Date)));
	        // 120.00 → 90 points
	        when(transcationRepository.findByCustomerIdAndDateBetween(customerId,windows.get(2).start(),windows.get(2).end())).thenReturn(List.of(new Transaction(12L, customerId, 120.00, w3Date)));

	        // EXECUTE
	        ResponseEntity<CustomerData> response = service.getRewardByCustomerResponse(customerId);

	        // ASSERT
	        assertThat(response).isNotNull();
	        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

	        CustomerData data = response.getBody();
	        assertThat(data).isNotNull();
	        assertThat(data.getCustomerId()).isEqualTo(customerId);
	        Map<String, Integer> monthPoints = data.getMonth_points();
	        assertThat(monthPoints).isInstanceOf(LinkedHashMap.class);

	        List<String> expectedLabels = windows.stream().map(Window::label).toList();
	      
	        assertThat(monthPoints.keySet()).containsExactlyElementsOf(expectedLabels);

	        // Per-month points
	        assertThat(monthPoints.get(expectedLabels.get(0))).isEqualTo(0);   // 49.99
	        assertThat(monthPoints.get(expectedLabels.get(1))).isEqualTo(50);  // 100.99
	        assertThat(monthPoints.get(expectedLabels.get(2))).isEqualTo(90);  // 120
	        // Total = 0 + 50 + 90 = 140
	        assertThat(data.getTotal()).isEqualTo(140);
	    }

	    @Test
	    void getRewardByCustomerResponse_whenRepositoryThrows_wrapsAsDataNotFound() {

	        LocalDate today = LocalDate.now();
	        List<Window> windows = rollingWindows(today);

	        // Force exception when first period is fetched
	        when(transcationRepository.findByCustomerIdAndDateBetween(customerId,windows.get(0).start(),windows.get(0).end())).thenThrow(new RuntimeException("DB error"));

	        assertThatThrownBy(() -> service.getRewardByCustomerResponse(customerId)).isInstanceOf(DataNotFoundException.class)
	                																 .hasMessageContaining("Data is not found for mentioned customer id.");
	    }
}
