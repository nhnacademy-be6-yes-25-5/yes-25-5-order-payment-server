# Dockerfile

# Use the official Maven image with Java 11
FROM maven:3.8.8-eclipse-temurin-21

# Set the working directory
WORKDIR /orders-payments

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the application code
COPY . .

# Build the application
RUN mvn package

# Default command
CMD ["java", "-jar", "target/yes-25-5-order-payment-server-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=prod"]
