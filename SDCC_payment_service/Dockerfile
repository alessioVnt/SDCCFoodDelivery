#
# Build stage
#
FROM maven:3.6.0-jdk-8-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

#
# Package stage
#
FROM openjdk:8-jre-slim
COPY --from=build /home/app/target/SDCC_payment_service-1.0-SNAPSHOT.jar /usr/local/lib/SDCC_payment_service.jar
EXPOSE 50051
ENTRYPOINT ["java","-jar","/usr/local/lib/SDCC_payment_service.jar"]
