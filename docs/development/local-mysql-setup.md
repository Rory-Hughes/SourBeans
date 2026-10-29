# SourBeans — Local MySQL setup

**Decision:** ADR-0001 selects a native local MySQL installation, **not a Docker container**. Rory and Josh each have an independent database on their own computer. No shared MySQL server is required.

**Scope:** Development only. The Spring Boot application and schema migration setup have not been implemented yet.

## 1. Standard settings for both developers

| Setting | Agreed value |
| --- | --- |
| MySQL version | Community Server **8.4 LTS** |
| Installation | Native OS installation |
| Host and protocol | **127.0.0.1**, TCP |
| Port | **3306** (unless already occupied on one machine) |
| Development database | **sourbeans_dev** |
| App database account | **sourbeans_app** |
| Root password | Unique and private on each machine |
| App password | Unique and private on each machine |
| Network exposure | **Localhost only**; no LAN or public MySQL access |

Both developers should stay on the MySQL 8.4 LTS family to minimize environment differences. If either already has an installation containing other projects' databases, do not uninstall it without backing up those databases.

## 2. Install and start MySQL

### Windows

1. Download **MySQL Community Server 8.4 LTS** from https://dev.mysql.com/downloads/mysql/ .
2. Install the Windows MSI and run the included **MySQL Configurator**.
3. Configure TCP/IP on port **3306**, limiting access to the local computer (loopback/127.0.0.1). Do not open a router port or allow remote network access.
4. Create a strong root password, store it securely, and configure the MySQL Windows service.
5. Optionally install **MySQL Workbench** for a graphical client.
6. Confirm the MySQL service is running and that you can connect locally.

Official installation reference: https://dev.mysql.com/doc/refman/8.4/en/windows-installation.html .

### macOS / Linux

Follow the official installation instructions for **MySQL Community Server 8.4 LTS** for that OS. Configure it to listen on **127.0.0.1** and keep it off remote networks. Continue with the steps below using a MySQL administrator account.

## 3. Create the database and non-root application account

Run the following from **MySQL Workbench** or a MySQL administrator session. On Windows, if the mysql CLI is on your PATH, this command prompts for your *root* password:

~~~bash
mysql --protocol=TCP -h 127.0.0.1 -P 3306 -u root -p
~~~

Run these statements **on each developer's machine**, replacing the placeholder with a different private password on each machine:

~~~sql
CREATE DATABASE IF NOT EXISTS sourbeans_dev
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

CREATE USER 'sourbeans_app'@'127.0.0.1'
  IDENTIFIED BY 'REPLACE_WITH_A_UNIQUE_LOCAL_PASSWORD';

GRANT SELECT, INSERT, UPDATE, DELETE,
      CREATE, ALTER, DROP, INDEX, REFERENCES
ON sourbeans_dev.*
TO 'sourbeans_app'@'127.0.0.1';

SHOW GRANTS FOR 'sourbeans_app'@'127.0.0.1';
~~~

**Do not commit a SQL script containing your actual password.** The development privileges above permit early JPA schema work, but are limited to the SourBeans development database, not all schemas. Use separate, narrower credentials for production.

If the account already exists, inspect it instead of blindly dropping and recreating it. Use ALTER USER if you need to reset its local password.

MySQL account and GRANT documentation: https://dev.mysql.com/doc/refman/8.4/en/creating-accounts.html .

## 4. Verify the connection

Using the app account (not root):

~~~bash
mysql --protocol=TCP -h 127.0.0.1 -P 3306 -u sourbeans_app -p sourbeans_dev
~~~

Then run:

~~~sql
SELECT DATABASE(), USER(), CURRENT_USER(), VERSION();
SHOW TABLES;
~~~

Confirm the database is sourbeans_dev and MySQL reports version 8.4.x. An empty table list is expected until the application schema is created.

**MySQL Workbench equivalent:**

| Workbench field | Value |
| --- | --- |
| Connection Method | Standard TCP/IP |
| Hostname | 127.0.0.1 |
| Port | 3306 |
| Username | sourbeans_app |
| Default Schema | sourbeans_dev |
| Password | Your own app-user password, kept private |

If the command-line tool is unavailable, Workbench can execute the same SQL.

## 5. Connect Spring Boot (after scaffolding the app)

When the project skeleton is created, include the **Spring Data JPA** and **MySQL Connector/J** dependencies. Put the following *secret-free* properties into the tracked **src/main/resources/application.properties** file:

~~~properties
spring.application.name=sourbeans
spring.datasource.url=${SOURBEANS_DB_URL:jdbc:mysql://127.0.0.1:3306/sourbeans_dev}
spring.datasource.username=${SOURBEANS_DB_USERNAME}
spring.datasource.password=${SOURBEANS_DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=none
~~~

The placeholders read environment variables from the running Java process. The JDBC URL defaults to the shared local DB, but an individual developer can override it if their port differs. The initial ddl-auto=none setting avoids silently changing the database. Agree on a reproducible SQL migration approach before creating JPA tables, and move to schema validation once migrations exist.

For **IntelliJ IDEA**, set *Run > Edit Configurations > Environment variables* separately on each computer:

- **SOURBEANS_DB_USERNAME** = sourbeans_app
- **SOURBEANS_DB_PASSWORD** = the developer's private app password
- **SOURBEANS_DB_URL** = optional override (only if local host, port, or schema differs)

Alternatively, set variables in the current PowerShell session before running the app:

~~~powershell
$env:SOURBEANS_DB_USERNAME = "sourbeans_app"
$env:SOURBEANS_DB_PASSWORD = "<your own local password>"
~~~

Real passwords typed into a shell may be retained in command history; use private IDE configuration or an approved secrets method where possible. Do not share IntelliJ run configs containing passwords.

**Spring Boot does not automatically load .env files.** The current .gitignore already excludes .env, .env.*, and application-local.properties; keep real secrets out of Git regardless.

## 6. Team conventions

1. **Separate local instances:** Rory and Josh never connect to each other's database for routine development.
2. **Same contract:** Use MySQL 8.4 LTS, database sourbeans_dev, user sourbeans_app, and local TCP.
3. **Private secrets:** Do not use MySQL root for the application. Never commit local credentials, DB backups with personal data, or machine-specific configuration.
4. **Reproducible schema:** Store agreed database schema changes in Git. Choosing a migrations tool remains a separate ADR/action item.
5. **Local-only MVP:** The app has no login in Release 1. Do not expose it or the DB publicly.
6. **Separate production design:** Ubuntu deployment, production provisioning and restricted credentials will be documented when security/deployment work begins.

## 7. Per-developer acceptance checklist

- [ ] MySQL Community Server 8.4 LTS installed and running.
- [ ] Connection to 127.0.0.1:3306 works.
- [ ] Local sourbeans_dev schema exists.
- [ ] Local sourbeans_app account exists with privileges only on sourbeans_dev.
- [ ] Can sign in to the schema with sourbeans_app, without using root.
- [ ] MySQL Workbench or CLI verifies connection and version.
- [ ] Spring Boot reads local environment variables (once scaffolding exists).
- [ ] No passwords or other secrets committed.

## Troubleshooting

| Problem | First checks |
| --- | --- |
| Connection refused | Is MySQL running? Is 127.0.0.1:3306 listening? |
| Access denied | Password and account host (127.0.0.1), not just username |
| Unknown database | Run CREATE DATABASE locally |
| Port 3306 occupied | Identify other services; override only this developer's JDBC URL if needed |
| Spring Boot cannot find credentials | Check environment variables are set on the Java/IntelliJ launch process |
| mysql CLI command missing | Use MySQL Workbench or add the MySQL client executable directory to PATH |

See [ADR-0001](../decisions/0001-initial-scope.md) for the architectural decision.
