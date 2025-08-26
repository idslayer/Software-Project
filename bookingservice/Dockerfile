FROM amazoncorretto:17.0.16 AS builder

WORKDIR /app

# Copy project files
COPY . .

# Ensure gradlew is executable and fix line endings (Windows safe)
RUN chmod +x gradlew && sed -i "s/\r$//" gradlew

# Run tests & build the jar
RUN ./gradlew clean build -x test

# --- Stage 2: Runtime ---
FROM amazoncorretto:17.0.16

WORKDIR /app

# Copy only the built jar from builder stage
COPY --from=builder /app/build/libs/*.jar bookingservice.jar

# Run the application
CMD ["java", "-jar", "bookingservice.jar"]