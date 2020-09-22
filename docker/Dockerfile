FROM openjdk:11
EXPOSE 8182
COPY "target/gramev-0.0.1-SNAPSHOT.jar" gramev.jar
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar","gramev.jar"]
