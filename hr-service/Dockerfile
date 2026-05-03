FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY hr-service/pom.xml .
RUN mvn dependency:go-offline -B
COPY hr-service/src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
COPY --from=builder /app/target/*.jar /app.jar
EXPOSE 50054
ENTRYPOINT ["java", "-jar", "/app.jar"]
