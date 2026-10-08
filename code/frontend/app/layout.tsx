import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import { Providers } from "@/components/Providers";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "ระบบจองห้อง SC09",
  description: "ระบบจองห้องอาคารวิทยวิภาส (SC09) วิทยาลัยการคอมพิวเตอร์ มหาวิทยาลัยขอนแก่น",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="th" className={`${geistSans.variable} ${geistMono.variable} h-full antialiased`}>
      <body className="min-h-full flex flex-col">
        <Providers>
          <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-8">{children}</main>
        </Providers>
      </body>
    </html>
  );
}
