# บทที่ 2 ทฤษฎีและเทคโนโลยีที่เกี่ยวข้อง

## 2.1 สถาปัตยกรรมแบบชั้น (Layered Architecture) และ MVC

สถาปัตยกรรมแบบชั้นแบ่งโปรแกรมเป็นชั้นตามหน้าที่ แต่ละชั้นเรียกได้เฉพาะชั้นที่อยู่ถัดลงไป ทำให้แก้ไขชั้นหนึ่งโดยไม่กระทบชั้นอื่น backend ของระบบแบ่งเป็น

| ชั้น | หน้าที่ | ในโปรเจกต์ |
|---|---|---|
| Presentation | รับ HTTP request ตรวจรูปแบบข้อมูล คืน response | `controller/api`, DTO (`dto/request`, `dto/response`) |
| Service (Business Logic) | กฎทางธุรกิจ transaction ประสานงานระหว่างโมดูล | `service`, `service/impl` และ pattern ใน `service/*` |
| Repository (Data Access) | อ่าน/เขียนฐานข้อมูล | `repository` (Spring Data JPA) |
| Domain | entity และ enum ของโดเมน | `domain/entity`, `domain/enums` |

MVC (Model–View–Controller) แยกข้อมูล การแสดงผล และการควบคุมออกจากกัน ฝั่ง backend ใช้ Spring MVC (`@RestController` คืน JSON แทน View) ฝั่ง frontend ใช้ Next.js ที่แยก page (View), component และ API client (`lib/`)

## 2.2 RESTful API

REST เป็นรูปแบบการออกแบบ API ที่มอง resource เป็น URL และใช้ HTTP method สื่อความหมาย (GET อ่าน, POST สร้าง, PUT แก้ทั้งก้อน, PATCH แก้บางส่วน, DELETE ลบ) และใช้ HTTP status บอกผล

| Status | ความหมายในระบบ |
|---|---|
| 200 / 201 / 204 | สำเร็จ / สร้างสำเร็จ / ลบสำเร็จ |
| 400 Bad Request | ข้อมูลผิดรูปแบบหรือผิดกฎธุรกิจ เช่น จองเกินเวลา |
| 401 Unauthorized | ไม่ได้ login, token ไม่ถูกต้อง หรือรหัสผ่านผิด |
| 403 Forbidden | login แล้วแต่ไม่มีสิทธิ์ |
| 404 Not Found | ไม่พบข้อมูล |
| 409 Conflict | ขัดแย้งกับข้อมูลปัจจุบัน เช่น เวลาชน สถานะเปลี่ยนไปแล้ว |
| 500 | ข้อผิดพลาดภายใน (ไม่ส่งรายละเอียดภายในให้ผู้ใช้) |

API ทั้งหมดอยู่ใต้ `/api/v1` และคืน error ในรูปแบบเดียวกัน (`timestamp`, `status`, `error`, `message`, `path`, `fieldErrors`)

## 2.3 การยืนยันตัวตนด้วย JWT และการเก็บรหัสผ่านด้วย BCrypt

**JWT (JSON Web Token)** เป็น token ที่ server ลงลายเซ็นด้วย secret key (HMAC-SHA) ภายในมีข้อมูล (claims) เช่น อีเมล รหัสผู้ใช้ บทบาท และเวลาหมดอายุ client แนบ token ใน header `Authorization: Bearer <token>` ทุก request server ตรวจลายเซ็นโดยไม่ต้องเก็บ session (stateless) จึงขยายระบบได้ง่าย ในระบบ token หมดอายุใน 480 นาที และถ้าเปลี่ยน secret key token เก่าทั้งหมดใช้ไม่ได้ทันที

**BCrypt** เป็นฟังก์ชัน hash รหัสผ่านที่มี salt และปรับความช้าได้ (cost factor) ป้องกันการเดารหัสด้วยตาราง rainbow table ข้อจำกัดคือรับข้อมูลได้ไม่เกิน 72 ไบต์ ระบบจึงตรวจความยาวรหัสผ่านเป็นจำนวนไบต์ UTF-8 (อักษรไทย 1 ตัว = 3 ไบต์)

## 2.4 Spring Boot, Spring Data JPA และ Flyway

- **Spring Boot** ช่วยตั้งค่า Spring อัตโนมัติ มี web server ฝังในตัว รันเป็น jar เดียว ใช้ Dependency Injection สร้างและเชื่อม object ให้
- **Spring Security** จัดการการยืนยันตัวตนและสิทธิ์ ระบบใช้ filter ของตัวเองตรวจ JWT และ `@PreAuthorize` กำหนดสิทธิ์ราย endpoint
- **Spring Data JPA / Hibernate** แปลง object เป็นตาราง (ORM) และสร้าง implementation ของ repository จากชื่อ method หรือ `@Query` รองรับ Specification สำหรับเงื่อนไขค้นหาแบบประกอบ
- **Flyway** จัดการเวอร์ชันของโครงสร้างฐานข้อมูลด้วยไฟล์ `V1__...sql`, `V2__...sql` รันตามลำดับครั้งเดียวและตรวจ checksum ป้องกันการแก้ไฟล์ที่รันไปแล้ว
- **Bean Validation** ตรวจข้อมูลที่เข้ามาด้วย annotation เช่น `@NotBlank`, `@Email`, `@Size` และ annotation ที่ทีมเขียนเอง (`@MaxUtf8Bytes`)

## 2.5 Next.js, React และ TypeScript

- **React** สร้าง UI จาก component และ state เมื่อ state เปลี่ยน React วาดหน้าใหม่ให้ การใส่ `key` ให้ component บอก React ว่าเมื่อ key เปลี่ยนต้องสร้าง component ใหม่ (ใช้แก้ปัญหาฟอร์มค้างข้อมูลเก่า)
- **Next.js** เป็น framework ของ React ที่มี routing ตามโครงสร้างโฟลเดอร์ (`app/`) และ build สำหรับ production
- **TypeScript** เพิ่มชนิดข้อมูลให้ JavaScript ตรวจความผิดพลาดก่อนรัน type ใน `lib/` ตรงกับ DTO ของ backend
- **Tailwind CSS** เขียน style ด้วย utility class ทำ responsive รองรับมือถือ

## 2.6 Design Patterns

Design Pattern คือรูปแบบการแก้ปัญหาการออกแบบที่เกิดซ้ำ ระบบใช้ GoF Behavioral Pattern 4 ตัว

| Pattern | แนวคิด | ปัญหาในระบบ |
|---|---|---|
| Strategy | แยก algorithm ที่สลับกันได้เป็น class ที่ implement interface เดียวกัน เลือกใช้ตอน runtime | กฎการจองต่างกันตามบทบาท |
| Chain of Responsibility | ส่ง request ผ่าน handler ต่อกันเป็นสาย แต่ละตัวจัดการส่วนของตัวเองแล้วส่งต่อ | ตรวจคำขอจองหลายกฎตามลำดับ |
| State | object เปลี่ยนพฤติกรรมตามสถานะภายใน โดยให้แต่ละสถานะเป็น class | การจองทำ action ได้ต่างกันในแต่ละสถานะ |
| Observer | subject แจ้ง observer เมื่อเกิดเหตุการณ์ โดยไม่รู้ว่า observer เป็นใคร | แจ้งเตือนเมื่อการจองเปลี่ยน โดยไม่ผูกโมดูล |

นอกจากนี้ใช้ Enterprise Pattern ได้แก่ Repository, Service Layer, DTO + Mapper, Specification, Dependency Injection และ Template Method รายละเอียดการใช้งานจริงอยู่ใน [design-patterns.md](../design-patterns.md)

## 2.7 หลัก SOLID

| หลัก | ใจความ |
|---|---|
| Single Responsibility | class หนึ่งมีเหตุผลในการเปลี่ยนเพียงเรื่องเดียว |
| Open/Closed | เปิดให้ขยาย ปิดไม่ต้องแก้ของเดิม |
| Liskov Substitution | ใช้ subclass แทน superclass ได้โดยโปรแกรมยังถูกต้อง |
| Interface Segregation | ไม่บังคับ client ให้พึ่ง method ที่ไม่ได้ใช้ ใช้ interface เล็กเฉพาะเรื่อง |
| Dependency Inversion | พึ่ง abstraction ไม่พึ่ง implementation โดยตรง |

การวิเคราะห์จากโค้ดจริงอยู่ใน [solid-analysis.md](../solid-analysis.md)

## 2.8 Transaction และการควบคุมการทำงานพร้อมกัน

Transaction รวมหลายคำสั่งให้สำเร็จหรือยกเลิกทั้งหมด (ACID) แต่ transaction เพียงอย่างเดียวไม่กันปัญหา **check-then-act** คือ 2 transaction อ่านข้อมูลพร้อมกันว่า "ยังไม่ชน" แล้วต่างคนต่างบันทึก ทำให้จองซ้อนกันได้

วิธีป้องกันมี 2 แบบ

| วิธี | หลักการ | ข้อดี / ข้อเสีย |
|---|---|---|
| Pessimistic Locking | ล็อกแถวก่อนอ่าน (`SELECT ... FOR UPDATE`) คำขออื่นที่ต้องการแถวเดียวกันต้องรอ | กันได้แน่นอน ไม่ต้องแก้โครงสร้างตาราง / คำขอต้องรอกันเล็กน้อย |
| Optimistic Locking | เก็บ version ในแถว ตอนบันทึกตรวจว่า version ยังเหมือนเดิม | ไม่ต้องรอ / ต้องเพิ่มคอลัมน์และให้ผู้ใช้ลองใหม่เมื่อชน |

ระบบเลือก Pessimistic Locking เพราะการจองต้องตรวจหลายตาราง (เวลาชน โควตา ช่วงปิด) ไม่ใช่แค่แถวเดียว และไม่ต้องเพิ่ม migration ล็อกตามลำดับเดียวกันทุกที่ (ผู้ใช้ → ห้อง → การจอง) เพื่อป้องกัน deadlock

## 2.9 Docker, CI/CD และบริการคลาวด์

- **Docker** บรรจุแอปพร้อมสภาพแวดล้อมเป็น image ทำงานเหมือนกันทุกเครื่อง ระบบใช้ multi-stage build (build ด้วย JDK/Node แล้วรันด้วย image ที่เล็กกว่า) และ Docker Compose รันฐานข้อมูล backend และ frontend พร้อมกัน
- **GitHub Actions** รัน CI ทุก Pull Request (test backend, lint/build frontend, build Docker image) และ deploy อัตโนมัติเมื่อ merge เข้า `develop`
- **Branch protection** บังคับให้ทุกการรวมโค้ดผ่าน Pull Request มีผู้อนุมัติอย่างน้อย 1 คน และ CI ต้องผ่าน
- **Vercel** โฮสต์ Next.js, **Render** รัน backend จาก Dockerfile, **Neon** ให้บริการ PostgreSQL แบบ serverless

## 2.10 การทดสอบซอฟต์แวร์

| ระดับ | จุดประสงค์ | เครื่องมือในระบบ |
|---|---|---|
| Unit Test | ทดสอบ class เดียว แทน dependency ด้วย mock | JUnit 5 + Mockito |
| Controller (Slice) Test | ทดสอบ HTTP layer โดย mock service | `@WebMvcTest` + MockMvc |
| Repository Test | ทดสอบ query กับฐานข้อมูลจริงที่สร้างจาก migration | `@DataJpaTest` + H2 + Flyway |
| Integration Test | ทดสอบหลาย layer ร่วมกัน รวมถึงการทำงานพร้อมกัน | `@SpringBootTest` + thread pool |
| System / Acceptance Test | ทดสอบบนเว็บจริงตาม use case | เบราว์เซอร์, Postman |

Code coverage วัดว่า test เรียกโค้ดไปกี่เปอร์เซ็นต์ (บรรทัด, แขนงเงื่อนไข) ด้วย JaCoCo และการ inject `Clock` แทนการเรียกเวลาปัจจุบันตรง ๆ ทำให้ทดสอบกฎที่ขึ้นกับเวลาได้แน่นอน

## 2.11 ระบบที่เกี่ยวข้อง

**ระบบจองห้องเรียนคณะวิทยาศาสตร์ มหาวิทยาลัยขอนแก่น** (e.sc.kku.ac.th) แสดงตารางการใช้ห้องรายวันของแต่ละพื้นที่ในรูปแบบตาราง ห้อง × เวลา มีปฏิทินเลือกวัน ฟอร์มจองที่ระบุรายวิชา ผู้รับผิดชอบ และเลือกการจองซ้ำรายสัปดาห์ได้

| ความสามารถ | ระบบคณะวิทยาศาสตร์ | ระบบ SC09 |
|---|:-:|:-:|
| ตารางการใช้ห้องรายวัน (ห้อง × เวลา) | ✓ | ✓ |
| ปฏิทินบอกวันที่มีการจอง | ✓ | ✓ |
| ค้นหาห้องว่างตามความจุและอุปกรณ์ | - | ✓ |
| กฎการจองต่างกันตามบทบาท | - | ✓ |
| ขั้นตอนอนุมัติ + ประวัติสถานะ | ✓ (ยืนยันสถานะ) | ✓ |
| Check-in และบันทึกไม่มาใช้ห้องอัตโนมัติ | - | ✓ |
| แจ้งเตือนในระบบ | - | ✓ |
| แสดงช่วงปิดห้อง | - | ✓ |
| การจองซ้ำรายสัปดาห์ | ✓ | - (แนวทางพัฒนาต่อ) |

ระบบ SC09 นำแนวคิดตารางรวมทุกห้องและปฏิทินมาใช้ และเพิ่มการตรวจกฎอัตโนมัติ check-in และแจ้งเตือน ซึ่งระบบเดิมไม่มี
