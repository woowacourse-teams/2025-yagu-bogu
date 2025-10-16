# --- Build stage ---
FROM amazoncorretto:21 AS build
WORKDIR /workspace

# Gradle 캐시 최적화: wrapper/gradle 관련 파일을 먼저 복사
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
# 소스는 마지막에 복사 (캐시 활용)
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
RUN ./gradlew --no-daemon dependencies || true

COPY src src

# 빌드
RUN ./gradlew --no-daemon --build-cache bootJar -x test

# --- Run stage ---
FROM amazoncorretto:21-alpine

# 환경 변수 설정
# Dockerfile에는 JVM 옵션, 프로필 정도만 기본값으로
ENV TZ=Asia/Seoul
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC"

WORKDIR /app

# 빌드 결과 복사
COPY --from=build /workspace/build/libs/*SNAPSHOT.jar /app/app.jar

# 실행
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
