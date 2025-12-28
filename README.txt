
> cd /path/to/lmsbooks

Dockerfile
> mvn package
> docker build -t lmsbooks .

DockerfileWithPackaging (inclui mvn package)
> docker build -f DockerfileWithPackaging -t lmsbooks .

Running:
> docker compose -f docker-compose-rabbitmq+postgres.yml up -d
> docker exec -it postgres_in_lms_network psql -U postgres
    psql (16.3 (Debian 16.3-1.pgdg120+1))
    Type "help" for help.

    postgres=# create database books_1;
    CREATE DATABASE
    postgres=# create database books_1;
    CREATE DATABASE
    postgres=# \l
                                                      List of databases
       Name    |  Owner   | Encoding | Locale Provider |  Collate   |   Ctype    | ICU Locale | ICU Rules |   Access privileges
    -----------+----------+----------+-----------------+------------+------------+------------+-----------+-----------------------
     books_1   | postgres | UTF8     | libc            | en_US.utf8 | en_US.utf8 |            |           |
     books_2   | postgres | UTF8     | libc            | en_US.utf8 | en_US.utf8 |            |           |
     books_3   | postgres | UTF8     | libc            | en_US.utf8 | en_US.utf8 |            |           |
     postgres  | postgres | UTF8     | libc            | en_US.utf8 | en_US.utf8 |            |           |
     template0 | postgres | UTF8     | libc            | en_US.utf8 | en_US.utf8 |            |           | =c/postgres          +
               |          |          |                 |            |            |            |           | postgres=CTc/postgres
     template1 | postgres | UTF8     | libc            | en_US.utf8 | en_US.utf8 |            |           | =c/postgres          +
               |          |          |                 |            |            |            |           | postgres=CTc/postgres
     users_1   | postgres | UTF8     | libc            | en_US.utf8 | en_US.utf8 |            |           |
    (7 rows)
    postgres=# exit
> docker compose up



# LMS Books Service
## ArqSoft Requirements
#### Alignment with business requirements
The LMS Books Service is designed to manage book-related operations in a Library Management System (LMS). It provides functionalities such as adding new books, updating book information, deleting books, and retrieving book details. The service aligns with the business requirements of efficiently managing the library's book inventory and ensuring data consistency across multiple instances.
#### ADD-driven design, visual models & alternatives
The service is designed using ADD-driven design principles, with clear visual models representing the architecture and design decisions. Alternatives were considered and documented in the design documentation.
#### Deployment Scalability: Multiple instances per service (VMs or containers)
Using docker-compose, I have created three instances of the books service (books_1, books_2) to demonstrate deployment scalability.
#### Strangler fig
Available in the documentation of the LMS system.
#### Command-Query Responsibility Segregation (CQRS)
We can see it in the code structure, commands and queries are separated.
#### Database-per-Service
Each instance of the service uses its own database (books_1, books_2) to ensure data isolation and independence.
#### Polyglot persistence
I am using PostgreSQL as the database for this service. Try to use cassandra (Non Relational) in the command instance, but my machine cannot handle all docker instances.
#### Messaging through Message Broker (e.g. RabbitMQ)
The service uses RabbitMQ as the message broker for communication between services.
#### Outbox
The service implements the Outbox pattern to ensure reliable message delivery.
When a book is created or updated, the corresponding event is stored in an outbox table within the same transaction as the database operation.
A separate process reads from the outbox table and publishes the events to RabbitMQ.
#### Domain Events
The service publishes domain events to RabbitMQ when significant actions occur, such as when a book is created or updated.
#### Saga
To simplify the implementation, I used a simple local saga pattern using only the books service. In a real-world scenario, the saga would involve multiple services.
#### Change Data Capture (CDC)
The service uses Debezium to capture changes in the PostgreSQL database and publish them to Rabbit
#### Performance/load testing


## OdSoft Requirements