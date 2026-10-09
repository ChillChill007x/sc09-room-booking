# base image ดึงจาก AWS ECR Public (mirror ของ Docker Official Images ตัวเดียวกับบน Docker Hub)
# เพราะ Docker Hub จำกัดการดึงแบบไม่ login ทำให้ CI บน GitHub Actions ล้มด้วย 429 Too Many Requests
# ---------- dependencies ----------
FROM public.ecr.aws/docker/library/node:22-alpine AS deps
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci

# ---------- build ----------
FROM public.ecr.aws/docker/library/node:22-alpine AS build
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
# NEXT_PUBLIC_* ถูกฝังตอน build จึงต้องส่ง URL ของ backend ผ่าน build arg
ARG NEXT_PUBLIC_API_URL=http://localhost:8080
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
ENV NEXT_TELEMETRY_DISABLED=1
RUN npm run build

# ---------- run ----------
FROM public.ecr.aws/docker/library/node:22-alpine AS run
WORKDIR /app
ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
ENV PORT=3000
ENV HOSTNAME=0.0.0.0

COPY --from=build /app/public ./public
COPY --from=build --chown=node:node /app/.next/standalone ./
COPY --from=build --chown=node:node /app/.next/static ./.next/static
USER node

EXPOSE 3000
CMD ["node", "server.js"]
