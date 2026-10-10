import { BOOKING_STATUS_LABELS, BookingStatus } from "@/lib/bookings";
import { Badge } from "./ui";

const STYLES: Record<BookingStatus, string> = {
  PENDING: "bg-amber-50 text-amber-700",
  APPROVED: "bg-emerald-50 text-emerald-700",
  REJECTED: "bg-rose-50 text-rose-700",
  CANCELLED: "bg-slate-100 text-slate-500",
  CHECKED_IN: "bg-sky-50 text-sky-700",
  COMPLETED: "bg-indigo-50 text-indigo-700",
  NO_SHOW: "bg-orange-50 text-orange-700",
};

export function BookingStatusBadge({ status }: { status: BookingStatus }) {
  return <Badge className={STYLES[status]}>{BOOKING_STATUS_LABELS[status]}</Badge>;
}
