# ---- Build Stage ----
FROM eclipse-temurin:21-jdk AS build

# Install Gradle manually
RUN apt-get update && \
    apt-get install -y wget unzip && \
    wget https://services.gradle.org/distributions/gradle-8.2.1-bin.zip && \
    unzip gradle-8.2.1-bin.zip -d /opt && \
    ln -s /opt/gradle-8.2.1/bin/gradle /usr/bin/gradle

WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon --warning-mode all

# ---- Production Stage ----
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/build/libs/GridServerFinalClean-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]