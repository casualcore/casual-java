FROM quay.io/wildfly/wildfly:32.0.1.Final-jdk21

USER 1001

RUN mkdir /opt/jboss/wildfly/casual

ENV CASUAL_VERSION 3.2.47
ENV GSON_VERSION 2.11.0
ENV OBJENESIS_VERSION 3.4

RUN wget -P /opt/jboss/wildfly/casual https://repo1.maven.org/maven2/com/google/code/gson/gson/$GSON_VERSION/gson-$GSON_VERSION.jar
RUN wget -P /opt/jboss/wildfly/casual https://repo1.maven.org/maven2/org/objenesis/objenesis/$OBJENESIS_VERSION/objenesis-$OBJENESIS_VERSION.jar

ENV CASUAL_ROOT /opt/jboss/wildfly/casual

COPY dist/*.jar $CASUAL_ROOT
COPY dist/casual-jca-app-*.ear $CASUAL_ROOT

RUN /opt/jboss/wildfly/bin/jboss-cli.sh --command="module add --name=se.laz.casual \
	--resources=/opt/jboss/wildfly/casual/casual-inbound-handler-api-${CASUAL_VERSION}.jar:/opt/jboss/wildfly/casual/casual-fielded-annotations-${CASUAL_VERSION}.jar:/opt/jboss/wildfly/casual/casual-service-discovery-extension-${CASUAL_VERSION}.jar:/opt/jboss/wildfly/casual/casual-api-${CASUAL_VERSION}.jar:/opt/jboss/wildfly/casual/casual-event-api-${CASUAL_VERSION}.jar:/opt/jboss/wildfly/casual/gson-${GSON_VERSION}.jar:/opt/jboss/wildfly/casual/objenesis-${OBJENESIS_VERSION}.jar \
                --dependencies=javaee.api,sun.jdk"

ENV WILDFLY_HOME=/opt/jboss/wildfly

COPY files/setup.cli /tmp
RUN ${WILDFLY_HOME}/bin/jboss-cli.sh --file=/tmp/setup.cli
RUN rm -rf /opt/jboss/wildfly/standalone/configuration/standalone_xml_history/current

USER root

RUN chgrp -R 0 /opt && \
    chmod -R g=u /opt

USER 1001

EXPOSE 8080 7772 9990

CMD ["/opt/jboss/wildfly/bin/standalone.sh",  "-c", "standalone-full.xml", "--debug", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]