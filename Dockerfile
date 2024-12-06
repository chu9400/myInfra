FROM amazoncorretto:17-alpine AS build
WORKDIR /app
COPY . .

FROM amazoncorretto:17-alpine AS runtime
WORKDIR /app
COPY --from=build /app/build/libs/*.jar /app/server.jar
EXPOSE 8080
CMD ["java", "-jar", "/app/server.jar"]