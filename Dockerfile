# ---- Build Stage ----
FROM gradle:8.2.1-jdk21 AS build

WORKDIR /app
COPY . .

RUN gradle bootJar -i --stacktrace

# ---- Production Stage ----
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/build/libs/GridServerFinalClean-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]