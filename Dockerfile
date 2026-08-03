# Etapa 1: Construir el WAR con Maven
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM tomcat:10.1-jdk17
COPY target/ProBiblio.war /usr/local/tomcat/webapps/ProBiblio.war

EXPOSE 8080
CMD ["catalina.sh", "run"]