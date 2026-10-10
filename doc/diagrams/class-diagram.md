# Class Diagram (Design Class Diagram)

Class Diagram ระดับออกแบบ ใช้ชื่อ class, field และ method ตามโค้ดจริงใน `code/backend/src/main/java/com/example/cp_room_booking/` แบ่งเป็น 6 ภาพตามโมดูล เพื่อไม่ให้ภาพเดียวแน่นเกินไป

| ภาพ | เนื้อหา | ผู้รับผิดชอบโมดูล |
|---|---|---|
| 1 | Domain Entity และ Enum | ทุกคน |
| 2 | Layered Architecture ของโมดูลการจอง | คนที่ 3 |
| 3 | Chain of Responsibility + Strategy (ตรวจกฎการจอง) | คนที่ 3, คนที่ 1 |
| 4 | State Pattern (วงจรสถานะการจอง) | คนที่ 4 |
| 5 | Observer Pattern (แจ้งเตือน) | คนที่ 5 |
| 6 | Interface ระหว่างโมดูลห้องกับโมดูลการจอง (ISP) | คนที่ 2, คนที่ 3 |

## 1. Domain Entity และ Enum

```mermaid
classDiagram
    direction TB

    class BaseEntity {
        <<abstract>>
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
    }
    class User {
        -Long id
        -String email
        -String passwordHash
        -Role role
        -UserStatus status
        -UserProfile profile
        +attachProfile(UserProfile) void
        +isActive() boolean
    }
    class UserProfile {
        -Long id
        -User user
        -String fullName
        -String studentCode
        -String phone
        -String department
    }
    class Room {
        -Long id
        -String code
        -String name
        -Integer floor
        -Integer capacity
        -String description
        -RoomType roomType
        -RoomStatus status
        -Set~RoomEquipment~ equipment
        +syncEquipment(Map~Equipment,Integer~) void
        +isActive() boolean
    }
    class RoomType {
        -Long id
        -String name
        -String description
    }
    class Equipment {
        -Long id
        -String name
        -String description
    }
    class RoomEquipment {
        -RoomEquipmentId id
        -Room room
        -Equipment equipment
        -int quantity
    }
    class RoomEquipmentId {
        -Long roomId
        -Long equipmentId
    }
    class RoomClosure {
        -Long id
        -Room room
        -LocalDateTime startTime
        -LocalDateTime endTime
        -String reason
        -LocalDateTime createdAt
    }
    class Booking {
        -Long id
        -User user
        -Room room
        -LocalDateTime startTime
        -LocalDateTime endTime
        -String purpose
        -Integer attendees
        -BookingStatus status
        -List~BookingStatusHistory~ history
        +changeStatus(BookingStatus, User, String, LocalDateTime) void
        +isOwnedBy(Long) boolean
    }
    class BookingStatusHistory {
        -Long id
        -Booking booking
        -BookingStatus fromStatus
        -BookingStatus toStatus
        -User changedBy
        -String note
        -LocalDateTime changedAt
    }
    class Notification {
        -Long id
        -User user
        -Booking booking
        -NotificationType type
        -String message
        -boolean read
        -LocalDateTime createdAt
        +belongsTo(Long) boolean
    }

    class Role {
        <<enumeration>>
        STUDENT
        LECTURER
        STAFF
        ADMIN
        +isStaff() boolean
    }
    class BookingStatus {
        <<enumeration>>
        PENDING
        APPROVED
        REJECTED
        CANCELLED
        CHECKED_IN
        COMPLETED
        NO_SHOW
        +ACTIVE$ Set~BookingStatus~
    }
    class RoomStatus {
        <<enumeration>>
        ACTIVE
        MAINTENANCE
        INACTIVE
    }
    class NotificationType {
        <<enumeration>>
        BOOKING_CREATED
        BOOKING_APPROVED
        BOOKING_REJECTED
        BOOKING_CANCELLED
        BOOKING_NO_SHOW
    }

    BaseEntity <|-- User
    BaseEntity <|-- Room
    BaseEntity <|-- Booking
    User "1" *-- "1" UserProfile : profile
    User --> Role
    Room "0..*" --> "1" RoomType : roomType
    Room "1" *-- "0..*" RoomEquipment : equipment
    RoomEquipment "0..*" --> "1" Equipment
    RoomEquipment *-- RoomEquipmentId : EmbeddedId
    Room --> RoomStatus
    RoomClosure "0..*" --> "1" Room
    Booking "0..*" --> "1" User : user
    Booking "0..*" --> "1" Room : room
    Booking "1" *-- "1..*" BookingStatusHistory : history
    Booking --> BookingStatus
    BookingStatusHistory --> "0..1" User : changedBy
    Notification "0..*" --> "1" User
    Notification "0..*" --> "0..1" Booking
    Notification --> NotificationType
```

จุดออกแบบ:
- `Booking.changeStatus(...)` เปลี่ยนสถานะพร้อมเพิ่ม `BookingStatusHistory` ในตัว entity เอง ประวัติจึงไม่หลุดจากการเปลี่ยนสถานะ
- `RoomEquipment` เป็น entity แยก (ไม่ใช้ `@ManyToMany` ตรง ๆ) เพราะต้องเก็บ `quantity` ใช้ composite key `RoomEquipmentId` กับ `@MapsId`
- ทุกความสัมพันธ์ `@ManyToOne` ใช้ `FetchType.LAZY` และดึงข้อมูลที่ต้องใช้ผ่าน `@EntityGraph` หรือ `join fetch` ใน repository

## 2. Layered Architecture ของโมดูลการจอง

```mermaid
classDiagram
    direction LR

    class BookingController {
        +create(...) BookingResponse
        +findAll(...) PageResponse~BookingResponse~
        +findById(...) BookingResponse
        +update(...) BookingResponse
        +delete(...) void
        +findByUser(...) PageResponse~BookingResponse~
        +findRoomSchedule(...) List~BookingSlotResponse~
    }
    class BookingService {
        <<interface>>
        +create(UserPrincipal, BookingRequest) BookingResponse
        +getById(Long, UserPrincipal) BookingResponse
        +update(Long, UserPrincipal, BookingRequest) BookingResponse
        +delete(Long, UserPrincipal) void
        +findByUser(Long, UserPrincipal, Pageable) PageResponse
        +findRoomSchedule(Long, LocalDate) List~BookingSlotResponse~
    }
    class BookingServiceImpl {
        -BookingRepository bookingRepository
        -UserRepository userRepository
        -RoomQueryService roomQueryService
        -BookingValidationChain validationChain
        -BookingMapper bookingMapper
        -ApplicationEventPublisher eventPublisher
        -Clock clock
        -lockUserAndRoom(Long, Long) void
        -findEditable(Long, UserPrincipal) Booking
    }
    class BookingRepository {
        <<interface>>
        +existsOverlap(roomId, start, end, statuses, excludeId) boolean
        +findBookedRoomIds(start, end, statuses) Set~Long~
        +countByUserIdAndStatusInAndEndTimeAfter(...) long
        +findDetailById(Long) Optional~Booking~
        +findRoomSchedule(...) List~Booking~
        +findAllInRange(from, to, statuses) List~Booking~
        +lockUser(Long) Optional~Long~
        +lockRoom(Long) Optional~Long~
        +lockBooking(Long) Optional~Long~
    }
    class BookingMapper {
        +toEntity(BookingRequest, User, Room) Booking
        +updateEntity(Booking, BookingRequest, Room) void
        +toResponse(Booking) BookingResponse
        +toSlot(Booking) BookingSlotResponse
    }
    class BookingRequest {
        <<record>>
        Long roomId
        LocalDateTime startTime
        LocalDateTime endTime
        String purpose
        Integer attendees
    }
    class BookingResponse {
        <<record>>
        Long id
        Long roomId
        String roomCode
        String roomName
        Long userId
        String userEmail
        String userFullName
        LocalDateTime startTime
        LocalDateTime endTime
        String purpose
        Integer attendees
        BookingStatus status
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }
    class JpaRepository~T,ID~ {
        <<interface>>
    }

    BookingController --> BookingService : ใช้ผ่าน interface
    BookingService <|.. BookingServiceImpl
    BookingServiceImpl --> BookingRepository
    BookingServiceImpl --> BookingMapper
    BookingServiceImpl --> BookingValidationChain
    BookingServiceImpl --> RoomQueryService
    JpaRepository <|-- BookingRepository
    BookingController ..> BookingRequest
    BookingController ..> BookingResponse
    BookingMapper ..> Booking
```

Controller รับและคืนเฉพาะ DTO (`record`) ไม่ส่ง entity ออกนอก service ส่วน service แปลงด้วย mapper ทุกครั้ง โมดูลอื่น (ห้อง ผู้ใช้ แจ้งเตือน สถิติ) ใช้โครงสร้างเดียวกัน: `XController → XService (interface) → XServiceImpl → XRepository`

## 3. Chain of Responsibility + Strategy (ตรวจกฎการจอง)

```mermaid
classDiagram
    direction LR

    class BookingValidationChain {
        -BookingValidationHandler head
        +validate(BookingValidationContext) void
    }
    class BookingValidationHandler {
        <<abstract>>
        -BookingValidationHandler next
        +setNext(BookingValidationHandler) BookingValidationHandler
        +validate(BookingValidationContext) void
        #check(BookingValidationContext)* void
    }
    class TimeRangeHandler {
        -Clock clock
        -LocalTime buildingOpen
        -LocalTime buildingClose
        #check(context) void
    }
    class PolicyHandler {
        -BookingPolicyResolver policyResolver
        -BookingRepository bookingRepository
        #check(context) void
    }
    class RoomHandler {
        -RoomQueryService roomQueryService
        #check(context) void
    }
    class ClosureHandler {
        -RoomQueryService roomQueryService
        #check(context) void
    }
    class ConflictHandler {
        -BookingRepository bookingRepository
        #check(context) void
    }
    class BookingValidationContext {
        -Long userId
        -Role role
        -Long roomId
        -LocalDateTime startTime
        -LocalDateTime endTime
        -int attendees
        -Long excludeBookingId
        -BookingPolicy policy
        -Room room
        +isUpdate() boolean
        +duration() Duration
    }

    class BookingPolicy {
        <<interface>>
        +supportedRoles() Set~Role~
        +maxDurationHours() int
        +maxAdvanceDays() int
        +maxActiveBookings() int
        +requiresApproval() boolean
    }
    class StudentBookingPolicy {
        2 ชม. / 7 วัน / 2 รายการ / รออนุมัติ
    }
    class LecturerBookingPolicy {
        4 ชม. / 30 วัน / 5 รายการ / อนุมัติอัตโนมัติ
    }
    class StaffBookingPolicy {
        8 ชม. / 90 วัน / ไม่จำกัด / อนุมัติอัตโนมัติ
    }
    class BookingPolicyResolver {
        -Map~Role,BookingPolicy~ policies
        +resolve(Role) BookingPolicy
    }

    BookingValidationChain --> BookingValidationHandler : head
    BookingValidationHandler --> BookingValidationHandler : next
    BookingValidationHandler <|-- TimeRangeHandler
    BookingValidationHandler <|-- PolicyHandler
    BookingValidationHandler <|-- RoomHandler
    BookingValidationHandler <|-- ClosureHandler
    BookingValidationHandler <|-- ConflictHandler
    BookingValidationHandler ..> BookingValidationContext
    PolicyHandler --> BookingPolicyResolver
    BookingPolicyResolver o-- BookingPolicy
    BookingPolicy <|.. StudentBookingPolicy
    BookingPolicy <|.. LecturerBookingPolicy
    BookingPolicy <|.. StaffBookingPolicy
```

ลำดับใน chain: `TimeRangeHandler → PolicyHandler → RoomHandler → ClosureHandler → ConflictHandler` ตรวจกฎที่ไม่ต้องใช้ฐานข้อมูลก่อน แล้วค่อย query `StaffBookingPolicy` รองรับทั้ง `STAFF` และ `ADMIN`

## 4. State Pattern (วงจรสถานะการจอง)

```mermaid
classDiagram
    direction LR

    class BookingLifecycleService {
        <<interface>>
        +changeStatus(Long, BookingAction, String, UserPrincipal) BookingResponse
        +getHistory(Long, UserPrincipal) List~BookingHistoryResponse~
        +getAllowedActions(Long, UserPrincipal) AllowedActionsResponse
        +markNoShows() int
        +completeFinished() int
    }
    class BookingLifecycleServiceImpl {
        -BookingRepository bookingRepository
        -BookingLifecycleRepository lifecycleRepository
        -RoomQueryService roomQueryService
        -BookingStateFactory stateFactory
        -ApplicationEventPublisher eventPublisher
        -Clock clock
        -isPermitted(BookingAction, Booking, UserPrincipal) boolean
        -timeViolation(BookingAction, Booking, LocalDateTime) Optional~String~
        -ensureRoomUsable(Booking) void
        -lockAndStillIn(Booking, BookingStatus) boolean
        -transition(...) void
    }
    class BookingLifecycleRepository {
        <<interface>>
        +findByStatusAndStartTimeBefore(status, threshold) List~Booking~
        +findByStatusAndEndTimeBefore(status, threshold) List~Booking~
        +lockById(Long) Optional~Long~
        +findStatusById(Long) Optional~BookingStatus~
    }
    class BookingStateFactory {
        -Map~BookingStatus,BookingState~ states
        +of(BookingStatus) BookingState
    }
    class BookingState {
        <<interface>>
        +status() BookingStatus
        +canHandle(BookingAction) boolean
        +next(BookingAction) Optional~BookingStatus~
    }
    class AbstractBookingState {
        <<abstract>>
        -BookingStatus status
        -Map~BookingAction,BookingStatus~ transitions
    }
    class PendingState
    class ApprovedState
    class CheckedInState
    class RejectedState
    class CancelledState
    class CompletedState
    class NoShowState
    class BookingStatusScheduler {
        +closeOverdueBookings() void
    }
    class BookingAction {
        <<enumeration>>
        APPROVE
        REJECT
        CANCEL
        CHECK_IN
        COMPLETE
        MARK_NO_SHOW
    }

    BookingLifecycleService <|.. BookingLifecycleServiceImpl
    BookingLifecycleServiceImpl --> BookingStateFactory
    BookingLifecycleServiceImpl --> BookingLifecycleRepository
    BookingStateFactory o-- BookingState
    BookingState <|.. AbstractBookingState
    AbstractBookingState <|-- PendingState
    AbstractBookingState <|-- ApprovedState
    AbstractBookingState <|-- CheckedInState
    AbstractBookingState <|-- RejectedState
    AbstractBookingState <|-- CancelledState
    AbstractBookingState <|-- CompletedState
    AbstractBookingState <|-- NoShowState
    BookingState ..> BookingAction
    BookingStatusScheduler --> BookingLifecycleService : ทุก 1 นาที
```

การเปลี่ยนสถานะที่อนุญาตของแต่ละ state ดูใน [state-diagram.md](state-diagram.md)

## 5. Observer Pattern (แจ้งเตือน)

```mermaid
classDiagram
    direction LR

    class ApplicationEventPublisher {
        <<interface>>
        +publishEvent(Object) void
    }
    class BookingCreatedEvent {
        <<record>>
        Long bookingId
        Long userId
        String roomCode
        LocalDateTime startTime
        LocalDateTime endTime
        BookingStatus status
    }
    class BookingStatusChangedEvent {
        <<record>>
        Long bookingId
        Long userId
        BookingStatus fromStatus
        BookingStatus toStatus
        String note
        String roomCode
        LocalDateTime startTime
    }
    class NotificationListener {
        -NotificationService notificationService
        +onBookingCreated(BookingCreatedEvent) void
        +onStatusChanged(BookingStatusChangedEvent) void
    }
    class NotificationService {
        <<interface>>
        +notifyUser(Long, Long, NotificationType, String) void
        +notifyStaff(Long, NotificationType, String) void
        +findMine(Long, Pageable) PageResponse
        +countUnread(Long) long
        +markRead(Long, UserPrincipal) NotificationResponse
        +markAllRead(Long) int
        +delete(Long, UserPrincipal) void
    }
    class NotificationServiceImpl {
        -NotificationRepository notificationRepository
        -UserRepository userRepository
        -List~NotificationSender~ senders
        -NotificationMapper notificationMapper
    }
    class NotificationSender {
        <<interface>>
        +send(NotificationMessage) void
    }
    class InAppNotificationSender {
        -NotificationRepository notificationRepository
        +send(NotificationMessage) void
    }
    class NotificationMessage {
        <<record>>
        Long recipientId
        Long bookingId
        NotificationType type
        String message
    }

    BookingServiceImpl ..> BookingCreatedEvent : publish
    BookingLifecycleServiceImpl ..> BookingStatusChangedEvent : publish
    BookingServiceImpl --> ApplicationEventPublisher
    BookingLifecycleServiceImpl --> ApplicationEventPublisher
    NotificationListener ..> BookingCreatedEvent : AFTER_COMMIT
    NotificationListener ..> BookingStatusChangedEvent : AFTER_COMMIT
    NotificationListener --> NotificationService
    NotificationService <|.. NotificationServiceImpl
    NotificationServiceImpl o-- NotificationSender : senders
    NotificationSender <|.. InAppNotificationSender
    NotificationSender ..> NotificationMessage
```

โมดูลการจองไม่รู้จักโมดูลแจ้งเตือนเลย รู้แค่ event ส่วน `NotificationSender` เปิดให้เพิ่มช่องทางใหม่ (อีเมล, LINE) ได้โดยไม่แก้ `NotificationService`

## 6. Interface ระหว่างโมดูลห้องกับโมดูลการจอง

```mermaid
classDiagram
    direction LR

    class RoomQueryService {
        <<interface>>
        +getActiveRoom(Long) Room
        +isClosed(Long, LocalDateTime, LocalDateTime) boolean
        +existsById(Long) boolean
    }
    class RoomQueryServiceImpl {
        -RoomRepository roomRepository
        -RoomClosureRepository roomClosureRepository
    }
    class BookingQueryService {
        <<interface>>
        +findBookedRoomIds(LocalDateTime, LocalDateTime) Set~Long~
        +hasFutureBookings(Long) boolean
        +hasActiveBookingOverlap(Long, LocalDateTime, LocalDateTime) boolean
    }
    class BookingQueryServiceImpl {
        -BookingRepository bookingRepository
        -Clock clock
    }
    class RoomServiceImpl {
        +search(RoomSearchRequest, Pageable) PageResponse
        +findAvailable(start, end, minCapacity, equipmentIds) List~RoomResponse~
        +delete(Long) void
    }
    class RoomClosureServiceImpl {
        +create(Long, RoomClosureRequest) RoomClosureResponse
    }
    class RoomHandler
    class ClosureHandler
    class BookingLifecycleServiceImpl

    RoomQueryService <|.. RoomQueryServiceImpl
    BookingQueryService <|.. BookingQueryServiceImpl
    RoomHandler --> RoomQueryService : โมดูลการจองใช้
    ClosureHandler --> RoomQueryService
    BookingLifecycleServiceImpl --> RoomQueryService
    RoomServiceImpl --> BookingQueryService : โมดูลห้องใช้
    RoomClosureServiceImpl --> BookingQueryService
```

แต่ละโมดูลเห็นอีกโมดูลผ่าน interface เล็ก ๆ ที่มีเฉพาะ method ที่ต้องใช้ (Interface Segregation) ไม่ต้องรู้จัก repository ของกันและกัน และไม่เกิด dependency วนระหว่าง `RoomService` กับ `BookingService`
