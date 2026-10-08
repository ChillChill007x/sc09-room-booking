# ---------- build ----------
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# โหลด dependency ก่อนเพื่อให้ Docker cache layer นี้ไว้ ถ้า pom.xml ไม่เปลี่ยนจะไม่โหลดใหม่
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -q package -DskipTests

# ---------- run ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV TZ=Asia/Bangkok

RUN useradd --system --no-create-home spring
COPY --from=build /app/target/*.jar app.jar
USER spring

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
