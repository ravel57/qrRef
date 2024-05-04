FROM node:18-alpine3.17 AS nodejs
RUN apk add --no-cache git
WORKDIR /usr/src/node
RUN git clone https://github.com/ravel57/qrref-front.git
WORKDIR /usr/src/node/qrref-front
RUN ["yarn", "install"]
RUN ["yarn", "build"]

FROM gradle:8.7.0-jdk17 AS gradle
COPY --chown=gradle:gradle . /home/gradle/
COPY --from=nodejs /usr/src/node/qrref-front/dist/static/index.js   /home/gradle/src/main/webapp/js/index.js
COPY --from=nodejs /usr/src/node/qrref-front/dist/static/vendor.js  /home/gradle/src/main/webapp/js/vendor.js
COPY --from=nodejs /usr/src/node/qrref-front/dist/static/index.css  /home/gradle/src/main/webapp/css/index.css
COPY --from=nodejs /usr/src/node/qrref-front/dist/static/vendor.css /home/gradle/src/main/webapp/css/vendor.css
WORKDIR /home/gradle/
RUN ["gradle", "war"]

FROM tomcat:10.1.23-jre21 AS tomcat9
RUN rm -rf /usr/local/tomcat/webapps/* /usr/local/tomcat/conf/server.xml
COPY ./src/main/resources/toTomcat/* /usr/local/tomcat/conf/
COPY --from=gradle /home/gradle/build/libs/*.war /usr/local/tomcat/webapps/ROOT.war
CMD chmod +x /usr/local/tomcat/bin/catalina.sh
CMD ["catalina.sh", "run"]