# 1️⃣ Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -e -B -DskipTests clean package

# 2️⃣ Run stage
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar

# Expose port (Render will inject PORT automatically)
EXPOSE 8080

# Run Spring Boot app
ENTRYPOINT ["java","-jar","app.jar"]
