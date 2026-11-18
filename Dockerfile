# Spring Boot BE. Bước 1 đóng gói bằng Maven, bước 2 chỉ chạy JRE.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN ./mvnw -q dependency:go-offline
COPY src src
RUN ./mvnw -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/target/*.jar app.jar
RUN useradd --uid 10001 app
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
