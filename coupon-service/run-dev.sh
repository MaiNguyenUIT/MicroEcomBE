cleanup() {
  echo "Caught signal or Spring Boot application exited. Stopping Docker Compose services..."
  docker compose down
  echo "Docker Compose services stopped."
}

trap cleanup INT TERM EXIT

if ! docker info > /dev/null 2>&1; then
  echo "Docker daemon is not running. Please start Docker and try again."
  exit 1
fi

echo "Starting Docker Compose services..."
docker compose up -d

if [ $? -ne 0 ]; then
  echo "Failed to start Docker Compose services. Aborting."
  exit 1
fi

echo "Waiting for services to be ready (e.g., PostgreSQL on port 5432)..."
sleep 10 

echo "Running Spring Boot application..."

mvn clean install -DskipTests
java -jar target/coupon-service-0.0.1-SNAPSHOT.jar
