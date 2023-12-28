FROM maven:3.9.6-eclipse-temurin-17-alpine as build
WORKDIR /
COPY . .
RUN mvn clean install


FROM eclipse-temurin:17-jdk-alpine as run
COPY --from=build /target/*.jar app.jar
RUN keytool -genkey -keyalg RSA -alias notes -keystore keystore.jks -storepass password -validity 365 -keysize 2048 -keypass password -dname "CN= Wojciech Majchrzak, OU=, O=, L=, ST=, C=PL"
#COPY notes.jks notes.jks

ENTRYPOINT ["java","-jar","/app.jar"]