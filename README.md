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

## Cấu trúc mã nguồn

Chia theo tầng (`src/main/java/com/restaurant/manager`):

| Thư mục | Vai trò |
|---|---|
| `controller/` | REST API: nhận request, gọi service, trả DTO. Không chứa nghiệp vụ |
| `service/` | Nghiệp vụ và phân quyền (`CurrentUser.require`), transaction, kiểm tra dữ liệu |
| `repository/` | Spring Data JPA, mỗi entity một interface; `lockById` khóa dòng khi ghi đồng thời |
| `entity/` | Bảng CSDL; mọi entity kế thừa `AuditableEntity` (created_at, created_by, updated_at, updated_by tự ghi) |
| `dto/request`, `dto/response` | Dữ liệu vào (có Bean Validation, nhóm `OnCreate` cho trường bắt buộc khi tạo) và ra (không lộ `passwordHash`, mã QR) |
| `client/` | Gọi dịch vụ ngoài: `AiServiceClient` gọi AI service (FastAPI) với khóa nội bộ |
| `security/` | JWT, refresh token trên Redis, `CurrentUser` |
| `exception/` | `ApiException`, xử lý lỗi chung theo RFC 7807 |
| `config/` | Security, OpenAPI, JPA Auditing |

Dữ liệu demo (hồ sơ dev): `src/main/resources/db/demo/V2__demo_seed.sql`, sinh bằng `scripts/export-demo-seed.mjs` từ `restaurant-manager-fe/src/mocks/seed.js`. Mật khẩu mọi tài khoản demo: `123456`.
