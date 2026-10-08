# Test

โฟลเดอร์นี้เก็บ Test Report และ Postman collection ส่วน source ของ test อยู่ใน `code/backend/src/test/java` ตามโครงสร้าง Maven

## Unit Test และ Integration Test

```bash
cd code/backend
./mvnw verify
```

- Surefire report: `code/backend/target/surefire-reports/`
- JaCoCo coverage: `code/backend/target/site/jacoco/index.html`
- บน GitHub Actions ดาวน์โหลดได้จาก artifact `surefire-report` และ `jacoco-report` ของแต่ละ workflow run

ก่อนส่งงาน ให้ดาวน์โหลด report จาก CI รอบล่าสุดบน `develop` มาใส่ใน `test/reports/`

| ประเภท | เครื่องมือ | ตัวอย่าง |
| --- | --- | --- |
| Service | JUnit 5 + Mockito | `BookingServiceImplTest`, `BookingLifecycleServiceImplTest` |
| Controller | `@WebMvcTest` + MockMvc | `BookingControllerTest`, `RoomControllerTest` |
| Repository | `@DataJpaTest` + H2 (โหมด PostgreSQL) + Flyway | `BookingRepositoryTest`, `RoomRepositoryTest` |
| Integration | `@SpringBootTest` | `NotificationFlowIntegrationTest` |

## Postman

import `postman/sc09-room-booking.postman_collection.json` แล้วรัน request "Login (staff)" หรือ "Login (student)" ก่อน token จะถูกเก็บในตัวแปร `token` ให้ request อื่นใช้อัตโนมัติ
