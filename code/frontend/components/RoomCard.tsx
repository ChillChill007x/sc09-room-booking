import Link from "next/link";
import { Room, ROOM_STATUS_LABELS } from "@/lib/rooms";
import { Badge } from "./ui";

export function RoomCard({ room }: { room: Room }) {
  return (
    <Link
      href={`/rooms/${room.id}`}
      className="block rounded-xl border border-slate-200 bg-white p-4 shadow-sm transition hover:border-indigo-300 hover:shadow"
    >
      <div className="flex items-start justify-between gap-2">
        <div>
          <p className="text-xs font-medium text-indigo-600">{room.code}</p>
          <h3 className="font-semibold text-slate-900">{room.name}</h3>
        </div>
        {room.status !== "ACTIVE" && (
          <Badge className="bg-amber-50 text-amber-700">{ROOM_STATUS_LABELS[room.status]}</Badge>
        )}
      </div>
      <p className="mt-2 text-sm text-slate-500">
        ชั้น {room.floor} · {room.roomType.name} · {room.capacity} ที่นั่ง
      </p>
      {room.equipment.length > 0 && (
        <div className="mt-3 flex flex-wrap gap-1.5">
          {room.equipment.map((item) => (
            <Badge key={item.equipmentId} className="bg-slate-100 text-slate-600">
              {item.name} ×{item.quantity}
            </Badge>
          ))}
        </div>
      )}
    </Link>
  );
}
