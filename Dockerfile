
# Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
# Click nbfs://nbhost/SystemFileSystem/Templates/Other/Dockerfile to edit this template

FROM tomcat:8.5.86
#WORKDIR /usr/local/tomcat
RUN  apt-get update && apt-get install gnupg wget -y && \
     wget --quiet --output-document=- https://dl-ssl.google.com/linux/linux_signing_key.pub | gpg --dearmor > /etc/apt/trusted.gpg.d/google-archive.gpg && \
     sh -c 'echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list' && \
     apt-get update && \
     apt-get install google-chrome-stable -y
RUN chown -R nobody:nogroup /usr/local/tomcat && mkdir /nonexistent && chown -R nobody:nogroup /nonexistent
USER nobody
ENV JAVA_OPTS "\
 -Dcom.sun.management.jmxremote.local.only=false\
 -Dcom.sun.management.jmxremote.port=9000\
 -Dcom.sun.management.jmxremote.rmi.port=9000\
 -Dcom.sun.management.jmxremote.authenticate=false\
 -Dcom.sun.management.jmxremote.ssl=false\
 -Dcom.sun.management.jmxremote.ssl.need.client.auth=false\
 -Dcom.sun.management.jmxremote.registry.ssl=false\
 -Dcom.sun.management.jmxremote.authenticate=false\
 -Dcom.sun.management.jmxremote=true\
 -Dcom.sun.management.jmxremote.host=0.0.0.0\
 -Dactivemq.brokerURL=tcp://localhost:61616 -Dactivemq.username=admin -Dactivemq.password=admin
 "
EXPOSE 9000
ADD  target/Jaxrs-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/
#COPY  target/Jaxrs-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/Jaxrs-1.0-SNAPSHOT.war
#COPY  target/Jaxrs-1.0-SNAPSHOT.war /opt/tomcat/webapps/
#CMD ["catalina.sh", "run"]


    