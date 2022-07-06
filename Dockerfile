FROM node:current-alpine3.16 AS nodejs
RUN apk add --no-cache git
WORKDIR /usr/src/node
RUN git clone https://github.com/ravel57/qrref-front.git
WORKDIR /usr/src/node/qrref-front
RUN ["yarn", "install"]
RUN ["yarn", "build"]

FROM gradle:7.4.2-jdk11-alpine AS gradle
COPY --chown=gradle:gradle . /home/gradle/src
COPY --from=nodejs /usr/src/node/qrref-front/dist/js/app*.js           /home/gradle/src/main/webapp/js/app.js
COPY --from=nodejs /usr/src/node/qrref-front/dist/js/chunk-vendors*.js /home/gradle/src/main/webapp/js/chunk-vendors.js
COPY --from=nodejs /usr/src/node/qrref-front/dist/css/app*.css         /home/gradle/src/main/webapp/css/app.css
WORKDIR /home/gradle/src
RUN gradle war

FROM tomcat:9.0.64-jre11 AS tomcat9
RUN rm -rf /usr/local/tomcat/webapps/* /usr/local/tomcat/conf/server.xml
COPY ./src/main/resources/toTomcat/* /usr/local/tomcat/conf/
COPY --from=gradle /home/gradle/src/build/libs/qrRef*.war /usr/local/tomcat/webapps/ROOT.war
CMD chmod +x /usr/local/tomcat/bin/catalina.sh
CMD ["catalina.sh", "run"]
