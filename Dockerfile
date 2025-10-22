# Use a base image with Java
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the JAR from the target folder (will be built in Maven)
COPY target/event-ticketing-order-service-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8082

# Run the jar
ENTRYPOINT ["java","-jar","app.jar"]
