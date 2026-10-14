# Fulfilment Warehouse Assignment

This repository contains the Java implementation and written answers for the warehouse-management code assignment.

## Project

- **Application:** `java-assignment/`
- **Framework:** Quarkus 3.13.3, REST/JAX-RS, Hibernate ORM with Panache
- **Runtime:** JDK 17 or newer
- **Build:** Maven Wrapper
- **Database:** H2 for local development/tests; PostgreSQL for the production profile
- **CI:** `.github/workflows/ci.yml`

## Run and test

Open a terminal in `java-assignment` and run:

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
```

On Windows PowerShell:

```powershell
.\mvnw.cmd --batch-mode --no-transfer-progress clean verify
```

For local development:

```bash
./mvnw quarkus:dev
```

See [`java-assignment/README.md`](java-assignment/README.md) for database setup and API examples. The implementation tasks are in [`CODE_ASSIGNMENT.md`](java-assignment/CODE_ASSIGNMENT.md), design answers are in [`QUESTIONS.md`](java-assignment/QUESTIONS.md), and the five business case scenarios are completed in [`case-study/CASE_STUDY.md`](case-study/CASE_STUDY.md).

## Main behavior

- Resolves supported locations by identifier.
- Creates, retrieves, replaces and archives warehouses with business-rule validation.
- Keeps the archived warehouse row when a replacement reuses its Business Unit Code.
- Synchronizes Store create/update/patch to the legacy gateway only after a successful transaction commit.
- Enforces the optional warehouse/store/product fulfillment limits.

## Test status for this delivered archive

The source and test Java files were parsed with the JDK compiler parser, the Maven POM was checked as valid XML, and isolated warehouse-domain smoke checks passed. A full Maven `verify` was attempted, but this environment could not resolve `repo.maven.apache.org` to download Maven, so the Quarkus integration-test suite could not be executed here. Run the command above locally or let GitHub Actions execute it in an environment with Maven Central access.
