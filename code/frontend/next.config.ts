import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // standalone ทำให้ Docker image เล็ก มีแค่ไฟล์ที่ต้องใช้รัน
  output: "standalone",
};

export default nextConfig;
