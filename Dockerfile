FROM openjdk:17

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} usedauction.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=build", "-jar", "/usedauction.jar"]