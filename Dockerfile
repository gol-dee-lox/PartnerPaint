# Use stable OpenJDK base image
FROM eclipse-temurin:17-jdk

# Set working directory inside container
WORKDIR /app

# Copy your built fat jar into container
COPY build/libs/GridServerFinalClean-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080 (standard Spring Boot port)
EXPOSE 8080

# Run Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]