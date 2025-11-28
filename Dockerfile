# Use a base image with a Java Runtime Environment (JRE)
FROM eclipse-temurin:21-jre-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the built Spring Boot JAR file into the container
# Replace 'your-application.jar' with the actual name of your JAR file
COPY target/shop-lens-0.0.1-SNAPSHOT/.jar app.jar

# Expose the port your Spring Boot application listens on (default is 8080)
EXPOSE 8080

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]