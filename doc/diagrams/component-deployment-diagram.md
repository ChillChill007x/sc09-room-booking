# Component Diagram และ Deployment Diagram

## 1. Component Diagram

```mermaid
flowchart TB
    subgraph Browser["เบราว์เซอร์ผู้ใช้"]
        subgraph FE["Frontend (Next.js)"]
            Pages["Pages (app/)<br/>rooms, schedule, bookings, notifications,<br/>admin/*, login, register, profile, help"]
            Comp["Components<br/>Navbar, NotificationBell, RoomSchedule,<br/>RoomClosureForm, BookingStatusBadge, ui"]
            Ctx["AuthContext + RouteGuard"]
            Lib["API Client (lib/)<br/>api.ts (fetch + JWT), auth, rooms,<br/>bookings, lifecycle, notifications, stats"]
            Pages --> Comp
            Pages --> Ctx
            Pages --> Lib
            Ctx --> Lib
        end
    end

    subgraph BE["Backend (Spring Boot)"]
        Sec["Security<br/>JwtAuthenticationFilter, JwtService,<br/>SecurityConfig (CORS, stateless)"]
        Ctrl["REST Controllers (/api/v1)<br/>Auth, User, Room, RoomType, Equipment,<br/>RoomClosure, Booking, BookingLifecycle,<br/>Schedule, Notification, Stats"]
        subgraph Svc["Services"]
            UserMod["โมดูลผู้ใช้<br/>AuthService, UserService"]
            RoomMod["โมดูลห้อง<br/>RoomService, RoomQueryService,<br/>RoomClosureService, Equipment, RoomType"]
            BookMod["โมดูลการจอง<br/>BookingService, BookingQueryService,<br/>ScheduleService, ValidationChain, Policy"]
            LifeMod["โมดูลวงจรสถานะ<br/>BookingLifecycleService, State,<br/>BookingStatusScheduler"]
            NotiMod["โมดูลแจ้งเตือนและสถิติ<br/>NotificationListener, NotificationService,<br/>NotificationSender, StatsService"]
        end
        Repo["Repositories (Spring Data JPA)"]
        Ex["GlobalExceptionHandler<br/>ErrorResponse มาตรฐาน"]
        Flyway["Flyway Migration V1-V5"]
        Docs["springdoc-openapi<br/>Swagger UI"]
    end

    DB[("PostgreSQL 17")]

    Lib -- "REST / JSON + Bearer JWT" --> Sec
    Sec --> Ctrl
    Ctrl --> UserMod
    Ctrl --> RoomMod
    Ctrl --> BookMod
    Ctrl --> LifeMod
    Ctrl --> NotiMod
    BookMod -- "RoomQueryService" --> RoomMod
    RoomMod -- "BookingQueryService" --> BookMod
    LifeMod -- "RoomQueryService" --> RoomMod
    BookMod -. "BookingCreatedEvent" .-> NotiMod
    LifeMod -. "BookingStatusChangedEvent" .-> NotiMod
    UserMod --> Repo
    RoomMod --> Repo
    BookMod --> Repo
    LifeMod --> Repo
    NotiMod --> Repo
    Ctrl -. "exception" .-> Ex
    Repo --> DB
    Flyway --> DB
```

| Component | หน้าที่ | ผู้รับผิดชอบ |
|---|---|---|
| Security + โมดูลผู้ใช้ | สมัคร login ออก JWT ตรวจสิทธิ์ จัดการผู้ใช้และโปรไฟล์ | คนที่ 1 |
| โมดูลห้อง | CRUD ห้อง ประเภท อุปกรณ์ ค้นหา/กรอง/แบ่งหน้า ห้องว่าง ช่วงปิดห้อง | คนที่ 2 |
| โมดูลการจอง | CRUD การจอง ตรวจกฎ (Chain + Strategy) ตรวจเวลาชน ตารางการใช้ห้อง | คนที่ 3 (Policy: คนที่ 1) |
| โมดูลวงจรสถานะ | อนุมัติ ปฏิเสธ ยกเลิก check-in ประวัติสถานะ Scheduler | คนที่ 4 |
| โมดูลแจ้งเตือนและสถิติ | Observer สร้างแจ้งเตือน API แจ้งเตือน สถิติ | คนที่ 5 |
| GlobalExceptionHandler | แปลง exception เป็น HTTP status + `ErrorResponse` (400/401/403/404/409/500) | คนที่ 1 |

โมดูลคุยกันผ่าน interface เล็ก (`RoomQueryService`, `BookingQueryService`) หรือผ่าน event เท่านั้น ไม่เรียก repository ของโมดูลอื่นโดยตรง

## 2. Deployment Diagram (Production)

```mermaid
flowchart TB
    user(["ผู้ใช้<br/>เบราว์เซอร์ / มือถือ"])

    subgraph Vercel["Vercel (Hobby)"]
        fe["Next.js production build<br/>sc09-room-booking.vercel.app"]
    end

    subgraph Render["Render Web Service (Free, Singapore)"]
        subgraph Docker["Docker container: eclipse-temurin:17-jre"]
            jar["Spring Boot app.jar<br/>port $PORT<br/>sc09-room-booking.onrender.com"]
        end
    end

    subgraph Neon["Neon (Free, AWS ap-southeast-1)"]
        pg[("PostgreSQL 17<br/>database: cp_room_booking")]
    end

    subgraph GitHub["GitHub: ChillChill007x/sc09-room-booking"]
        repo["Repository<br/>branch protection: develop, main"]
        ci["GitHub Actions<br/>ci.yml: backend test, frontend lint/build,<br/>docker images build"]
        cd["GitHub Actions<br/>deploy.yml: เมื่อ push เข้า develop"]
    end

    ecr["AWS ECR Public<br/>base images (mirror ของ Docker Hub)"]

    user -- HTTPS --> fe
    user -- "HTTPS (fetch API, CORS)" --> jar
    jar -- "JDBC + SSL (sslmode=require)" --> pg
    repo --> ci
    repo --> cd
    cd -- "ขั้นที่ 1: เรียก CI ก่อน" --> ci
    cd -- "ขั้นที่ 2: Render Deploy Hook" --> Render
    cd -- "ขั้นที่ 3: vercel build + deploy --prod" --> Vercel
    ecr -. "docker pull" .-> Docker
    ecr -. "docker pull (CI)" .-> ci
```

| Node | เทคโนโลยี | ค่าที่ตั้ง (ไม่ใส่ค่าลับ) |
|---|---|---|
| Vercel | Next.js build | `NEXT_PUBLIC_API_URL` = URL ของ backend (ฝังตอน build) |
| Render | Docker (Dockerfile แบบ multi-stage ใน `code/backend`) | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`, Health check `/v3/api-docs`, Auto-Deploy ปิด |
| Neon | PostgreSQL 17 (direct connection ไม่ใช้ pooler เพราะ Flyway) | database `cp_room_booking` |
| GitHub Actions | `ci.yml`, `deploy.yml` | Secrets: `RENDER_DEPLOY_HOOK_URL`, `BACKEND_URL`, `VERCEL_TOKEN`, `VERCEL_ORG_ID`, `VERCEL_PROJECT_ID` |

ข้อจำกัดของแผนฟรี: Render หลับเมื่อไม่มีคนใช้ 15 นาที request แรกหลังจากนั้นช้าประมาณ 1 นาที และ Neon หยุด compute เมื่อไม่ได้ใช้

## 3. Deployment บนเครื่องนักพัฒนา (Docker Compose)

```mermaid
flowchart LR
    dev(["นักพัฒนา<br/>localhost"])
    subgraph Compose["docker compose up --build"]
        f["frontend<br/>node:22-alpine<br/>:3000"]
        b["backend<br/>eclipse-temurin:17-jre<br/>:8080"]
        p[("postgres:17-alpine<br/>:5432<br/>volume pgdata")]
    end
    dev --> f
    dev --> b
    f -- "REST API ที่พอร์ต 8080" --> b
    b -- "JDBC ที่พอร์ต 5432" --> p
```

backend รอ postgres ผ่าน healthcheck (`pg_isready`) ก่อนเริ่ม ส่วนการพัฒนาแบบไม่ใช้ Docker ใช้ `./mvnw spring-boot:run` กับ `npm run dev` ได้ ดู README
