package com.cg.rewardsystem;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.cg.rewardsystem.entity.Transaction;
import com.cg.rewardsystem.repository.TranscationRepository;


/**
 * End-to-end integration test for GET /rewards/{customerId}.
 * Validates the rolling last-3-month windows (anchored to today), 
 * correct month MonthPeriod labels, points per MonthPeriod, and total points.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RewardServiceIntegrationTest {


		@LocalServerPort
	    private int port;
	    @Autowired
	    private TestRestTemplate rest;
	    @Autowired
	    private TranscationRepository txRepo;

	    private String baseUrl() {
	        return "http://localhost:" + port;
	    }
	    // ---- Helpers ----
	    private record MonthPeriod(LocalDate start, LocalDate end, String label) {}

	    /**
	     * Builds the 3 rolling month windows anchored to "today", oldest -> newest.
	     * Windows are half-open [start, end): start = today.minusMonths(i), end = today.minusMonths(i-1)
	     */
	    private List<MonthPeriod> rollingThreeWindows(LocalDate today) {
	        List<MonthPeriod> out = new ArrayList<>(3);
	        for (int i = 3; i >= 1; i--) {
	            LocalDate start = today.minusMonths(i);
	            LocalDate end   = today.minusMonths(i - 1);
	            String label = String.format("%02d-%02d-%04d to %02d-%02d-%04d",
	                    start.getDayOfMonth(), start.getMonthValue(), start.getYear(),
	                    end.getDayOfMonth(),   end.getMonthValue(),   end.getYear());
	            out.add(new MonthPeriod(start, end, label));
	        }
	        return out;
	    }

	    /**
	     * ForpPicks a safe date inside [start, end) so the transaction definitely belongs to that MonthPeriod.
	     */
	    private LocalDate inside(MonthPeriod w) {
	        LocalDate candidate = w.start().plusDays(1);
	        if (!candidate.isBefore(w.end())) {
	            // Edge fallback (e.g., if end == start+0 days, which shouldn't happen)
	            return w.start();
	        }
	        return candidate;
	    }

	    @BeforeEach
	    void setupData() {
	        txRepo.deleteAll();

	        final LocalDate today = LocalDate.now();
	        final List<MonthPeriod> windows = rollingThreeWindows(today);

	        // windows order: oldest, middle, newest
	        LocalDate w1d = inside(windows.get(0));
	        LocalDate w2d = inside(windows.get(1));
	        LocalDate w3d = inside(windows.get(2));

	        // Customer 1 transactions to exercise tiers:
	        //  - MonthPeriod 1: $49.99 -> truncates to 49 -> 0 points
	        //  - MonthPeriod 2: $100.99 -> truncates to 100 -> 50 points
	        //  - MonthPeriod 3: $120.00 -> 2*(20) + 50 = 90 points
	        txRepo.save(new Transaction(1L, 1L, 49.99,  w1d));
	        txRepo.save(new Transaction(2L, 1L, 100.99, w2d));
	        txRepo.save(new Transaction(3L, 1L, 120.00, w3d));

	        // Customer 2 noise (should not affect customer 1 response)
	        txRepo.save(new Transaction(4L, 2L, 200.00, w2d)); // 250 points for cust 2
	    }

	    @Test
	    void getRewardsResponse_returnsChronologicalWindows_andCorrectPointsAndTotal() {
	        // When
	        ResponseEntity<Map> resp = rest.getForEntity(
	                URI.create(baseUrl() + "/rewards/1"),
	                Map.class);

	        // Then
	        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
	        Map<String, Object> body = resp.getBody();
	        assertThat(body).isNotNull();
	        // Basic shape
	        assertThat(body.get("customerId")).isEqualTo(1);
	        Object mp = body.get("month_points");
	        assertThat(mp).isInstanceOf(Map.class);
	        @SuppressWarnings("unchecked")
	        LinkedHashMap<String, Integer> monthPoints = (LinkedHashMap<String, Integer>) mp;
	        // Expected labels oldest -> newest
	        final LocalDate today = LocalDate.now();
	        final List<MonthPeriod> windows = rollingThreeWindows(today);
	        final List<String> expectedLabels = windows.stream().map(MonthPeriod::label).toList();
	        // Keys are in chronological order
	        assertThat(monthPoints.keySet()).containsExactlyElementsOf(expectedLabels);
	        // Points per MonthPeriod (from setup)
	        assertThat(monthPoints.get(expectedLabels.get(0))).isEqualTo(0);   // oldest
	        assertThat(monthPoints.get(expectedLabels.get(1))).isEqualTo(50);  // middle
	        assertThat(monthPoints.get(expectedLabels.get(2))).isEqualTo(90);  // newest
	        // Total
	        assertThat(body.get("total")).isEqualTo(140);
	    }

	    @Test
	    void getRewardsResponse_unknownCustomer_returnsEmptyOr404() {
	        ResponseEntity<String> resp = rest.getForEntity(
	                URI.create(baseUrl() + "/rewards/999999"),
	                String.class);
	        assertThat(resp.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
	    }

}

