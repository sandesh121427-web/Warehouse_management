# Java Code Assignment

This is a short code assignment that explores various aspects of software development, including API implementation, documentation, persistence layer handling, and testing.

## About the assignment

You will find the tasks of this assignment on [CODE_ASSIGNMENT](CODE_ASSIGNMENT.md) file

## About the code base

This is based on https://github.com/quarkusio/quarkus-quickstarts

### Requirements

To compile and run this demo you will need:

- JDK 17+

For local development and automated tests, the project uses an in-memory H2 database, so Docker is not required. The production profile is configured for PostgreSQL.

### Configuring JDK 17+

Make sure that `JAVA_HOME` environment variables has been set, and that a JDK 17+ `java` command is on the path.

## Building the demo

From the `java-assignment` directory, execute the Maven build:

```sh
./mvnw package
```

## Running the demo

### Live coding with Quarkus

The Maven Quarkus plugin provides a development mode that supports
live coding. To try this out:

```sh
./mvnw quarkus:dev
```

In this mode you can make changes to the code and have the changes immediately applied, by just refreshing your browser.

    Hot reload works even when modifying your JPA entities.
    Try it! Even the database schema will be updated on the fly.

## (Optional) Run Quarkus in JVM mode

When you're done iterating in developer mode, you can run the application as a conventional jar file.

First compile it:

```sh
./mvnw package
```

Local development and tests use an in-memory H2 database. For a production-like run, configure PostgreSQL using the `%prod` settings in `src/main/resources/application.properties`.

```sh
docker run -it --rm=true --name quarkus_test -e POSTGRES_USER=quarkus_test -e POSTGRES_PASSWORD=quarkus_test -e POSTGRES_DB=quarkus_test -p 15432:5432 postgres:13.3
```

Connection properties for the Agroal datasource are defined in the standard Quarkus configuration file,
`src/main/resources/application.properties`.

Then run it:

```sh
java -jar ./target/quarkus-app/quarkus-run.jar
```
    Have a look at how fast it boots.
    Or measure total native memory consumption...


## See the demo in your browser

Navigate to:

<http://localhost:8080/index.html>

Have fun, and join the team of contributors!

## Troubleshooting

## CI pipeline

GitHub Actions runs `./mvnw --batch-mode --no-transfer-progress verify` on pushes and pull requests. The job uses Temurin Java 17 and Maven dependency caching. The test profile uses in-memory H2, so the workflow does not need Docker.

## Warehouse API behavior

- `GET /warehouse` lists active warehouses.
- `POST /warehouse` creates a warehouse after business-rule validation.
- `GET /warehouse/{id}` retrieves an active warehouse by database ID.
- `DELETE /warehouse/{id}` archives it (history is retained).
- `POST /warehouse/{businessUnitCode}/replacement` archives the active instance and creates its replacement using the same business-unit code.

Run unit and application tests with `./mvnw test`. Run the full Maven verification lifecycle with `./mvnw verify`.
