import type { Metadata } from "next";
import { IBM_Plex_Sans_Thai } from "next/font/google";
import { Providers } from "@/components/Providers";
import { Navbar } from "@/components/Navbar";
import "./globals.css";

const plexThai = IBM_Plex_Sans_Thai({
  variable: "--font-plex-thai",
  subsets: ["thai", "latin"],
  weight: ["400", "500", "600", "700"],
});

export const metadata: Metadata = {
  title: "ระบบจองห้อง SC09",
  description: "ระบบจองห้องอาคารวิทยวิภาส (SC09) วิทยาลัยการคอมพิวเตอร์ มหาวิทยาลัยขอนแก่น",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="th" className={`${plexThai.variable} h-full antialiased`}>
      <body className="flex min-h-full flex-col bg-slate-50 text-slate-900">
        <Providers>
          <Navbar />
          <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-8">{children}</main>
          <footer className="border-t border-slate-200 py-4 text-center text-xs text-slate-400">
            CP353002 · วิทยาลัยการคอมพิวเตอร์ มหาวิทยาลัยขอนแก่น
          </footer>
        </Providers>
      </body>
    </html>
  );
}
