FROM eclipse-temurin:22-jdk-alpine AS build

WORKDIR /app

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
COPY src src

RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:22-jdk-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]