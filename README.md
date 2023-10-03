# WebGE

Grammatical Evolution (GE) is a metaheuristic from the Genetic Programming family which allows to evolve individuals represented as chromosomes expressing their phenotypes by means of a grammar.

More info about this project can be found in https://grafo-urjc.github.io/WebGE/


## Running WebGE

The easiest way to run WebGE is, after installing docker and docker-compose, creating a docker-compose.yml file as the following, and run the corresponding docker commands to pull, and run the compose.

docker-compose.yml:
```
version: '3.2'
services:
  db:
    image: postgres:12-bullseye
    ports:
      - "5432:5432"
    restart: always
    environment:
      POSTGRES_USER: usuario
      POSTGRES_PASSWORD: "01234"
      POSTGRES_DB: "webge"
    volumes:
      - pgdata:/var/lib/postgresql/data
    networks:
      - backend

  gramev:
    image: jmcolmenar/webge
    depends_on:
      - db
      - rabbitmq
    ports:
      - "8182:8182"
      - "5005:5005"
      - "9010:9010"
    environment:
      SPRING_DATASOURCE_URL: "jdbc:postgresql://db:5432/webge"
      SPRING_DATASOURCE_USERNAME: "usuario"
      SPRING_DATASOURCE_PASSWORD: "01234"
      SPRING_FLYWAY_URL: "jdbc:postgresql://db:5432/webge"
      SPRING_FLYWAY_USER: "usuario"
      SPRING_FLYWAY_PASSWORD: "01234"
      SPRING_RABBITMQ_HOST: rabbitmq
      SPRING_RABBITMQ_LISTENER_SIMPLE_CONCURRENCY: "2"
    networks:
      - backend
  rabbitmq:
    image: rabbitmq:management
    ports:
      - "5672:5672"
      - "15672:15672"
    networks:
      - backend
volumes:
  pgdata:

networks:
  backend:
```

Notice that the variable SPRING_RABBITMQ_LISTENER_SIMPLE_CONCURRENCY determines the number of concurrent runs in the server. It is set to 2 by default, but it can be modified to a proper value depending on the computation power of the machine.

WebGE runs in port 8182 by default, and provides an administrator to create users whose credentials are admin/admin as user/password.

Enjoy WebGE!
