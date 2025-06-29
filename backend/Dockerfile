FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x gradlew

# Copy source code
COPY src src

# Copy startup script
COPY start.sh .
RUN chmod +x start.sh

# Build the application
RUN ./gradlew build -x test

# Expose port
EXPOSE 8080

# Run the application using startup script
CMD ["./start.sh"] 