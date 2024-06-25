FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY pom.xml .
COPY src /app/src

RUN mvn --quiet package -DskipTests

FROM bellsoft/liberica-openjdk-alpine:21

WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

ENV JAVA_OPTS="-Xms2048m -Duser.timezone=Europe/Tallinn"
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=80"

CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
