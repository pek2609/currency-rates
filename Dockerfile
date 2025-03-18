FROM maven:3.9.9-amazoncorretto-21 AS package
WORKDIR /app
COPY src /app/src
COPY pom.xml /app/
RUN mvn -f /app/pom.xml clean package -DskipTests

FROM openjdk:21
WORKDIR /app
COPY --from=package /app/target/currency_rates-0.0.1-SNAPSHOT.jar currency-rates.jar
ENTRYPOINT ["java", "-jar", "currency-rates.jar"]