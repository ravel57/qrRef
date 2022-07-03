FROM gradle:7.4.2-jdk11-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM openjdk:11
EXPOSE 8443
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/qrRef-1.jar /app/checkAdminPcHost.jar
COPY ./src/main/resources/cer.pfx /src/main/resources/cer.pfx
CMD java -jar /app/checkAdminPcHost.jar
