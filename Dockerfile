FROM amazoncorretto:21-alpine-jdk as builder

WORKDIR /app

COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradlew .
COPY gradle ./gradle
COPY src ./src

RUN ./gradlew build -x test

FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

COPY --from=builder /app/build/libs/payments-0.0.1-SNAPSHOT.jar ./payments.jar

# Expose the port your app will run on
EXPOSE 8080

# Specify the command to run on container startup
CMD ["java", "-jar", "payments.jar"]