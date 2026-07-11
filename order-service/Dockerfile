# --- Stage 1: Use pre-built JAR (already built locally) ---
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Copy the pre-built fat JAR
COPY target/order-service-1.0.0-SNAPSHOT.jar app.jar

# Expose port 8081
EXPOSE 8081

# Run with prod profile
ENTRYPOINT ["java", \
  "-Xms256m", \
  "-Xmx512m", \
  "--enable-native-access=ALL-UNNAMED", \
  "-jar", "app.jar", \
  "--spring.profiles.active=prod"]
