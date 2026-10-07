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
    restart: always
    environment:
      POSTGRES_USER: "${POSTGRES_USER:?Set POSTGRES_USER}"
      POSTGRES_PASSWORD: "${POSTGRES_PASSWORD:?Set POSTGRES_PASSWORD}"
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
    environment:
      SPRING_DATASOURCE_URL: "jdbc:postgresql://db:5432/webge"
      SPRING_DATASOURCE_USERNAME: "${POSTGRES_USER:?Set POSTGRES_USER}"
      SPRING_DATASOURCE_PASSWORD: "${POSTGRES_PASSWORD:?Set POSTGRES_PASSWORD}"
      SPRING_FLYWAY_URL: "jdbc:postgresql://db:5432/webge"
      SPRING_FLYWAY_USER: "${POSTGRES_USER:?Set POSTGRES_USER}"
      SPRING_FLYWAY_PASSWORD: "${POSTGRES_PASSWORD:?Set POSTGRES_PASSWORD}"
      SPRING_RABBITMQ_HOST: rabbitmq
      SPRING_RABBITMQ_LISTENER_SIMPLE_CONCURRENCY: "2"
      WEBGE_ADMIN_PASSWORD: "${WEBGE_ADMIN_PASSWORD:?Set WEBGE_ADMIN_PASSWORD}"
    networks:
      - backend
  rabbitmq:
    image: rabbitmq:management
    networks:
      - backend
volumes:
  pgdata:

networks:
  backend:
```

Set `POSTGRES_USER`, `POSTGRES_PASSWORD`, and `WEBGE_ADMIN_PASSWORD` in the deployment environment before starting Compose. The Compose service configures both JDBC URLs to `jdbc:postgresql://db:5432/webge`; the administrator password must contain at least 12 characters. Existing databases must have unique usernames and email addresses before applying the migration. Database and RabbitMQ ports remain private to the Compose network; only the WebGE HTTP port is published.

Notice that the variable SPRING_RABBITMQ_LISTENER_SIMPLE_CONCURRENCY determines the number of concurrent runs in the server. It is set to 2 by default, but it can be modified to a proper value depending on the computation power of the machine.

WebGE runs in port 8182 by default. On first startup, the seeded administrator account is enabled with the password supplied through `WEBGE_ADMIN_PASSWORD`; the insecure `admin/admin` default is no longer available.

Enjoy WebGE!
