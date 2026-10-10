# Sequence Diagrams

ลำดับการทำงานของ use case สำคัญ ใช้ชื่อ class และ method ตามโค้ดจริง

| # | Sequence | Use Case |
|---|---|---|
| 1 | เข้าสู่ระบบและเรียก API ด้วย JWT | UC-02 |
| 2 | ค้นหาห้องว่าง | UC-04 |
| 3 | สร้างการจอง (ล็อก + Chain of Responsibility + Strategy + Observer) | UC-06 |
| 4 | อนุมัติการจอง (ล็อก + State + Observer) | UC-13 |
| 5 | Scheduler บันทึกไม่มาใช้ห้อง | UC-18 |
| 6 | สร้างช่วงปิดห้อง | UC-15 |
| 7 | ตารางการใช้ห้อง | UC-10 |

## 1. เข้าสู่ระบบและเรียก API ด้วย JWT

```mermaid
sequenceDiagram
    autonumber
    actor U as ผู้ใช้
    participant FE as Frontend (Next.js)
    participant AC as AuthController
    participant AS as AuthServiceImpl
    participant UR as UserRepository
    participant PE as BCryptPasswordEncoder
    participant JS as JwtService
    participant F as JwtAuthenticationFilter
    participant C as Controller อื่น

    U->>FE: กรอกอีเมลและรหัสผ่าน
    FE->>AC: POST /api/v1/auth/login
    AC->>AC: validate LoginRequest (@Email, @NotBlank, @MaxUtf8Bytes 72)
    AC->>AS: login(request)
    AS->>UR: findByEmail(email ตัวพิมพ์เล็ก)
    UR-->>AS: Optional<User>
    AS->>PE: matches(password, passwordHash)
    alt ไม่พบผู้ใช้ หรือรหัสผ่านไม่ตรง
        AS-->>FE: 401 อีเมลหรือรหัสผ่านไม่ถูกต้อง
    else บัญชีถูกปิดใช้งาน
        AS-->>FE: 401 บัญชีนี้ถูกปิดใช้งาน
    else สำเร็จ
        AS->>JS: generateToken(UserPrincipal)
        JS-->>AS: JWT (sub=email, uid, role, หมดอายุ 480 นาที)
        AS-->>FE: 200 AuthResponse { accessToken, user }
        FE->>FE: เก็บ token ใน localStorage และไปหน้าที่ปลอดภัย (safeNext)
    end

    Note over FE,C: request ถัดไปทุกครั้ง
    FE->>F: GET /api/v1/... + Authorization: Bearer token
    F->>JS: ตรวจลายเซ็นและวันหมดอายุ
    F->>F: โหลด UserPrincipal ใส่ SecurityContext
    F->>C: ส่งต่อ request
    C-->>FE: 200 / 403 (ไม่มีสิทธิ์ตาม @PreAuthorize)
```

## 2. ค้นหาห้องว่าง

```mermaid
sequenceDiagram
    autonumber
    actor U as ผู้ใช้
    participant FE as หน้า /rooms
    participant RC as RoomController
    participant RS as RoomServiceImpl
    participant BQ as BookingQueryService
    participant CR as RoomClosureRepository
    participant RR as RoomRepository

    U->>FE: เลือกวัน เวลา จำนวนคน อุปกรณ์
    FE->>RC: GET /api/v1/rooms/available?start&end&minCapacity&equipmentIds
    RC->>RS: findAvailable(start, end, minCapacity, equipmentIds)
    alt start ไม่ก่อน end
        RS-->>FE: 400 เวลาเริ่มต้องมาก่อนเวลาสิ้นสุด
    else
        RS->>BQ: findBookedRoomIds(start, end)
        BQ-->>RS: ห้องที่มีการจอง PENDING/APPROVED/CHECKED_IN ทับ
        RS->>CR: findClosedRoomIds(start, end)
        CR-->>RS: ห้องที่มีช่วงปิดทับ
        RS->>RR: findAll(Specification: ACTIVE + ความจุ + อุปกรณ์ครบ + id not in ที่ไม่ว่าง)
        RR-->>RS: List<Room> เรียงตามชั้นและรหัส
        RS-->>FE: 200 List<RoomResponse>
    end
    FE-->>U: แสดงห้องว่างพร้อมปุ่มจอง
```

## 3. สร้างการจอง

```mermaid
sequenceDiagram
    autonumber
    actor U as นักศึกษา
    participant BC as BookingController
    participant BS as BookingServiceImpl
    participant BR as BookingRepository
    participant VC as BookingValidationChain
    participant T as TimeRangeHandler
    participant P as PolicyHandler
    participant PR as BookingPolicyResolver
    participant R as RoomHandler
    participant CL as ClosureHandler
    participant CF as ConflictHandler
    participant EP as ApplicationEventPublisher
    participant NL as NotificationListener

    U->>BC: POST /api/v1/bookings (roomId, start, end, purpose, attendees)
    BC->>BS: create(actor, request)
    Note over BS,BR: ล็อกแถวผู้ใช้แล้วห้อง (SELECT ... FOR UPDATE)<br/>คำขอที่ชนกันจะรอจน transaction แรก commit
    BS->>BR: lockUser(userId)
    BS->>BR: lockRoom(roomId)
    BS->>VC: validate(context)
    VC->>T: ภายในเวลาอาคาร 08:00-22:00, วันเดียว, ไม่ใช่อดีต
    T->>P: next
    P->>PR: resolve(role)
    PR-->>P: StudentBookingPolicy
    P->>P: ≤ 2 ชม., ล่วงหน้า ≤ 7 วัน, ค้าง < 2 รายการ
    P->>R: next
    R->>R: getActiveRoom(roomId), attendees ≤ capacity
    R->>CL: next
    CL->>CL: isClosed(roomId, start, end) ต้องเป็น false
    CL->>CF: next
    CF->>BR: existsOverlap(roomId, start, end, ACTIVE, -1)
    alt ผิดกฎข้อใดข้อหนึ่ง
        CF-->>U: 400 (กฎ) / 404 (ไม่พบห้อง) / 409 (ปิดห้องหรือเวลาชน)
    else ผ่านทุกขั้น
        BS->>BS: status = policy.requiresApproval() ? PENDING : APPROVED
        BS->>BR: save(booking + history "สร้างการจอง")
        BS->>EP: publishEvent(BookingCreatedEvent)
        BS-->>U: 201 BookingResponse (PENDING)
        Note over EP,NL: หลัง transaction commit (AFTER_COMMIT)
        EP->>NL: onBookingCreated(event)
        NL->>NL: notificationService.notifyStaff(...) แจ้งเจ้าหน้าที่และผู้ดูแลระบบทุกคน
    end
```

## 4. อนุมัติการจอง

```mermaid
sequenceDiagram
    autonumber
    actor S as เจ้าหน้าที่
    participant LC as BookingLifecycleController
    participant LS as BookingLifecycleServiceImpl
    participant LR as BookingLifecycleRepository
    participant BR as BookingRepository
    participant SF as BookingStateFactory
    participant ST as PendingState
    participant RQ as RoomQueryService
    participant EP as ApplicationEventPublisher
    participant NL as NotificationListener

    S->>LC: PATCH /api/v1/bookings/{id}/status { action: APPROVE }
    LC->>LS: changeStatus(id, APPROVE, note, actor)
    LS->>LR: lockById(id) (SELECT ... FOR UPDATE)
    Note over LS,LR: ถ้ามีอีกคนกำลังอนุมัติหรือปฏิเสธรายการเดียวกัน จะรอจนเสร็จ
    LS->>BR: findDetailById(id)
    BR-->>LS: Booking (อ่านสถานะล่าสุดหลังได้ล็อก)
    LS->>LS: isPermitted (APPROVE ต้องเป็นเจ้าหน้าที่) ไม่ผ่าน → 403
    LS->>SF: of(booking.status)
    SF-->>LS: PendingState
    LS->>ST: canHandle(APPROVE)
    alt สถานะเปลี่ยนไปแล้ว (เช่นอีกคนปฏิเสธก่อน)
        ST-->>S: 409 InvalidBookingStateException
    else รับได้
        LS->>LS: timeViolation: ต้องก่อนเวลาเริ่ม ไม่ผ่าน → 400
        LS->>RQ: getActiveRoom(roomId) ห้องไม่ ACTIVE → 400
        LS->>RQ: isClosed(roomId, start, end) มีช่วงปิดทับ → 409
        LS->>ST: next(APPROVE)
        ST-->>LS: APPROVED
        LS->>LS: booking.changeStatus(APPROVED, staff, note, now) + history
        LS->>EP: publishEvent(BookingStatusChangedEvent)
        LS-->>S: 200 BookingResponse (APPROVED)
        Note over EP,NL: หลัง commit
        EP->>NL: onStatusChanged(event)
        NL->>NL: notifyUser(ผู้จอง, BOOKING_APPROVED, "...ได้รับการอนุมัติแล้ว")
    end
```

การปฏิเสธ (`REJECT`) ใช้ลำดับเดียวกัน ต่างกันที่ต้องมีเหตุผล (`note`) และไม่ตรวจห้อง ส่วนการ check-in ตรวจห้องเหมือนการอนุมัติ และต้องอยู่ในช่วง 15 นาทีก่อนถึง 15 นาทีหลังเวลาเริ่ม

## 5. Scheduler บันทึกไม่มาใช้ห้อง

```mermaid
sequenceDiagram
    autonumber
    participant SC as BookingStatusScheduler
    participant LS as BookingLifecycleServiceImpl
    participant LR as BookingLifecycleRepository
    participant EP as ApplicationEventPublisher
    participant NL as NotificationListener

    loop ทุก 1 นาที (app.scheduler.fixed-delay-ms)
        SC->>LS: markNoShows()
        LS->>LR: findByStatusAndStartTimeBefore(APPROVED, now - 15 นาที)
        LR-->>LS: การจองที่เลยเวลา check-in
        loop แต่ละรายการ
            LS->>LR: lockById(id)
            LS->>LR: findStatusById(id)
            alt ยังเป็น APPROVED
                LS->>LS: changeStatus(NO_SHOW, changedBy = null) + history
                LS->>EP: publishEvent(BookingStatusChangedEvent)
                EP->>NL: แจ้งผู้จอง BOOKING_NO_SHOW (หลัง commit)
            else เพิ่ง check-in ทันก่อนล็อก
                LS->>LS: ข้ามรายการนี้
            end
        end
        SC->>LS: completeFinished()
        LS->>LR: findByStatusAndEndTimeBefore(CHECKED_IN, now)
        LS->>LS: CHECKED_IN → COMPLETED (ไม่ส่งแจ้งเตือน)
    end
```

## 6. สร้างช่วงปิดห้อง

```mermaid
sequenceDiagram
    autonumber
    actor A as ผู้ดูแลระบบ
    participant CC as RoomClosureController
    participant CS as RoomClosureServiceImpl
    participant RR as RoomRepository
    participant CR as RoomClosureRepository
    participant BQ as BookingQueryService

    A->>CC: POST /api/v1/rooms/{roomId}/closures (start, end, reason)
    CC->>CS: create(roomId, request)
    CS->>RR: lockById(roomId) (แถวเดียวกับที่การจองล็อก)
    CS->>RR: findById(roomId) ไม่พบ → 404
    CS->>CS: start ต้องก่อน end ไม่ผ่าน → 400
    CS->>CR: existsOverlap(roomId, start, end)
    alt ทับช่วงปิดเดิม
        CR-->>A: 409 ช่วงเวลานี้ทับกับช่วงปิดห้องที่มีอยู่แล้ว
    else
        CS->>BQ: hasActiveBookingOverlap(roomId, start, end)
        alt มีการจองที่ยังใช้งานทับ
            BQ-->>A: 409 มีการจองที่ยังใช้งานอยู่ในช่วงเวลานี้ กรุณายกเลิกหรือปฏิเสธก่อน
        else
            CS->>CR: save(closure)
            CS-->>A: 201 RoomClosureResponse
        end
    end
```

## 7. ตารางการใช้ห้อง

```mermaid
sequenceDiagram
    autonumber
    actor U as ผู้ใช้ที่ login
    participant FE as หน้า /schedule
    participant SC as ScheduleController
    participant SS as ScheduleServiceImpl
    participant BR as BookingRepository
    participant RC as RoomController / RoomClosureController

    U->>FE: เปิดหน้าตารางการใช้ห้อง
    par โหลดครั้งเดียวตอนเปิดหน้า
        FE->>RC: GET /rooms?size=100
        FE->>RC: GET /rooms/{id}/closures (ทุกห้อง)
    and ปฏิทินเดือน
        FE->>SC: GET /schedule/month?month=yyyy-MM
        SC->>SS: monthSummary(month)
        SS->>BR: findAllInRange(ต้นเดือน, ต้นเดือนถัดไป, PENDING/APPROVED/CHECKED_IN/COMPLETED)
        SS-->>FE: [{ date, bookings }] เฉพาะวันที่มีการจอง
    and ตารางวันที่เลือก
        FE->>SC: GET /schedule/day?date=yyyy-MM-dd
        SC->>SS: daySchedule(date)
        SS->>BR: findAllInRange(วันนั้น, วันถัดไป, ...)
        SS-->>FE: [{ bookingId, roomId, roomCode, start, end, status }] ไม่มีข้อมูลผู้จอง
    end
    FE-->>U: ปฏิทิน + ตารางห้อง × เวลา (การจอง, ช่วงปิดห้อง, ห้องปิดซ่อม)
    U->>FE: เลือกวันอื่น
    FE->>FE: ซ่อนข้อมูลวันเดิม แสดง Spinner
    FE->>SC: GET /schedule/day?date=...
```
