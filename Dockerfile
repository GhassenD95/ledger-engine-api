FROM node:20-alpine AS frontend-builder
WORKDIR /build
COPY frontend/ .
RUN npm ci && npm run build

FROM maven:3.9.9-eclipse-temurin-17-alpine AS backend-builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
COPY .mvn ./.mvn
COPY mvnw .
RUN ./mvnw package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S app && adduser -S app -G app
USER app
WORKDIR /app
COPY --from=backend-builder /build/target/*.jar app.jar
COPY --from=frontend-builder /build/dist /app/static
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
