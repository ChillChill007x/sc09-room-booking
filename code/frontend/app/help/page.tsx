import Link from "next/link";
import { BookingStatus } from "@/lib/bookings";
import { BookingStatusBadge } from "@/components/BookingStatusBadge";
import { Card, PageHeader } from "@/components/ui";

/*
 * ตัวเลขทุกค่าในหน้านี้ต้องตรงกับ backend:
 * StudentBookingPolicy / LecturerBookingPolicy / StaffBookingPolicy, app.booking.building-open/close,
 * CHECK_IN_WINDOW ใน BookingLifecycleServiceImpl ถ้าแก้กฎที่ backend ต้องแก้หน้านี้ด้วย
 */

const ROLE_RULES = [
  { role: "นักศึกษา", approval: "ต้องรอเจ้าหน้าที่อนุมัติ", hours: 2, days: 7, active: "2 รายการ" },
  { role: "อาจารย์", approval: "อนุมัติอัตโนมัติ", hours: 4, days: 30, active: "5 รายการ" },
  { role: "เจ้าหน้าที่ / ผู้ดูแลระบบ", approval: "อนุมัติอัตโนมัติ", hours: 8, days: 90, active: "ไม่จำกัด" },
];

const STATUSES: { status: BookingStatus; meaning: string }[] = [
  { status: "PENDING", meaning: "ส่งคำขอแล้ว รอเจ้าหน้าที่อนุมัติ ห้องช่วงเวลานี้ถูกกันไว้ให้แล้ว" },
  { status: "APPROVED", meaning: "ได้รับอนุมัติ ใช้ห้องได้ตามเวลา อย่าลืม check-in" },
  { status: "CHECKED_IN", meaning: "เข้าใช้ห้องแล้ว (check-in สำเร็จ)" },
  { status: "COMPLETED", meaning: "ใช้ห้องเสร็จแล้ว ระบบปิดให้อัตโนมัติเมื่อเลยเวลาสิ้นสุด" },
  { status: "REJECTED", meaning: "เจ้าหน้าที่ไม่อนุมัติ ดูเหตุผลได้ในหน้ารายละเอียดและแจ้งเตือน" },
  { status: "CANCELLED", meaning: "ยกเลิกแล้ว (โดยผู้จองหรือเจ้าหน้าที่) ห้องว่างให้คนอื่นจองได้" },
  { status: "NO_SHOW", meaning: "ไม่ check-in ภายใน 15 นาทีหลังเวลาเริ่ม ระบบบันทึกให้อัตโนมัติ" },
];

const FAQ = [
  {
    q: "ขึ้นว่า \"ช่วงเวลานี้มีการจองห้องนี้แล้ว\"",
    a: "มีคนจองหรือส่งคำขอช่วงเวลานั้นไว้ก่อนแล้ว ดูช่วงที่ว่างได้จากตารางในหน้าจอง หรือหน้าตารางการใช้ห้อง",
  },
  {
    q: "ขึ้นว่า \"ห้องปิดใช้งานในช่วงเวลาที่เลือก\"",
    a: "เจ้าหน้าที่ปิดห้องช่วงนั้นไว้ (เช่น ซ่อมบำรุง) ให้เลือกเวลาอื่นหรือห้องอื่น",
  },
  {
    q: "ขึ้นว่า \"มีการจองที่ยังไม่สิ้นสุดครบ ... รายการแล้ว\"",
    a: "นับรายการที่รออนุมัติ อนุมัติแล้ว และกำลังใช้ห้อง ต้องรอให้รายการเดิมจบ หรือยกเลิกรายการที่ไม่ใช้ก่อน",
  },
  {
    q: "แก้เวลาการจองได้ไหม",
    a: "ยังแก้ผ่านหน้าเว็บไม่ได้ ให้ลบคำขอ (ขณะรออนุมัติ) หรือยกเลิก (ก่อนเวลาเริ่ม) แล้วจองใหม่",
  },
  {
    q: "ใช้ห้องเสร็จก่อนเวลา ต้องทำอะไรไหม",
    a: "กด \"จบการใช้งาน\" ในหน้ารายละเอียดการจองได้ ถ้าไม่กด ระบบจะปิดให้เองเมื่อถึงเวลาสิ้นสุด",
  },
];

export default function HelpPage() {
  return (
    <div className="mx-auto max-w-3xl space-y-4">
      <PageHeader title="คู่มือการใช้งาน" subtitle="วิธีจองห้องในอาคาร SC09 กฎของระบบ และความหมายของสถานะ" />

      <Card>
        <h2 className="mb-3 font-semibold text-slate-900">ขั้นตอนการจองห้อง</h2>
        <ol className="list-decimal space-y-2 pl-5 text-sm text-slate-700">
          <li>
            <Link href="/login" className="text-indigo-600 hover:underline">
              เข้าสู่ระบบ
            </Link>{" "}
            ด้วยอีเมล @kkumail.com (ยังไม่มีบัญชีให้{" "}
            <Link href="/register" className="text-indigo-600 hover:underline">
              สมัครสมาชิก
            </Link>
            )
          </li>
          <li>
            ไปที่{" "}
            <Link href="/rooms" className="text-indigo-600 hover:underline">
              ห้อง
            </Link>{" "}
            แล้วเลือก &quot;ค้นหาห้องว่าง&quot; ระบุวัน เวลา จำนวนคน และอุปกรณ์ที่ต้องใช้ หรือดูภาพรวมทุกห้องที่{" "}
            <Link href="/schedule" className="text-indigo-600 hover:underline">
              ตารางการใช้ห้อง
            </Link>
          </li>
          <li>กด &quot;จองห้องนี้&quot; กรอกวัน เวลา วัตถุประสงค์ และจำนวนผู้เข้าร่วม แล้วส่งคำขอ</li>
          <li>รอผลทางกระดิ่งแจ้งเตือน นักศึกษาต้องรออนุมัติ ส่วนอาจารย์และเจ้าหน้าที่ได้รับอนุมัติทันที</li>
          <li>
            เมื่อถึงเวลา เปิดหน้า{" "}
            <Link href="/bookings" className="text-indigo-600 hover:underline">
              การจองของฉัน
            </Link>{" "}
            แล้วกด check-in ภายในช่วง 15 นาทีก่อนถึง 15 นาทีหลังเวลาเริ่ม
          </li>
        </ol>
      </Card>

      <Card>
        <h2 className="mb-3 font-semibold text-slate-900">กฎการจองตามบทบาท</h2>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-500">
              <tr>
                <th className="px-3 py-2">บทบาท</th>
                <th className="px-3 py-2">การอนุมัติ</th>
                <th className="px-3 py-2">ต่อครั้งไม่เกิน</th>
                <th className="px-3 py-2">จองล่วงหน้าได้</th>
                <th className="px-3 py-2">จองค้างไว้ได้พร้อมกัน</th>
              </tr>
            </thead>
            <tbody>
              {ROLE_RULES.map((rule) => (
                <tr key={rule.role} className="border-t border-slate-100">
                  <td className="px-3 py-2 font-medium text-slate-800">{rule.role}</td>
                  <td className="px-3 py-2">{rule.approval}</td>
                  <td className="px-3 py-2">{rule.hours} ชั่วโมง</td>
                  <td className="px-3 py-2">{rule.days} วัน</td>
                  <td className="px-3 py-2">{rule.active}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <ul className="mt-3 list-disc space-y-1 pl-5 text-sm text-slate-600">
          <li>จองได้เฉพาะเวลาเปิดอาคาร 08:00 - 22:00 น. และต้องเริ่มและจบในวันเดียวกัน</li>
          <li>จองเวลาที่ผ่านมาแล้วไม่ได้ และจำนวนผู้เข้าร่วมต้องไม่เกินความจุของห้อง</li>
          <li>ห้องเดียวกันจองเวลาซ้อนกันไม่ได้ (จบ 10:00 แล้วอีกคนเริ่ม 10:00 ได้)</li>
          <li>&quot;จองค้างไว้&quot; นับรายการที่รออนุมัติ อนุมัติแล้ว และกำลังใช้ห้อง</li>
        </ul>
      </Card>

      <Card>
        <h2 className="mb-3 font-semibold text-slate-900">ความหมายของสถานะ</h2>
        <dl className="space-y-2 text-sm">
          {STATUSES.map((item) => (
            <div key={item.status} className="flex flex-col gap-1 sm:flex-row sm:items-start sm:gap-3">
              <dt className="w-32 shrink-0">
                <BookingStatusBadge status={item.status} />
              </dt>
              <dd className="text-slate-700">{item.meaning}</dd>
            </div>
          ))}
        </dl>
      </Card>

      <Card>
        <h2 className="mb-3 font-semibold text-slate-900">ยกเลิก ลบคำขอ และ check-in</h2>
        <ul className="list-disc space-y-1 pl-5 text-sm text-slate-700">
          <li>ลบคำขอได้เฉพาะของตัวเอง และเฉพาะตอนที่ยังรออนุมัติ</li>
          <li>ยกเลิกการจองได้ก่อนถึงเวลาเริ่ม (ทั้งรายการที่รออนุมัติและอนุมัติแล้ว)</li>
          <li>check-in ได้เฉพาะผู้จอง ตั้งแต่ 15 นาทีก่อนถึง 15 นาทีหลังเวลาเริ่ม</li>
          <li>ถ้าไม่ check-in ภายใน 15 นาทีหลังเวลาเริ่ม ระบบจะบันทึกว่า &quot;ไม่มาใช้ห้อง&quot; ให้อัตโนมัติ</li>
        </ul>
      </Card>

      <Card>
        <h2 className="mb-3 font-semibold text-slate-900">สำหรับเจ้าหน้าที่และผู้ดูแลระบบ</h2>
        <ul className="list-disc space-y-1 pl-5 text-sm text-slate-700">
          <li>
            อนุมัติหรือปฏิเสธคำขอที่{" "}
            <Link href="/admin/approvals" className="text-indigo-600 hover:underline">
              คิวอนุมัติ
            </Link>{" "}
            การปฏิเสธต้องระบุเหตุผลทุกครั้ง ผู้จองจะได้รับแจ้งเตือนพร้อมเหตุผล
          </li>
          <li>อนุมัติได้ก่อนถึงเวลาเริ่มเท่านั้น และอนุมัติไม่ได้ถ้าห้องปิดหรืออยู่ระหว่างปิดปรับปรุงในช่วงนั้น</li>
          <li>
            ปิดห้องชั่วคราวได้ที่{" "}
            <Link href="/admin/rooms" className="text-indigo-600 hover:underline">
              จัดการห้อง
            </Link>{" "}
            &gt; ปิดห้อง ถ้าช่วงที่จะปิดมีการจองที่ยังใช้งานอยู่ ต้องยกเลิกหรือปฏิเสธการจองเหล่านั้นก่อน
          </li>
          <li>ลบห้องที่ยังมีการจองในอนาคตไม่ได้ ห้องที่ลบจะเปลี่ยนเป็นเลิกใช้งาน ข้อมูลการจองเดิมยังอยู่</li>
          <li>ดูสถิติการใช้ห้องได้ที่แดชบอร์ด</li>
        </ul>
      </Card>

      <Card>
        <h2 className="mb-3 font-semibold text-slate-900">คำถามที่พบบ่อย</h2>
        <div className="space-y-3 text-sm">
          {FAQ.map((item) => (
            <div key={item.q}>
              <p className="font-medium text-slate-800">{item.q}</p>
              <p className="text-slate-600">{item.a}</p>
            </div>
          ))}
        </div>
      </Card>
    </div>
  );
}
