# Generic Dockerfile for CallMe Spring Boot microservices
# Build with: docker build --build-arg MODULE=utilisateur-ms .

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY . .
ARG MODULE
# Build the selected module and its required modules, then copy the executable Spring Boot jar explicitly.
RUN mvn -pl ${MODULE} -am clean package -Dmaven.test.skip=true  && cp ${MODULE}/target/${MODULE}-1.0.0.jar /workspace/app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
