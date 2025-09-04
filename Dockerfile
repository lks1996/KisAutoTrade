# ===== 빌드 단계 =====
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# 소스 복사
COPY . .

# Gradle 빌드 (테스트 제외)
RUN gradle clean bootJar -x test

# ===== 실행 단계 =====
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# 빌드 단계에서 생성된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# Spring Boot 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
