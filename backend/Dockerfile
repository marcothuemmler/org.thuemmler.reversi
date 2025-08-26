FROM gradle:8.14-jdk21 AS build
WORKDIR /app

COPY settings.gradle.kts ./
COPY backend/build.gradle.kts ./backend/
COPY gradle ./gradle
COPY backend/src ./backend/src

WORKDIR /app/backend
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/backend/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
