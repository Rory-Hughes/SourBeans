# 06 — Local Development Setup

**Status:** Environment setup guidance for the approved starting point; Spring Boot application code has not yet been committed.  
**Decision:** [ADR-0001](decisions/0001-initial-scope.md) — use a **local MySQL installation** on each developer's computer, rather than a container or a shared development database.

## 1. Install prerequisites on both computers

- Install a compatible **JDK**, **MySQL Server**, and MySQL command-line client. Maven or the Maven Wrapper will be used when the application skeleton is created.
- Together, record compatible Java, Spring Boot, and MySQL versions in the setup issue before generating the skeleton. **No exact version is approved in ADR-0001.**
- Keep the MySQL server on the developer's own machine. Do not expose it on the internet or open unrestricted inbound database ports.
- Verify the tools are available (`java -version`, `mysql --version`). Verify MySQL is running using the operating system's service tools or a successful local client login.

Rory and Josh each complete the same procedure **independently**; database contents and credentials are not shared.

## 2. Create a local database and application user

Using a MySQL administrator account, open the MySQL client (for example, `mysql -u root -p`), then run:

```sql
CREATE DATABASE sourbeans
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER 'sourbeans_app'@'localhost'
  IDENTIFIED BY 'REPLACE_WITH_YOUR_OWN_STRONG_LOCAL_PASSWORD';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES
  ON sourbeans.* TO 'sourbeans_app'@'localhost';
```

The password above is a **placeholder**, not a password to reuse. Each developer chooses a distinct local password. The schema privileges permit early local development; production database permissions and migrations must be reviewed separately. Run creation commands only once per local instance; existing database/user names require a deliberate adjustment rather than blind recreation.

Verify access:

```bash
mysql -h localhost -u sourbeans_app -p sourbeans
```

## 3. Configure Spring locally, without committing secrets

The intended datasource values for each developer are:

| Setting                      | Local value                             |
| ---------------------------- | --------------------------------------- |
| `SPRING_DATASOURCE_URL`      | `jdbc:mysql://localhost:3306/sourbeans` |
| `SPRING_DATASOURCE_USERNAME` | `sourbeans_app`                         |
| `SPRING_DATASOURCE_PASSWORD` | Your **own** local password             |

For a temporary **PowerShell** terminal session:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://localhost:3306/sourbeans"
$env:SPRING_DATASOURCE_USERNAME = "sourbeans_app"
$env:SPRING_DATASOURCE_PASSWORD = "<your-own-local-password>"
```

For a temporary **Bash** terminal session:

```bash
export SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3306/sourbeans'
export SPRING_DATASOURCE_USERNAME='sourbeans_app'
export SPRING_DATASOURCE_PASSWORD='<your-own-local-password>'
```

These values will be read by Spring Boot once the datasource dependency and application configuration exist. Environment settings only apply to the current terminal session unless deliberately persisted.

**Never commit** real credentials, `.env` files, `application-local.properties`/YAML containing secrets, or database dumps with sensitive information. The repository's `.gitignore` already excludes common local configuration files, but review `git status` before committing.

## 4. First integration checkpoint (after the skeleton exists)

1. Both developers can connect to their **own** MySQL database with the application account.
2. Spring Boot starts with the agreed package `ca.humanbeans.sourbeans` and externally supplied datasource values.
3. A simple JPA connectivity/persistence test passes independently on both machines.
4. Tests and launch commands are added to the README **only after they have been run successfully** on the created project.
5. Choose a schema migration / creation strategy together; **ADR-0001 does not prescribe JPA `ddl-auto` or a migration tool**. Do not assume a production-safe schema strategy from these local setup instructions.

Share repeatable **schema definitions or sanitized seed scripts**, when agreed, rather than sharing MySQL data directories or local credentials.

## 5. Security and later deployment

The first MVP is unauthenticated, so the application and its MySQL instance must remain local-only. Ubuntu deployment is a **later milestone**, after browser authentication, JWT API security, authorization checks, and security tests are complete. Production credentials, database accounts, network access, backups, and migration behavior need their own deployment plan.
