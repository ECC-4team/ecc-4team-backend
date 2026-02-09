FROM gradle:8-jdk17 AS build
WORKDIR /app

# 전체 레포 복사
COPY . .

# backend로 이동 (Gradle 프로젝트 위치)
WORKDIR /app/backend
RUN gradle bootJar -x test

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/backend/build/libs/*.jar app.jar

ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]
