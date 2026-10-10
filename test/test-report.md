# Test Report

รายงานผลการทดสอบระบบจองห้อง SC09 ครอบคลุม Unit Test, Controller Test, Repository Test, Integration Test (รวม test การทำงานพร้อมกัน) และการทดสอบบนเว็บที่ deploy จริง

| หัวข้อ | ค่า |
|---|---|
| Commit ที่ทดสอบ | `develop` @ `32a7f47` (Merge PR #36) |
| วันที่ทดสอบ | 10 ตุลาคม 2569 |
| คำสั่ง | `cd code/backend && ./mvnw verify` |
| สภาพแวดล้อม | JDK 17, Spring Boot 4.1.1, H2 (โหมด PostgreSQL) + Flyway V1–V5 |
| ผลรวม | **226 tests ผ่านทั้งหมด** (Failures 0, Errors 0, Skipped 0) จาก 36 test class |
| Coverage (JaCoCo) | **Line 72.9%**, Branch 63.4%, Instruction 73.9%, Method 69.3% |
| CI | GitHub Actions `ci.yml` รัน test ชุดเดียวกันทุก Pull Request และทุก push เข้า `develop`/`main` |

## 1. ประเภทของ Test

| ประเภท | เครื่องมือ | จำนวน class | จำนวน test | ทดสอบอะไร |
|---|---|---:|---:|---|
| Unit Test | JUnit 5 + Mockito (`@ExtendWith(MockitoExtension)`) | 21 | 160 | business logic ของ service, handler ใน chain, policy, state, listener, exception handler แยกจากฐานข้อมูล |
| Controller Test | `@WebMvcTest` + MockMvc | 6 | 30 | path, HTTP status, validation ของ request, รูปแบบ JSON ของ response |
| Repository Test | `@DataJpaTest` + H2 + Flyway | 5 | 28 | query จริงกับ schema จริงจาก migration: เวลาชน ค้นหา กรอง Many-to-Many ล็อกแถว |
| Integration Test | `@SpringBootTest` | 4 | 8 | ทั้งระบบทำงานร่วมกัน: แจ้งเตือนหลัง commit, จองพร้อมกัน, อนุมัติพร้อมกัน |
| **รวม** | | **36** | **226** | |

## 2. ผลรายละเอียดตาม Test Class

| Test Class | ประเภท | Tests | ผล | โมดูล / ผู้รับผิดชอบ |
|---|---|---:|:-:|---|
| `controller.api.AuthControllerTest` | Controller | 7 | ✅ | ผู้ใช้ (คนที่ 1) |
| `service.impl.AuthServiceImplTest` | Unit | 6 | ✅ | ผู้ใช้ (คนที่ 1) |
| `service.impl.UserServiceImplTest` | Unit | 4 | ✅ | ผู้ใช้ (คนที่ 1) |
| `repository.UserRepositoryTest` | Repository | 4 | ✅ | ผู้ใช้ (คนที่ 1) |
| `exception.GlobalExceptionHandlerTest` | Unit | 6 | ✅ | ฐานราก (คนที่ 1) |
| `service.policy.BookingPolicyTest` | Unit | 3 | ✅ | Strategy (คนที่ 1) |
| `service.policy.BookingPolicyResolverTest` | Unit | 5 | ✅ | Strategy (คนที่ 1) |
| `controller.api.RoomControllerTest` | Controller | 6 | ✅ | ห้อง (คนที่ 2) |
| `service.impl.RoomServiceImplTest` | Unit | 10 | ✅ | ห้อง (คนที่ 2) |
| `service.impl.RoomClosureServiceImplTest` | Unit | 6 | ✅ | ห้อง (คนที่ 2) |
| `service.impl.RoomAvailabilityTest` | Repository | 3 | ✅ | ห้อง (คนที่ 2) |
| `repository.RoomRepositoryTest` | Repository | 7 | ✅ | ห้อง (คนที่ 2) |
| `repository.RoomClosureRepositoryTest` | Repository | 4 | ✅ | ห้อง (คนที่ 2) |
| `controller.api.BookingControllerTest` | Controller | 5 | ✅ | การจอง (คนที่ 3) |
| `controller.api.ScheduleControllerTest` | Controller | 4 | ✅ | การจอง (คนที่ 3) |
| `service.impl.BookingServiceImplTest` | Unit | 9 | ✅ | การจอง (คนที่ 3) |
| `service.impl.ScheduleServiceImplTest` | Unit | 3 | ✅ | การจอง (คนที่ 3) |
| `service.impl.BookingConcurrencyIntegrationTest` | Integration | 3 | ✅ | การจอง (คนที่ 3) |
| `repository.BookingRepositoryTest` | Repository | 10 | ✅ | การจอง (คนที่ 3) |
| `service.validation.TimeRangeHandlerTest` | Unit | 5 | ✅ | Chain (คนที่ 3) |
| `service.validation.PolicyHandlerTest` | Unit | 6 | ✅ | Chain (คนที่ 3) |
| `service.validation.RoomHandlerTest` | Unit | 3 | ✅ | Chain (คนที่ 3) |
| `service.validation.ClosureHandlerTest` | Unit | 2 | ✅ | Chain (คนที่ 3) |
| `service.validation.ConflictHandlerTest` | Unit | 3 | ✅ | Chain (คนที่ 3) |
| `controller.api.BookingLifecycleControllerTest` | Controller | 4 | ✅ | วงจรสถานะ (คนที่ 4) |
| `service.impl.BookingLifecycleServiceImplTest` | Unit | 26 | ✅ | วงจรสถานะ (คนที่ 4) |
| `service.impl.BookingStatusConcurrencyIntegrationTest` | Integration | 3 | ✅ | วงจรสถานะ (คนที่ 4) |
| `service.state.BookingStateTransitionTest` | Unit | 42 | ✅ | State (คนที่ 4) |
| `service.state.BookingStateFactoryTest` | Unit | 2 | ✅ | State (คนที่ 4) |
| `service.scheduler.BookingStatusSchedulerTest` | Unit | 1 | ✅ | Scheduler (คนที่ 4) |
| `controller.api.NotificationControllerTest` | Controller | 4 | ✅ | แจ้งเตือน (คนที่ 5) |
| `service.impl.NotificationServiceImplTest` | Unit | 6 | ✅ | แจ้งเตือน (คนที่ 5) |
| `event.NotificationListenerTest` | Unit | 8 | ✅ | Observer (คนที่ 5) |
| `event.NotificationFlowIntegrationTest` | Integration | 1 | ✅ | Observer (คนที่ 5) |
| `service.impl.StatsServiceImplTest` | Unit | 4 | ✅ | สถิติ (คนที่ 5) |
| `CpRoomBookingApplicationTests` | Integration | 1 | ✅ | ทั้งระบบ (context โหลดได้) |

## 3. Coverage ตาม Package (JaCoCo)

| Package | Line | Branch | หมายเหตุ |
|---|---:|---:|---|
| `service.validation` | 100% | 96.7% | Chain of Responsibility ครบทุกกฎ |
| `service.state` | 100% | 100% | ทุกคู่ สถานะ × action |
| `service.policy` | 100% | - | Strategy ทุกบทบาท |
| `service.scheduler` | 100% | 25% | |
| `service.notification` | 100% | 50% | |
| `event` | 100% | 83.3% | Observer |
| `dto.validation` | 100% | 75% | `@MaxUtf8Bytes` |
| `domain.entity`, `domain.enums` | 100% | 87.5–100% | |
| `service.impl` | 73.3% | 59.7% | business logic หลัก |
| `mapper` | 75.0% | 50.0% | |
| `exception` | 67.8% | - | |
| `controller.api` | 21.4% | - | มี Controller Test 6 จาก 11 controller (ส่วนที่เหลือทดสอบผ่าน service test และการทดสอบบนเว็บ) |
| `security` | 20.3% | - | Controller Test ปิด security filter (`addFilters = false`) ส่วน JWT ทดสอบบนเว็บจริงและ Postman |
| **รวม** | **72.9%** | **63.4%** | |

รายงาน HTML ฉบับเต็มสร้างที่ `code/backend/target/site/jacoco/index.html` หลังรัน `./mvnw verify` และดาวน์โหลดจาก GitHub Actions ได้ที่ artifact `jacoco-report` และ `surefire-report` ของแต่ละ workflow run

## 4. Requirement Traceability (กฎสำคัญ → Test)

| กฎ / ความต้องการ | Test ที่ยืนยัน |
|---|---|
| ห้องเดียวกันจองเวลาซ้อนไม่ได้ (ต่อกันพอดีได้) | `BookingRepositoryTest` (ทับเต็ม ทับบางส่วน อยู่ข้างใน ต่อกันพอดี ยกเลิกแล้วไม่นับ), `ConflictHandlerTest` |
| จองพร้อมกันได้สำเร็จเพียงรายการเดียว | `BookingConcurrencyIntegrationTest` (5 ผู้ใช้ส่งพร้อมกัน × 3 รอบ) |
| กฎตามบทบาท (ชั่วโมง ล่วงหน้า โควตา อนุมัติ) | `BookingPolicyTest`, `BookingPolicyResolverTest`, `PolicyHandlerTest` |
| เวลาเปิดอาคาร 08:00–22:00, วันเดียว, ไม่ใช่อดีต | `TimeRangeHandlerTest` |
| ห้อง ACTIVE และความจุพอ | `RoomHandlerTest` |
| ห้ามจองทับช่วงปิดห้อง | `ClosureHandlerTest`, `RoomAvailabilityTest` |
| ห้ามสร้างช่วงปิดทับการจองที่ยังใช้งาน | `RoomClosureServiceImplTest` |
| เปลี่ยนสถานะได้เฉพาะตามตาราง State | `BookingStateTransitionTest` (42 กรณี), `BookingLifecycleServiceImplTest` |
| อนุมัติ/ปฏิเสธพร้อมกันได้เพียงรายการเดียว | `BookingStatusConcurrencyIntegrationTest` |
| อนุมัติ / check-in ไม่ได้ถ้าห้องปิด | `BookingLifecycleServiceImplTest` (approve/checkIn เมื่อห้องปิดหรือไม่ ACTIVE) |
| Scheduler ไม่ทับรายการที่เพิ่ง check-in | `BookingLifecycleServiceImplTest.markNoShows_skipsBookingCheckedInAfterSchedulerRead` |
| แจ้งเตือนคนที่ถูกต้องหลัง commit | `NotificationListenerTest`, `NotificationFlowIntegrationTest` |
| ค้นหาห้อง: กรอง เรียง แบ่งหน้า Many-to-Many | `RoomRepositoryTest`, `RoomServiceImplTest`, `RoomControllerTest` |
| รหัสผ่านเกิน 72 ไบต์ตอบ 400 ไม่ใช่ 500 | `AuthControllerTest` |
| Error response รูปแบบเดียวกันทุก status | `GlobalExceptionHandlerTest` |
| ตารางการใช้ห้องรายเดือน/รายวัน | `ScheduleServiceImplTest`, `ScheduleControllerTest`, `BookingRepositoryTest.findAllInRange_...` |

## 5. Test การทำงานพร้อมกัน (Concurrency)

พบจากการตรวจ (audit) ว่าคำขอที่มาพร้อมกันผ่านการตรวจกฎได้ทั้งคู่ จึงแก้ด้วยการล็อกแถว (`SELECT ... FOR UPDATE`) และเขียน test ที่ปล่อยหลาย thread พร้อมกันด้วย `CountDownLatch` บนฐานข้อมูลจริง (คนละ transaction)

| Test | สถานการณ์ | ก่อนแก้ (ถอดล็อกออก) | หลังแก้ |
|---|---|---|---|
| `BookingConcurrencyIntegrationTest` | 5 ผู้ใช้จองห้อง SC09-9231 เวลาเดียวกันพร้อมกัน | ❌ สำเร็จทั้ง 5 รายการ (`Expected size: 1 but was: 5`) | ✅ สำเร็จ 1, ได้ 409 อีก 4 |
| `BookingStatusConcurrencyIntegrationTest` | เจ้าหน้าที่อนุมัติ + ผู้ดูแลระบบปฏิเสธรายการเดียวกันพร้อมกัน | ❌ สำเร็จทั้งคู่ (`Expected size: 1 but was: 2`) | ✅ สำเร็จ 1, ได้ 409 อีก 1, ประวัติจาก PENDING มีครั้งเดียว |

แต่ละ test รันซ้ำ 3 รอบ (`@RepeatedTest(3)`) ผ่านทุกรอบ

## 6. การทดสอบบนเว็บที่ deploy (System / Acceptance Test)

ทดสอบบน `https://sc09-room-booking.vercel.app` + `https://sc09-room-booking.onrender.com` (PostgreSQL 17 บน Neon)

### 6.1 ทดสอบการใช้งานตามบทบาท (10 ต.ค. 2569, ก่อนแก้บั๊ก)

| กรณี | ผลที่คาด | ผล |
|---|---|:-:|
| login ทั้ง 5 บทบาท / logout | เข้าได้ แสดงชื่อและบทบาทถูก | ✅ |
| ไม่ login แล้วเปิด `/bookings` | พาไป `/login?next=/bookings` | ✅ |
| นักศึกษาเปิด `/admin/users`, อาจารย์เปิด `/admin/approvals` | ไม่มีสิทธิ์เข้าถึง | ✅ |
| นักศึกษาคนที่ 2 เปิดการจองของอีกคน | ดูได้เฉพาะการจองของตัวเอง | ✅ |
| นักศึกษาจองเกิน 7 วัน / เกิน 2 ชม. / นอกเวลาอาคาร / เกินโควตา 2 รายการ | ปฏิเสธพร้อมข้อความตามกฎ | ✅ |
| อาจารย์จองเกิน 4 ชม. / เกิน 30 วัน / จองถูกกฎ | ปฏิเสธ / ปฏิเสธ / อนุมัติอัตโนมัติ | ✅ |
| เจ้าหน้าที่จองเกิน 8 ชม. / เกิน 90 วัน / จองถูกกฎ | ปฏิเสธ / ปฏิเสธ / อนุมัติอัตโนมัติ | ✅ |
| จองเวลาซ้ำกับรายการเดิม | 409 เวลาชน | ✅ |
| ปฏิเสธโดยไม่มีเหตุผล / มีเหตุผล | ไม่ให้ทำ / REJECTED พร้อมเหตุผลในประวัติ | ✅ |
| ผู้จองได้แจ้งเตือนอนุมัติและปฏิเสธ | ข้อความถูกต้อง | ✅ |
| เจ้าของยกเลิกก่อนเวลาเริ่ม | CANCELLED พร้อมประวัติ | ✅ |
| ค้นหาห้อง กรอง เรียง แบ่งหน้า (16 ห้อง) | ถูกต้อง | ✅ |
| สถิติช่วงวันที่กลับด้าน / วันเดียว | ปฏิเสธ / ชั่วโมงตรงกับการจอง | ✅ |
| หน้าจอมือถือ 390×844 | เมนูใช้ได้ ไม่ล้นจอ | ✅ |

### 6.2 บั๊กที่พบและการแก้

| # | บั๊ก | ระดับ | การแก้ | ยืนยันหลังแก้ |
|---|---|---|---|---|
| 1 | จองห้องเดียวกันพร้อมกันสำเร็จทั้งคู่ | P1 | ล็อกแถวผู้ใช้และห้องก่อนตรวจกฎ | Concurrency test (หัวข้อ 5) |
| 2 | อนุมัติและปฏิเสธพร้อมกันสำเร็จทั้งคู่ | P1 | ล็อกแถวการจองก่อนเปลี่ยนสถานะ, Scheduler ตรวจซ้ำหลังล็อก | Concurrency test (หัวข้อ 5) |
| 3 | สลับรายการแล้วฟอร์มค้างข้อมูลเก่า (ผู้ใช้ ห้อง อุปกรณ์) | P1 | `key` ตาม ID ให้ฟอร์ม | ทดสอบในเบราว์เซอร์ |
| 4 | อนุมัติการจองทับช่วงปิดห้องได้ | P2 | ห้ามสร้างช่วงปิดทับการจอง + ตรวจห้องก่อนอนุมัติ/check-in | `RoomClosureServiceImplTest`, `BookingLifecycleServiceImplTest` + API |
| 5 | รหัสผ่านไทย 25 ตัว (75 ไบต์) ได้ error 500 | P2 | `@MaxUtf8Bytes(72)` ตอบ 400 | `AuthControllerTest` + เว็บจริงตอบ 400 |
| 6 | เปลี่ยนบทบาทคนสุดท้ายแล้วค้างหน้าว่าง | P2 | ถอยไปหน้าสุดท้ายที่มีข้อมูล | ทดสอบในเบราว์เซอร์ |
| 7 | สถานะในหน้ารายละเอียดค้างเมื่อเปลี่ยนจากแท็บอื่น | P2 | ยังไม่แก้ (ระบบตอบ 409 ถูกต้อง แต่หน้าจอต้องรีเฟรช) | - |
| 8 | ตัวกรองชั้นไม่มีชั้น 5–6 | P2 | รายการชั้น 1–6 | ทดสอบในเบราว์เซอร์ |
| 9 | Open redirect ผ่าน `?next=//example.com` | P2 | `safeNext()` รับเฉพาะ path ภายใน | ทดสอบในเบราว์เซอร์ 3 กรณี |
| - | หน้าแบ่งหน้าว่างหลังลบรายการสุดท้าย (4 หน้า) | P2 | ถอยไปหน้าสุดท้ายที่มีข้อมูล | ทดสอบในเบราว์เซอร์ |
| - | ปุ่ม "ค้นหาห้อง" มองไม่เห็น, ปุ่มลบเป็นสีเทา | P3 | เพิ่ม variant ปุ่ม `inverse`, `ghost-danger` | ทดสอบในเบราว์เซอร์ |
| - | CI ล้มเพราะ Docker Hub จำกัดการดึง image (429) | CI | ดึง base image จาก AWS ECR Public | CI ผ่าน |

### 6.3 ตรวจเว็บจริงหลัง deploy การแก้ทั้งหมด

| ตรวจ | ผล |
|---|:-:|
| CI และ Deploy workflow ของ PR #30–#36 เขียวทุกรอบ | ✅ |
| `POST /auth/login` ด้วยรหัสไทย 25 ตัว ตอบ 400 (โค้ดใหม่) ไม่ใช่ 500 | ✅ |
| `/api/v1/schedule/month`, `/day` มีใน Swagger, ไม่มี token ตอบ 401 | ✅ |
| หน้า `/schedule` และ `/help` เปิดได้ | ✅ |
| CORS อนุญาตเฉพาะ Vercel และ localhost (เว็บอื่นได้ 403) | ✅ |

## 7. Postman

`test/postman/sc09-room-booking.postman_collection.json` ครอบคลุม 3 use case หลัก รันด้วย **Run collection** ได้ทันที วันที่จองคำนวณเป็นอีก 2 วันข้างหน้าอัตโนมัติ และขั้นสุดท้ายยกเลิกการจองที่สร้าง จึงรันซ้ำได้

| โฟลเดอร์ | Request | ตรวจ |
|---|---|---|
| Auth | Login wrong password | 401 |
| Use case 1 | Login (student), Rooms (page/sort/filter), Available rooms | 200 |
| Use case 2 | Create booking (student), Create same slot again | 201 + PENDING, 409 |
| Use case 3 | Login (staff), Approve, History, Stats, Login (student), My notifications | 200, APPROVED, มีแจ้งเตือน BOOKING_APPROVED |
| Cleanup | Cancel booking | 200 |

## 8. ข้อจำกัดของการทดสอบ

- Test อัตโนมัติรันบน H2 โหมด PostgreSQL ส่วนการล็อกแถวบน PostgreSQL จริงยืนยันด้วยการใช้งานบนเว็บที่ deploy
- Controller และ security มี coverage ต่ำ เพราะ Controller Test ปิด security filter และยังไม่มี test ของ controller บางตัว (ห้องประเภท อุปกรณ์ ผู้ใช้ สถิติ ช่วงปิดห้อง)
- ฝั่ง frontend ยังไม่มี automated test (ใช้ `tsc`, `eslint`, `next build` ใน CI และทดสอบในเบราว์เซอร์)
- บั๊กข้อ 7 (สถานะค้างเมื่อเปลี่ยนจากแท็บอื่น) ยังไม่แก้

## 9. วิธีรันซ้ำ

```bash
cd code/backend
./mvnw test      # 226 tests
./mvnw verify    # + JaCoCo coverage → target/site/jacoco/index.html
```

```bash
cd code/frontend
npm run lint
npm run build
```
