
# RewardApp(Spring Boot)

A Spring Boot service that calculates **monthly and total reward points** for customers based on their transaction history.

## 📌 Problem Statement

- **2 points** for every whole dollar spent **over $100** in each transaction
- **1 point** for every whole dollar spent **between $50 and $100** in each transaction
- Cents are **truncated** to whole dollars _before_ applying the rules.

> Example: `$120.00` → `2 × 20 + 1 × 50 = 90` points

The API returns a **3-month rolling window** (anchored to today) with **chronological** month ranges and a **total**.

---

## 🧮 Reward Calculation

Implemented in `CalculateRewardsPoints.calculatePoints(Double amount)`:

- Truncates to whole dollars: `int dollars = (int) Math.floor(amount)`
- `dollars <= 50` → `0`
- `51..100` → `dollars - 50`
- `> 100` → `(dollars - 100) * 2 + 50`

Robustness:
- Validates `null`, negative, `NaN` and `Infinity` inputs.

---

## 📅 Rolling 3-Month Windows

Service computes **three consecutive month-sized windows** using a half‑open interval **[start, end)** anchored to **today**:

- Window 1: `[today-3 months, today-2 months)` (oldest)
- Window 2: `[today-2 months, today-1 month)`
- Window 3: `[today-1 month, today)` (newest)

Labels are rendered as: `dd-MM-yyyy to dd-MM-yyyy` and returned **oldest → newest** using a `LinkedHashMap` to preserve order.

---

## 🧭 API

### `GET /rewards/{customerId}`

**Response**
```json
{
  "customerId": 1,
  "month_points": {
    "07-12-2025 to 07-01-2026": 0,
    "07-01-2026 to 07-02-2026": 50,
    "07-02-2026 to 07-03-2026": 90
  },
  "total": 140
}
```

**Notes**
- `month_points` keys are **chronological**.
- Each key covers a **half‑open** window `[start, end)` to avoid double-counting boundary dates.

---

## 🏗️ Project Structure (example)

```
RewardServiceApp/
  ├── src/main/java/com/cg/rewardsystem/
  │   ├── model/CustomerData.java
  │   ├── controller/RewardController.java
  │   ├── entity/Transaction.java
  │   ├── exception/{DataNotFoundExcepition, GlobalExceptionHandler}.java
  │   ├── service/{IRewardService, RewardServiceImpl}.java
  │   ├── repository/TranscationRepository.java
  │   ├── serviceImpl/.java
  │   ├── util/CalculateRewardsPoints.java
  │   └── RewardServiceAppApplication.java
  ├── src/main/resources/
  │   ├── application.properties
  │   ├── data.sql
  │   └── schema.sql
  ├── src/test/java/com/cg/rewardsystem/
  │   ├── RewardAppApplicationTests.java
  │   └── service/RewardServiceImplTest.java
  ├── src/test/resources/application-test.properties
  ├── pom.xml
  
```

---

## ⚙️ Configuration

Example `application.properties` (adapt as needed):

```properties
server.port=8080

spring.datasource.url=jdbc:h2:mem:rewardsdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Use exact field names (e.g., CUSTOMERID, TRANSACTIONDATE) without snake_case conversion
spring.jpa.properties.hibernate.physical_naming_strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

**H2 Console**
- URL: `http://localhost:8080/h2-console`
- JDBC: `jdbc:h2:mem:rewardsdb`
- User: `sa` / Password: _(blank unless set)_

> If you changed the server port or context path, adjust the console URL accordingly.

---

## ▶️ Run & Test

**Build & Run**
```bash
mvn clean package
mvn spring-boot:run
```

**Unit Tests** (calculator & service)
```bash
mvn -Dtest=*CalculateRewardsPointsTest,*RewardServiceTest test
```

**Integration Tests** (end-to-end HTTP with TestRestTemplate)
```bash
mvn -Dtest=*RewardControllerIT test
```

---

## ✅ Testing Strategy

- **Unit tests** for `CalculateRewardsPoints` covering:
  - Boundaries: `50`, `100`, `101`
  - Truncation: `100.99 → 100`
  - Invalid inputs: `null`, negative, `NaN`, `Infinity`
- **Service unit tests** using Mockito:
  - Stubs repository for the three rolling windows
  - Verifies labels order and totals
- **Integration tests** using `@SpringBootTest(RANDOM_PORT)` + `TestRestTemplate`:
  - Seeds H2 with transactions in each window
  - Asserts JSON response shape, ordering, and totals

---

## 🙅 Common Pitfalls

- **H2 console 404**: ensure `spring.h2.console.enabled=true` and check context/servlet path.
- **Column not found**: align entity `@Column(name=...)` with actual DB columns or set the physical naming strategy (above).
- **TestRestTemplate bean errors**: ensure you have `spring-boot-starter-test` and no custom `@Bean TestRestTemplate`.
- **WebFlux on classpath**: H2 console is Servlet-based; prefer `spring-boot-starter-web` for MVC tests.

---

## 🛣️ Roadmap

- Custom date-range queries (start/end as params)
- Rewards report for all customers
- Caching for heavy datasets
- Swagger/OpenAPI annotations

---

## 📄 License

MIT (or company-internal). Use as a reference implementation.
