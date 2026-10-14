# Test and verification notes

## Checks completed in the authoring environment

- Parsed all Java source and test files with the JDK compiler parser: no Java syntax errors.
- Checked `pom.xml` for well-formed XML.
- Confirmed there are no remaining `com.warehouse.api` imports or `UnsupportedOperationException` stubs in main Java sources.
- Compiled and ran an isolated smoke harness against the actual warehouse domain classes. Location lookup, create validations, duplicate business-unit rejection, unknown-location rejection, stock/capacity validation, replacement stock matching, archive-history preservation, and archiving all passed.

## Full Maven verification

`./mvnw --batch-mode --no-transfer-progress verify` was attempted. The Maven Wrapper could not download Maven because DNS resolution for `repo.maven.apache.org` failed with `UnknownHostException`. Therefore, the full Quarkus build and integration tests have **not** been confirmed in this environment. GitHub Actions is configured to run the full verification lifecycle with JDK 17.
