# Use an official Maven image as a parent image
FROM maven:3.9.9-openjdk-17 AS builder

# Set the working directory
WORKDIR /app

# Copy the pom.xml and the source code
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Use an official JDK runtime as a parent image for the final image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the JAR file from the builder stage
COPY --from=builder /app/target/upskilled-0.0.1-SNAPSHOT.jar /app/upskilled.jar

# Expose the port your Spring Boot app is running on
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "/app/upskilled.jar"]
