# --- Build stage ---
FROM amazoncorretto:21 AS build
WORKDIR /workspace

# Gradle 캐시 최적화: wrapper/gradle 관련 파일을 먼저 복사
# 빌드 컨텍스트는 backend/ (Gradle 루트)
COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle ./
COPY build.gradle ./

# app 서브모듈의 build.gradle 복사
COPY app/build.gradle app/build.gradle

# crawling 서브모듈의 build.gradle 복사 (settings.gradle에 include 되어 있어 Gradle 초기화 시 필요)
COPY crawling/build.gradle crawling/build.gradle

RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
RUN ./gradlew --no-daemon dependencies || true

# app 소스만 복사 (crawling은 불필요)
COPY app/src app/src

# app 서브모듈만 빌드
RUN ./gradlew :app:bootJar --no-daemon --build-cache -x test

# 빌드 결과 확인 (디버깅용)
RUN ls -la /workspace/app/build/libs/

# --- Run stage ---
FROM amazoncorretto:21-alpine

# 환경 변수 설정
# Dockerfile에는 JVM 옵션, 프로필 정도만 기본값으로
ENV TZ=Asia/Seoul
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC"

WORKDIR /app

# 빌드 결과 복사 (멀티모듈이므로 app/build/libs/ 경로)
COPY --from=build /workspace/app/build/libs/app.jar app.jar

# 실행
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
