# -----------------------------
# 1단계: Build Stage
# -----------------------------
FROM gradle:7.6.1-jdk17 AS build
WORKDIR /home/gradle/project
COPY . .

# 권한 주고 gradlew로 빌드
RUN chmod +x ./gradlew && ./gradlew clean build -x test --no-daemon

# -----------------------------
# 2단계: Runtime Stage
# -----------------------------
FROM amazoncorretto:17.0.7-alpine
WORKDIR /app

COPY --from=build /home/gradle/project/build/libs/monew.jar monew.jar

EXPOSE 8080

CMD ["java", "-jar", "monew.jar"]