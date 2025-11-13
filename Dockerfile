# ---- Build Stage ----
FROM maven:3.9.9 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY target/generated-sources ./target/generated-sources

# Package application (skip jOOQ generation - using pre-generated sources)
RUN mvn package -DskipTests -Djooq.codegen.skip=true

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
