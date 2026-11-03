# Local development

Use Java 21 or 25 (Maven targets release 21), Spring Boot 4.1.2, and a local database. Rory uses MySQL 8.0.43 on **port 3307**. Josh uses MariaDB on Arch Linux; follow the [Arch installation guide](https://wiki.archlinux.org/title/MariaDB). Both drivers are included; the JDBC URL selects the driver and Hibernate detects the dialect. Josh's environment still needs verification.

## Provision the databases once

1. Copy `SQL/setup.sql` to the git-ignored `local-setup.sql` in the repository root.
2. Replace `change-me` in that local copy with your own application-user password. Never put a real password in the tracked original.
3. Open an administrator client from the repository root. For Rory:

    ```powershell
    & 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe' --host=localhost --port=3307 --user=root --password
    ```

    On Arch, socket administrator authentication may use `sudo mariadb`.

4. Run `SOURCE local-setup.sql;` in the database client. It creates `sourbeans` and `sourbeans_test` with explicit charset/collation and privileges restricted to those databases. Existing users keep their current password.
5. Delete your local SQL copy afterward. Review and back up useful existing data before deliberately rebuilding any database.

## Configure credentials

Copy `local.properties.example` to `local.properties` in the repository root. For Rory:

```properties
spring.datasource.url=jdbc:mysql://localhost:3307/sourbeans
spring.datasource.username=sourbeans_app
spring.datasource.password=YOUR_LOCAL_PASSWORD
```

For Josh use `jdbc:mariadb://localhost:3307/sourbeans` and his own credentials. Escape literal backslashes in Java properties values with another backslash. The ignored file is imported automatically. `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` environment variables override it. Keep credentials out of commits and chat.

The database URL is required in your local configuration; `application.properties` does not hard-code a MySQL URL. This project uses `local.properties` as its ignored environment configuration file. A `.env` file is not loaded automatically.

## Build and run

Tests use the same credentials with a dedicated test URL. Their explicit test property source overrides ordinary development configuration, including `SPRING_DATASOURCE_URL`. Always point `SOURBEANS_TEST_DATASOURCE_URL` at `sourbeans_test`.

PowerShell for Rory:

```powershell
$env:SOURBEANS_TEST_DATASOURCE_URL = 'jdbc:mysql://localhost:3307/sourbeans_test'
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

Bash for Josh:

```bash
export SOURBEANS_TEST_DATASOURCE_URL='jdbc:mariadb://localhost:3307/sourbeans_test'
./mvnw clean verify
./mvnw spring-boot:run
```

In IntelliJ, use the repository root as the working directory and set the test URL variable in Maven Runner or the JUnit run configuration. Select Java 21 or 25 as runner JDK. Terminal wrapper invocations separately use `JAVA_HOME`.

The transactional integration test verifies a live connection, the test database name, both schema tables, and a JPA native query. No H2 or containers are used. Startup should initialize JPA and listen on localhost:8080; a 404 is expected until controllers exist.

## Schema and failure checks

Per [ADR-0002](decisions/0002-db-schema-management.md), `src/main/resources/schema.sql` is now the canonical schema, moved from `SQL/schema.sql`. Spring runs it before JPA. Tables use `CREATE TABLE IF NOT EXISTS`; Hibernate uses `ddl-auto=validate`. Entity mapping validation becomes meaningful as later issues add entities. Record manual table changes in [schema-changes.md](schema-changes.md).

Hibernate uses `PhysicalNamingStrategyStandardImpl` to preserve table and column names as mapped, including their casing. Use explicit mappings that match the canonical schema when adding entities, including on case-sensitive MariaDB installations.

Check startup against a confirmed unused port without stopping a database service:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.datasource.url=jdbc:mysql://127.0.0.1:1/sourbeans"
```

Expect a connection error and nonzero result. An unknown database requires provisioning; access denied requires checking credentials and grants. Keep the database and unauthenticated app local. If Windows clean fails, stop running applications and check generated folders for a read-only attribute (the cause of an earlier local failure).

References: [Spring initialization](https://docs.spring.io/spring-boot/how-to/data-initialization.html), [external configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html), [MariaDB Connector/J](https://mariadb.com/docs/connectors/mariadb-connector-j/about-mariadb-connector-j).
