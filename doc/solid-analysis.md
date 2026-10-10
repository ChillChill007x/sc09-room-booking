# SOLID Analysis

วิเคราะห์หลัก SOLID จากโค้ดจริงของ backend (`code/backend/src/main/java/com/example/cp_room_booking/`) แต่ละหลักมีตัวอย่างที่ทำตาม และจุดที่ยังปรับปรุงได้ตามจริง

| หลัก | สรุปการใช้ในระบบ |
|---|---|
| S — Single Responsibility | แยก layer, แยก handler ตามกฎ, แยก mapper, แยกโมดูลตามผู้รับผิดชอบ |
| O — Open/Closed | เพิ่มบทบาท กฎการจอง สถานะ หรือช่องทางแจ้งเตือนได้ด้วยการเพิ่ม class ใหม่ |
| L — Liskov Substitution | policy, handler, state และ sender ทุกตัวใช้แทนกันได้ผ่าน interface เดียวกัน |
| I — Interface Segregation | โมดูลห้องกับโมดูลการจองคุยกันผ่าน interface เล็ก ๆ เฉพาะที่ต้องใช้ |
| D — Dependency Inversion | service พึ่ง interface และ abstraction (`Clock`, `ApplicationEventPublisher`) ทั้งหมด inject ผ่าน constructor |

---

## S — Single Responsibility Principle

> class หนึ่งควรมีเหตุผลในการเปลี่ยนเพียงเรื่องเดียว

**ตัวอย่างที่ทำตาม**

| Class | หน้าที่เดียว | เปลี่ยนเมื่อ |
|---|---|---|
| `BookingController` | แปลง HTTP request/response | รูปแบบ API เปลี่ยน |
| `BookingServiceImpl` | ขั้นตอนสร้าง แก้ ลบ การจอง และ transaction | ขั้นตอนธุรกิจของการจองเปลี่ยน |
| `TimeRangeHandler` | ตรวจเรื่องเวลาเท่านั้น | เวลาเปิดอาคารหรือกฎเวลาเปลี่ยน |
| `ConflictHandler` | ตรวจเวลาชนเท่านั้น | นิยามเวลาชนเปลี่ยน |
| `BookingMapper` | แปลง entity กับ DTO | field ของ DTO เปลี่ยน |
| `GlobalExceptionHandler` | แปลง exception เป็น `ErrorResponse` | รูปแบบ error เปลี่ยน |
| `JwtService` | สร้างและตรวจ token | วิธีเข้ารหัส token เปลี่ยน |
| `BookingStatusScheduler` | ตั้งเวลาเรียกงาน (ไม่มี logic เอง เรียก `BookingLifecycleService`) | ความถี่ของ scheduler เปลี่ยน |

ตัวอย่างเด่นคือการตรวจคำขอจอง แทนที่จะเป็น method ยาวเดียว แต่ละกฎแยกเป็น handler ของตัวเอง (Chain of Responsibility) และ test แยกกันได้ 5 ไฟล์

ระดับโมดูลก็แยกตามความรับผิดชอบเช่นกัน: การจอง (คนที่ 3) กับวงจรสถานะ (คนที่ 4) แยก service และ repository ออกจากกัน (`BookingService` กับ `BookingLifecycleService`, `BookingRepository` กับ `BookingLifecycleRepository`) ทั้งที่ทำงานกับตาราง `bookings` เดียวกัน

**จุดที่ยังปรับได้:** `BookingLifecycleServiceImpl` รวมการตรวจสิทธิ์ (`isPermitted`) กฎเวลา (`timeViolation`) การตรวจห้อง (`ensureRoomUsable`) และงานของ Scheduler ไว้ใน class เดียว (ประมาณ 220 บรรทัด) ถ้าระบบโตขึ้น ควรแยกกฎสิทธิ์และกฎเวลาออกเป็น policy object แบบเดียวกับฝั่งสร้างการจอง

---

## O — Open/Closed Principle

> เปิดให้ขยาย ปิดไม่ให้ต้องแก้ของเดิม

| ต้องการเพิ่ม | ทำอย่างไร | ไม่ต้องแก้ |
|---|---|---|
| บทบาทใหม่ เช่น ผู้ช่วยสอน | เพิ่ม `class TaBookingPolicy implements BookingPolicy` + `@Component` | `BookingPolicyResolver`, `PolicyHandler`, `BookingServiceImpl` |
| กฎการจองใหม่ เช่น ห้ามจองวันหยุด | เพิ่ม `class HolidayHandler extends BookingValidationHandler` แล้วต่อเข้า chain | handler เดิมทั้ง 5 ตัว |
| ช่องทางแจ้งเตือน เช่น อีเมล | เพิ่ม `class EmailNotificationSender implements NotificationSender` | `NotificationService`, `NotificationListener` |
| ผู้รับ event ใหม่ เช่น บันทึก audit log | เพิ่ม `@TransactionalEventListener` ที่รับ `BookingStatusChangedEvent` | โมดูลการจองที่ publish event |
| เงื่อนไขค้นหาห้องใหม่ | เพิ่ม method ใน `RoomSpecifications` แล้วประกอบใน `RoomServiceImpl.search` | specification เดิม |

ตัวอย่าง: `BookingPolicyResolver` ไม่รู้จัก class policy ใด ๆ เลย รับ `List<BookingPolicy>` จาก Spring แล้วสร้าง map จาก `supportedRoles()` ของแต่ละตัว เพิ่ม policy ใหม่จึงไม่ต้องแตะ resolver

**จุดที่ยังปรับได้:** เพิ่ม `BookingAction` ใหม่ยังต้องแก้ enum และ `switch` ใน `isPermitted` กับ `timeViolation` ของ `BookingLifecycleServiceImpl` (แต่ compiler ช่วยเตือนว่าลืมกรณีไหน เพราะใช้ switch expression ที่ต้องครบทุกค่า)

---

## L — Liskov Substitution Principle

> ใช้ subclass แทน superclass ได้โดยโปรแกรมยังถูกต้อง

- **`BookingPolicy`**: `PolicyHandler` เรียก `maxDurationHours()`, `maxAdvanceDays()`, `maxActiveBookings()` โดยไม่สนว่าเป็น policy ของบทบาทไหน ทุก implementation คืนค่าตามสัญญาเดียวกัน (`StaffBookingPolicy` คืน `Integer.MAX_VALUE` แทน "ไม่จำกัด" แทนที่จะคืนค่าพิเศษที่ผู้เรียกต้องเช็ก)
- **`BookingValidationHandler`**: chain เรียก `validate()` ของ handler ทุกตัวแบบเดียวกัน ทุก subclass ทำแค่ `check()` และไม่เปลี่ยนพฤติกรรมการส่งต่อ ลำดับ handler สลับได้โดยไม่พัง (ต่างกันแค่ประสิทธิภาพ)
- **`BookingState`**: state ทุกตัวสืบทอด `AbstractBookingState` และตอบคำถาม `canHandle` / `next` แบบเดียวกัน state สุดท้าย (`RejectedState`, `CancelledState`, `CompletedState`, `NoShowState`) คืน `false` / `Optional.empty()` ไม่โยน exception ที่ผู้เรียกไม่คาดคิด
- **`NotificationSender`**: `NotificationServiceImpl` วน `senders.forEach(sender -> sender.send(message))` ใช้ sender ชนิดไหนก็ได้
- **test ยืนยัน**: `BookingPolicyTest` ตรวจทุก policy ผ่าน interface เดียวกัน, `BookingStateTransitionTest` ตรวจทุก state ด้วยตารางเดียว 42 กรณี

---

## I — Interface Segregation Principle

> client ไม่ควรถูกบังคับให้พึ่ง method ที่ไม่ได้ใช้

ตัวอย่างหลักคือการคุยกันข้ามโมดูลระหว่างห้องกับการจอง:

| Interface | เจ้าของ | method | ผู้ใช้ |
|---|---|---|---|
| `RoomQueryService` | คนที่ 2 (ห้อง) | `getActiveRoom`, `isClosed`, `existsById` | `RoomHandler`, `ClosureHandler`, `BookingServiceImpl`, `BookingLifecycleServiceImpl` |
| `BookingQueryService` | คนที่ 3 (การจอง) | `findBookedRoomIds`, `hasFutureBookings`, `hasActiveBookingOverlap` | `RoomServiceImpl`, `RoomClosureServiceImpl` |

โมดูลการจองไม่ต้องพึ่ง `RoomService` ที่มี 7 method (CRUD ห้อง ค้นหา ห้องว่าง และอุปกรณ์) ซึ่งไม่ได้ใช้ และโมดูลห้องไม่ต้องรู้จัก `BookingRepository` ผลพลอยได้คือไม่มี dependency วน (`RoomService ↔ BookingService`) และแต่ละคนแก้ implementation ของตัวเองได้โดยไม่กระทบอีกฝ่าย ตราบใดที่ interface ไม่เปลี่ยน

ตัวอย่างอื่น:
- `BookingLifecycleRepository` แยกจาก `BookingRepository` มีเฉพาะ query ที่งานวงจรสถานะใช้ (หาการจองที่เลยเวลา, ล็อก, อ่านสถานะ)
- `BookingState` มีแค่ 3 method ที่ service ต้องใช้
- `NotificationSender` มี method เดียว `send()`

**จุดที่ยังปรับได้:** `BookingRepository` มี query หลายเรื่อง (ตรวจเวลาชน, ตาราง, ล็อก, ค้นหา) เพราะเป็น repository ของ Spring Data ที่ผูกกับ entity เดียว ถ้าใหญ่ขึ้นอีกอาจแยกเป็น repository ย่อยตามงาน แบบที่ทำกับ `BookingLifecycleRepository`

---

## D — Dependency Inversion Principle

> module ระดับสูงไม่ควรพึ่ง module ระดับต่ำโดยตรง ทั้งคู่ควรพึ่ง abstraction

- **Controller พึ่ง service interface**: `BookingController` รู้จัก `BookingService` ไม่รู้จัก `BookingServiceImpl`
- **Service พึ่ง interface ของโมดูลอื่น**: `RoomHandler` พึ่ง `RoomQueryService` ไม่ใช่ `RoomQueryServiceImpl` หรือ `RoomRepository`
- **Repository เป็น interface**: Spring Data สร้าง implementation ให้ตอน runtime service ไม่ผูกกับ Hibernate โดยตรง
- **เวลาเป็น abstraction**: ทุก class ที่ใช้เวลาปัจจุบัน inject `java.time.Clock` (จาก `ClockConfig`) แทนการเรียก `LocalDateTime.now()` ตรง ๆ test จึงใช้ `Clock.fixed(...)` ทดสอบกฎเวลาได้แน่นอน เช่น check-in ±15 นาที
- **event เป็น abstraction**: `BookingServiceImpl` พึ่ง `ApplicationEventPublisher` ไม่รู้จัก `NotificationListener`
- **Constructor injection ทั้งหมด**: field เป็น `private final` + `@RequiredArgsConstructor` ทำให้ dependency ชัดและ unit test สร้าง object ด้วย mock ได้เลย เช่น `new BookingLifecycleServiceImpl(bookingRepository, lifecycleRepository, ..., clock)`

**จุดที่ยังปรับได้:** `InAppNotificationSender` (โมดูลแจ้งเตือน) ใช้ `BookingRepository.getReferenceById` ของโมดูลการจองโดยตรงเพื่อผูก `notification.booking` เป็นการพึ่งข้ามโมดูลจุดเดียวที่เหลือ ยอมรับได้เพราะใช้แค่สร้าง reference ไม่ query ข้อมูล แต่ถ้าจะให้สะอาดขึ้นควรผ่าน `BookingQueryService`

---

## สรุป

| หลัก | ระดับการใช้ | หลักฐานในโค้ด |
|---|---|---|
| SRP | สูง | handler แยกตามกฎ, mapper, service แยกตามโมดูล |
| OCP | สูง | Strategy, Chain, State, Observer + `NotificationSender` |
| LSP | สูง | ทุก interface ใช้แทนกันได้ มี test ครอบทุก implementation |
| ISP | สูง | `RoomQueryService`, `BookingQueryService`, `BookingLifecycleRepository` |
| DIP | สูง | interface ทุก layer, `Clock`, `ApplicationEventPublisher`, constructor injection |

จุดที่ยังปรับได้ทั้ง 4 จุดข้างต้นไม่กระทบความถูกต้องของระบบ เป็นแนวทางปรับปรุงเมื่อระบบขยาย
