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
COPY --from=build /home/app/target/CheckOutService-1.0-SNAPSHOT.jar /usr/local/lib/CheckOutService.jar
EXPOSE 5050
ENTRYPOINT ["java","-jar","/usr/local/lib/CheckOutService.jar"]
