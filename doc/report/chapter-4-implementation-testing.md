# บทที่ 4 การพัฒนาและการทดสอบระบบ

## 4.1 โครงสร้างโปรเจกต์

```text
sc09-room-booking/
├── code/
│   ├── backend/                 Spring Boot (Java 17, Maven)
│   │   └── src/main/java/com/example/cp_room_booking/
│   │       ├── common/          BaseEntity, PageResponse
│   │       ├── config/          Security + CORS, OpenAPI, Clock, Scheduling
│   │       ├── controller/api/  REST Controller 11 ตัว
│   │       ├── domain/          entity 11 ตัว และ enum 6 ตัว
│   │       ├── dto/             request, response, validation (@MaxUtf8Bytes)
│   │       ├── event/           BookingCreatedEvent, BookingStatusChangedEvent, NotificationListener
│   │       ├── exception/       exception ของระบบ + GlobalExceptionHandler
│   │       ├── mapper/          แปลง entity ↔ DTO
│   │       ├── repository/      Spring Data JPA
│   │       ├── security/        JWT filter, UserPrincipal, validator
│   │       └── service/         interface, impl, policy, validation, state, notification, scheduler
│   └── frontend/                Next.js 16 (TypeScript)
│       ├── app/                 หน้าเว็บตาม route
│       ├── components/          ui.tsx, Navbar, NotificationBell, RoomSchedule, RouteGuard ฯลฯ
│       ├── context/             AuthContext
│       └── lib/                 API client, type, ฟังก์ชันช่วย
├── doc/                         เอกสารออกแบบ diagram และรายงาน
├── test/                        Test report และ Postman collection
├── docker-compose.yml
├── render.yaml
└── .github/workflows/           ci.yml, deploy.yml
```

ขนาดของโค้ด: backend 155 ไฟล์ Java, test 37 ไฟล์, frontend 36 ไฟล์ TypeScript/TSX

## 4.2 การพัฒนาส่วน Backend

### 4.2.1 การยืนยันตัวตนและสิทธิ์

`JwtAuthenticationFilter` อ่าน token จาก header ตรวจลายเซ็นและวันหมดอายุ โหลดผู้ใช้ และตรวจว่าบัญชียังเปิดใช้งาน จากนั้นใส่ `UserPrincipal` ไว้ใน `SecurityContext` สิทธิ์ระดับ endpoint ประกาศด้วย `@PreAuthorize` เช่น

```java
@PreAuthorize("hasAnyRole('STAFF','ADMIN') or #id == principal.id")
@GetMapping("/{id}")
public UserResponse findById(@PathVariable Long id) { ... }
```

ข้อผิดพลาดด้านสิทธิ์ส่งผ่าน `RestAuthenticationEntryPoint` (401) และ `RestAccessDeniedHandler` (403) ให้มีรูปแบบ JSON เดียวกับ error อื่น

### 4.2.2 การสร้างการจอง

`BookingServiceImpl.create()` ทำงานใน transaction เดียว

1. ล็อกแถวผู้ใช้และห้อง (`SELECT ... FOR UPDATE`) ตามลำดับเดียวกันทุกครั้ง
2. สร้าง `BookingValidationContext` และเลือก `BookingPolicy` ตามบทบาท (Strategy)
3. ส่ง context ผ่าน chain ตรวจกฎ 5 ขั้น (Chain of Responsibility) ถ้าขั้นไหนไม่ผ่านจะโยน exception ทันที
4. กำหนดสถานะเริ่มต้นจาก `policy.requiresApproval()` แล้วบันทึกการจองและประวัติสถานะ
5. publish `BookingCreatedEvent` ซึ่ง `NotificationListener` จะรับหลัง commit สำเร็จ (Observer)

```java
BookingStatus initial = context.getPolicy().requiresApproval()
        ? BookingStatus.PENDING : BookingStatus.APPROVED;
```

### 4.2.3 วงจรสถานะและ Scheduler

`BookingLifecycleServiceImpl.changeStatus()` ล็อกแถวการจอง อ่านสถานะล่าสุด ขอ object สถานะจาก `BookingStateFactory` แล้วเรียก method ตาม action (approve, reject, cancel, checkIn, complete, markNoShow) สถานะที่ไม่รองรับ action นั้นโยน `InvalidBookingStateException` (409) โดยไม่ต้องเขียน `if` ตามสถานะใน service (State Pattern)

`BookingStatusScheduler` รันทุก 1 นาที (`app.scheduler.fixed-delay-ms=60000`) หาการจอง APPROVED ที่เลยเวลาเริ่ม 15 นาทีแล้วเปลี่ยนเป็น NO_SHOW และการจอง CHECKED_IN ที่เลยเวลาสิ้นสุดเปลี่ยนเป็น COMPLETED โดยล็อกแถวและตรวจสถานะซ้ำก่อนเปลี่ยน เพื่อไม่ทับการกระทำของผู้ใช้ที่เกิดพร้อมกัน

### 4.2.4 ห้อง อุปกรณ์ และการค้นหา

การค้นหาห้องใช้ JPA Specification ประกอบเงื่อนไข (คำค้น ชั้น ประเภท ความจุขั้นต่ำ อุปกรณ์) รองรับการเรียงและแบ่งหน้าด้วย `Pageable` ส่วนห้องว่างใช้ `BookingQueryService.findBookedRoomIds()` และช่วงปิดห้องตัดห้องที่ไม่ว่างออก การกำหนดอุปกรณ์ให้ห้องผ่าน `PUT /rooms/{id}/equipment` แทนที่รายการทั้งหมดพร้อมจำนวน

### 4.2.5 แจ้งเตือนและสถิติ

`NotificationServiceImpl` วน `List<NotificationSender>` ที่ Spring inject ให้ ตอนนี้มี `InAppNotificationSender` ตัวเดียว ถ้าจะเพิ่มอีเมลหรือ LINE ทำได้โดยเพิ่ม class ใหม่ที่ implement `NotificationSender` โดยไม่ต้องแก้โค้ดเดิม สถิติใช้ query รวมกลุ่ม (`GROUP BY`) ตามสถานะและตามห้อง

### 4.2.6 การจัดการข้อผิดพลาด

`GlobalExceptionHandler` (`@RestControllerAdvice`) แปลง exception เป็น HTTP status เช่น `ResourceNotFoundException` → 404, `ConflictException` และ `InvalidBookingStateException` → 409, `BusinessRuleException` → 400, `ForbiddenOperationException` → 403, `DataIntegrityViolationException` → 409, `MethodArgumentNotValidException` → 400 พร้อม `fieldErrors` และ exception อื่น → 500 พร้อมข้อความกลาง

### 4.2.7 ฐานข้อมูลและ Migration

ใช้ Flyway V1–V5 สร้างตารางและข้อมูลตั้งต้น (ห้อง 16 ห้อง ประเภท อุปกรณ์ และบัญชีตัวอย่าง) และตั้ง `spring.jpa.hibernate.ddl-auto=validate` ให้ Hibernate ตรวจว่า entity ตรงกับตาราง ไม่สร้างตารางเอง ทีมตกลงว่าห้ามแก้ไฟล์ migration ที่รันแล้ว ถ้าต้องเปลี่ยนให้สร้างเวอร์ชันใหม่

## 4.3 การพัฒนาส่วน Frontend

- `lib/api.ts` เป็น API client กลาง แนบ JWT อัตโนมัติ แปลง error JSON ของ backend เป็นข้อความภาษาไทย และออกจากระบบเมื่อ token หมดอายุ (401)
- `context/AuthContext` เก็บสถานะผู้ใช้ และใช้ซ่อน/แสดงเมนูตามบทบาท
- `components/ui.tsx` รวม Button, Input, Select, Textarea, Field, Card, PageHeader, Alert, Spinner, EmptyState, Badge, Pagination ใช้ร่วมกันทุกหน้า และ `RouteGuard` กันหน้าที่ต้อง login หรือต้องเป็น S/A
- หน้า `/schedule` แสดงปฏิทินรายเดือน (จำนวนการจองแต่ละวัน) และตารางห้อง × เวลา ของวันที่เลือก รวมช่วงปิดห้อง
- หน้า `/rooms/[id]/book` แสดงกฎของบทบาทผู้ใช้ก่อนส่ง และแสดง error จาก chain ตรวจกฎให้ผู้ใช้แก้ได้ทันที
- หน้า `/help` อธิบายขั้นตอนการจอง กฎตามบทบาท และความหมายของสถานะ

## 4.4 การทำงานเป็นทีมด้วย Git

### 4.4.1 Branch และ Pull Request

| Branch | หน้าที่ |
|---|---|
| `main` | โค้ดที่ส่งงาน |
| `develop` | รวมงานของทีม และ deploy อัตโนมัติ |
| `<ชื่อ>_<รหัส>_<section>` | branch ของสมาชิกแต่ละคน |

ทุกการรวมโค้ดผ่าน Pull Request โดย branch protection บน `develop` และ `main` บังคับให้มีผู้อนุมัติอย่างน้อย 1 คนและ CI ผ่านทั้ง 3 job ใช้ Conventional Commits เช่น `feat:`, `fix:`, `docs:`, `test:`, `ci:`

### 4.4.2 สถิติการมีส่วนร่วม (ช่วง 6–10 ต.ค. 2569)

| สมาชิก | Commit (ไม่นับ merge) |
|---|---:|
| ศุภกิตติ์ | 40 |
| ดรัณภพ | 36 |
| อนัตตา | 33 |
| กฤษฎา | 31 |
| พัชรพล | 23 |
| **รวม** | **163** |

รวม Pull Request ที่ merge แล้ว 35 รายการ

## 4.5 CI/CD และการ Deploy

### 4.5.1 Continuous Integration (`ci.yml`)

ทำงานทุก Pull Request และ push มี 3 job ทำงานขนานกัน

| Job | ขั้นตอน |
|---|---|
| Backend build and test | ตั้ง JDK 17 → `mvnw verify` (รัน test ทั้งหมด + JaCoCo) → สรุปผลและอัปโหลดรายงาน Surefire/JaCoCo |
| Frontend lint and build | ตั้ง Node 22 → `npm ci` → `npm run lint` → `npm run build` |
| Docker images build | build image ของ backend และ frontend (ลองใหม่อัตโนมัติเมื่อติด rate limit) |

### 4.5.2 Continuous Deployment (`deploy.yml`)

เมื่อ push เข้า `develop` workflow จะเรียก CI ก่อน ถ้าผ่านจึง

1. เรียก Deploy Hook ของ Render ให้ build backend จาก Dockerfile ใหม่ (Flyway รัน migration ตอนเริ่มระบบ)
2. ใช้ Vercel CLI build และ deploy frontend ไปยัง production

ค่าลับ (`RENDER_DEPLOY_HOOK_URL`, `VERCEL_TOKEN`, `VERCEL_ORG_ID`, `VERCEL_PROJECT_ID`) เก็บใน GitHub Secrets ส่วน `JWT_SECRET` และข้อมูลเชื่อมต่อฐานข้อมูลเก็บใน environment ของ Render

### 4.5.3 สภาพแวดล้อม Production

| ส่วน | บริการ | URL |
|---|---|---|
| Frontend | Vercel | https://sc09-room-booking.vercel.app |
| Backend API | Render (Docker) | https://sc09-room-booking.onrender.com/api/v1 |
| เอกสาร API | Swagger UI | https://sc09-room-booking.onrender.com/swagger-ui.html |
| ฐานข้อมูล | Neon PostgreSQL 17 | (เข้าถึงจาก backend เท่านั้น) |

Base image ของ Docker ดึงจาก AWS ECR Public mirror แทน Docker Hub เพื่อเลี่ยงปัญหา rate limit ของ CI แผนภาพการ deploy อยู่ใน [component-deployment-diagram.md](../diagrams/component-deployment-diagram.md)

### 4.5.4 การรันบนเครื่อง

```bash
docker compose up --build
```

เปิด frontend ที่ `http://localhost:3000` และ Swagger ที่ `http://localhost:8080/swagger-ui.html` (รายละเอียดอยู่ใน README)

## 4.6 การทดสอบระบบ

### 4.6.1 สรุปผล

| รายการ | ผล |
|---|---|
| จำนวน test | 226 test ใน 36 class ผ่านทั้งหมด |
| Line coverage | 72.9% |
| Branch coverage | 63.4% |
| Instruction coverage | 73.9% |
| Method coverage | 69.3% |

| ประเภท | Class | Test |
|---|---:|---:|
| Unit Test (service, policy, chain, state, listener) | 21 | 160 |
| Controller Test (`@WebMvcTest`) | 6 | 30 |
| Repository Test (`@DataJpaTest` + Flyway) | 5 | 28 |
| Integration Test (`@SpringBootTest`) | 4 | 8 |

### 4.6.2 การทดสอบการทำงานพร้อมกัน

ส่งคำขอจองห้องเดียวกันเวลาเดียวกัน 5 คำขอพร้อมกัน ก่อนแก้สำเร็จ 5 รายการ หลังเพิ่ม pessimistic lock สำเร็จ 1 รายการ ส่วนการอนุมัติและปฏิเสธพร้อมกัน ก่อนแก้สำเร็จทั้ง 2 คำขอ หลังแก้สำเร็จ 1 คำขอ อีกคำขอได้ 409

### 4.6.3 การทดสอบบนเว็บจริง

ทีมทดสอบบนเว็บ production ตามบทบาท (นักศึกษา อาจารย์ เจ้าหน้าที่ ผู้ดูแล) และทดสอบ API ด้วย Postman collection พบบั๊ก 9 รายการ แก้แล้ว 8 รายการ สรุปดังนี้

| ลำดับ | บั๊ก | ระดับ | การแก้ |
|---|---|---|---|
| 1 | จองห้องเดียวกันพร้อมกันสำเร็จทั้งคู่ | P1 | ล็อกแถวผู้ใช้และห้องก่อนตรวจกฎ |
| 2 | อนุมัติและปฏิเสธพร้อมกันสำเร็จทั้งคู่ | P1 | ล็อกแถวการจองก่อนเปลี่ยนสถานะ |
| 3 | ฟอร์มค้างข้อมูลเก่าเมื่อสลับรายการ | P1 | ใส่ `key` ตาม ID |
| 4 | อนุมัติการจองทับช่วงปิดห้องได้ | P2 | ตรวจช่วงปิดตอนสร้าง closure และก่อนอนุมัติ/check-in |
| 5 | รหัสผ่านภาษาไทยยาวได้ error 500 | P2 | `@MaxUtf8Bytes(72)` ตอบ 400 |
| 6 | เปลี่ยนบทบาทคนสุดท้ายแล้วค้างหน้าว่าง | P2 | ถอยไปหน้าที่มีข้อมูล |
| 7 | สถานะในหน้ารายละเอียดไม่อัปเดตเมื่อเปลี่ยนจากแท็บอื่น | P2 | ยังไม่แก้ (backend ตอบ 409 ถูกต้อง) |
| 8 | ตัวกรองชั้นไม่มีชั้น 5–6 | P2 | แสดงชั้น 1–6 |
| 9 | Open redirect ผ่าน `?next=//example.com` | P2 | รับเฉพาะ path ภายใน |

รายละเอียด test รายคลาส coverage แยก package traceability และวิธีรันซ้ำอยู่ใน [test-report.md](../../test/test-report.md)
