FROM openjdk:17
EXPOSE 8182
COPY "target/gramev-0.0.1-SNAPSHOT.jar" gramev.jar
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005","-Dcom.sun.management.jmxremote=true", \
                                                                                           "-Dcom.sun.management.jmxremote.port=9010", \
                                                                                           "-Dcom.sun.management.jmxremote.local.only=false", \
                                                                                           "-Dcom.sun.management.jmxremote.authenticate=false", \
                                                                                           "-Dcom.sun.management.jmxremote.ssl=false", \
                                                                                           "-Dcom.sun.management.jmxremote.rmi.port=9010", \
                                                                                        "-Djava.rmi.server.hostname=localhost","-jar","gramev.jar"]