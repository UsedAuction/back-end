FROM openjdk:17

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} usedauction.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=build", "-jar", "/usedauction.jar"]