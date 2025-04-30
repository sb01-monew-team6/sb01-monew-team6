# -----------------------------
# 1단계: Gradle 빌드 단계
# -----------------------------
FROM gradle:7.6.1-jdk17 AS build

# 작업 디렉토리 설정
WORKDIR /home/gradle/project

# 소스 코드 복사
COPY . .

# gradle 빌드 실행 (테스트 제외)
RUN chmod +x ./gradlew && ./gradlew clean build -x test --no-daemon


# -----------------------------
# 2단계: 인증서 다운로드 및 truststore 등록 단계
# -----------------------------
FROM amazoncorretto:17.0.7-alpine AS cert

# 작업 디렉토리 설정
WORKDIR /tmp

# AWS CLI 설치
RUN apk add --no-cache curl unzip && \
    curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
    unzip awscliv2.zip && \
    ./aws/install && \
    ln -s /usr/local/bin/aws /usr/bin/aws

# Docker build 시 전달받을 AWS 인증 정보
ARG AWS_ACCESS_KEY_ID
ARG AWS_SECRET_ACCESS_KEY
ARG AWS_REGION

# 환경 변수로 설정하여 aws cli가 사용할 수 있도록 함
ENV AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID \
    AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY \
    AWS_REGION=$AWS_REGION

# S3에서 인증서 다운로드 후 JVM truststore에 등록
RUN aws s3 cp s3://monew-s3/global-bundle.pem /etc/ssl/certs/ && \
    keytool -import -trustcacerts -alias aws-docdb \
    -file /etc/ssl/certs/global-bundle.pem \
    -keystore $JAVA_HOME/lib/security/cacerts \
    -storepass changeit -noprompt


# -----------------------------
# 3단계: 최종 실행 이미지 구성 단계
# -----------------------------
FROM amazoncorretto:17.0.7-alpine

# 애플리케이션 실행 디렉토리 설정
WORKDIR /app

# 인증서 등록된 truststore 파일 복사
COPY --from=cert $JAVA_HOME/lib/security/cacerts $JAVA_HOME/lib/security/cacerts

# 빌드된 JAR 파일 복사
COPY --from=build /home/gradle/project/build/libs/monew.jar monew.jar

# 컨테이너 포트 오픈
EXPOSE 8080

# Spring Boot 앱 실행 명령
CMD ["java", "-jar", "monew.jar"]