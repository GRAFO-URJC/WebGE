FROM java:8
EXPOSE 8182
ADD "https://github.com/GRAFO-URJC/WebGE/raw/lei-develop-branch/target/gramev-0.0.1-SNAPSHOT.jar" gramev.jar
ENTRYPOINT ["java","-jar","gramev.jar"]