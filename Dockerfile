# ----- Stage 1: The Build Stage -----
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# ----- Stage 2: The Final Image -----
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/ROOT-bootable.jar app.jar
EXPOSE 8080
# Add the -b=0.0.0.0 argument here
CMD ["java", "-jar", "app.jar", "-b=0.0.0.0"]