FROM --platform=linux/amd64 maven:amazoncorretto AS build

COPY src /app/src

COPY pom.xml /app

WORKDIR /app
RUN mvn clean package -DskipTests

FROM --platform=linux/amd64 openjdk:17-slim
COPY --from=build /app/target/moneytransfer-1.0.0.jar /app/app.jar

WORKDIR /app

EXPOSE 8081

CMD ["java", "-jar", "app.jar"]