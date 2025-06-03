# ---- Build Stage ----
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app
COPY . .

# Use gradle wrapper for build
RUN ./gradlew bootJar --no-daemon --warning-mode all

# ---- Production Stage ----
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/build/libs/GridServerFinalClean-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]