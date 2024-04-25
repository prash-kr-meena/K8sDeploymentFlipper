# Use the official OpenJDK image for the Java base
FROM openjdk:21-slim as build

LABEL authors="vishnu_vibhuti"

# Set the working directory inside the container
WORKDIR /app

# Copy maven executable to the image
COPY mvnw .
COPY .mvn .mvn

# Copy the pom.xml file
COPY pom.xml .

# Copy your source code
COPY src src

# Build the application
RUN ./mvnw package -DskipTests

# Multi-stage build: use a smaller JDK runtime for the final image
FROM openjdk:21-slim

# Set deployment directory
WORKDIR /app

# Copy the jar from the previous stage
COPY --from=build /app/target/*.jar app.jar

# Set the command to run on container start
ENTRYPOINT ["java", "-jar", "app.jar"]


# Commands
# docker login  # if not loged in
# docker build -t oladockerola/deployment-flipper:3 -f Dockerfile . && docker push oladockerola/deployment-flipper:3
# docker push oladockerola/deployment-flipper:1