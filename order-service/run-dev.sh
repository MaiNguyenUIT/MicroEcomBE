#!/bin/bash

# Function to clean up Docker Compose services when the script exits or is interrupted
cleanup() {
  echo "Caught signal or Spring Boot application exited. Stopping Docker Compose services..."
  # 'docker compose down' needs to be run from the directory containing docker-compose.yml
  # Assuming you're running this script from /home/minhk/WEB/SE356/order-service/
  docker compose down
  echo "Docker Compose services stopped."
}

# Trap signals to ensure cleanup() is always called
trap cleanup INT TERM EXIT

# Check if Docker daemon is running
if ! docker info > /dev/null 2>&1; then
  echo "Docker daemon is not running. Please start Docker and try again."
  exit 1
fi

echo "Starting Docker Compose services for order-service (MySQL)..."
# Run 'docker compose up -d' from the current directory (order-service/)
docker compose up -d

# Check if Docker Compose services started successfully
if [ $? -ne 0 ]; then
  echo "Failed to start Docker Compose services. Aborting."
  exit 1
fi

echo "Waiting for MySQL database service (localhost:3306) to be ready..."
# Giving MySQL a bit more time to fully initialize and become ready for connections
sleep 15 

echo "Building order-service application..."
# Run Maven clean install, skipping tests, from the current directory (order-service/)
mvn clean install -DskipTests

# Check if Maven build was successful
if [ $? -ne 0 ]; then
  echo "Maven build failed for order-service. Aborting."
  exit 1
fi

echo "Running Spring Boot application (order-service.jar)..."
# Run the Spring Boot JAR from the 'target' directory within order-service/
java -jar target/order-service-0.0.1-SNAPSHOT.jar

# The cleanup function will automatically run when the Java application exits or is interrupted