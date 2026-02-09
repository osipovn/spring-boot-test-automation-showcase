# Stage 1: Build
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Install Maven (no wrapper in repo)
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Cache dependencies first
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy sources and build
COPY src ./src
RUN mvn package -DskipTests -B

# Stage 2: Run
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
