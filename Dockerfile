# Maven 
FROM maven:3.8.1-openjdk-11-slim AS builder
WORKDIR /opt/app
COPY pom.xml /opt/app
RUN mvn dependency:go-offline
COPY ./src ./src
RUN mvn clean -e -B package

# Java
FROM eclipse-temurin:17-jre-jammy
EXPOSE 8080
WORKDIR /opt/app
RUN mkdir /opt/app/input
RUN mkdir /opt/app/output
RUN mkdir /opt/app/classifier
COPY --from=builder /opt/app/target/*.jar /opt/app/*.jar
ENTRYPOINT ["java", "-jar", "/opt/app/*.jar" ]