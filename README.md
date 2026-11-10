# restaurant-manager-be

Backend cho hệ thống quản lý nhà hàng tích hợp trợ lý hội thoại AI.
Spring Boot 4.1 · Java 21 · PostgreSQL 17 · Redis 7.4 · Flyway · Swagger UI.

## Chạy

Cần JDK 21 và Docker đang chạy.

```bash
./mvnw spring-boot:run      # tự bật Postgres (cổng 5433) và Redis (cổng 6380) từ compose.yaml
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

## Test

```bash
./mvnw test                 # cần Docker cho Testcontainers
```

## Kế hoạch

Xem [docs/plans/2026-11-10-be-plan.md](docs/plans/2026-11-10-be-plan.md).
