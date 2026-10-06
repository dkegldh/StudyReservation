FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# root가 아닌 사용자로 실행
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# build.gradle에 `jar { enabled = false }` 를 넣어 plain jar가 생기지 않게 할 것
COPY build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
