package ca.humanbeans.sourbeans;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional // every test rolls back, so sourbeans_test stays clean
class SchemaBehaviourTests {

    @Autowired JdbcTemplate jdbc;
    @Autowired DataSource dataSource;

    // ---------- helpers ----------

    private long insertProject(String name, String description) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement(
                "INSERT INTO projects (name, description) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setString(2, description);
            return ps;
        }, keys);
        return keys.getKey().longValue();
    }

    private long insertIssue(long projectId, String title) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement(
                "INSERT INTO issues (project_id, title) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, projectId);
            ps.setString(2, title);
            return ps;
        }, keys);
        return keys.getKey().longValue();
    }

    private int count(String table) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table,
                                   Integer.class);
    }

    // ---------- projects ----------

    @Test
    void canInsertAndReadProject() {
        long id = insertProject("Website", "Rebuild the site");

        var row = jdbc.queryForMap(
            "SELECT name, description FROM projects WHERE id = ?", id);
        assertEquals("Website", row.get("name"));
        assertEquals("Rebuild the site", row.get("description"));
    }

    @Test
    void projectDescriptionIsOptional() {
        long id = insertProject("NoDesc", null);

        String description = jdbc.queryForObject(
            "SELECT description FROM projects WHERE id = ?", String.class, id);
        assertNull(description);
    }

    @Test
    void projectCreatedAtIsFilledAutomatically() {
        long id = insertProject("Timestamped", null);

        Object createdAt = jdbc.queryForObject(
            "SELECT created_at FROM projects WHERE id = ?", Object.class, id);
        assertNotNull(createdAt);
    }

    @Test
    void projectIdsAutoIncrement() {
        long first = insertProject("Alpha", null);
        long second = insertProject("Bravo", null);
        assertTrue(second > first);
    }

    @Test
    void projectNameIsRequired() {
        assertThrows(DataAccessException.class, () -> insertProject(null, "x"));
    }

    @Test
    void projectNameMustBeAtLeast3Characters() {
        assertThrows(DataAccessException.class,
                     () -> insertProject("ab", null));
        assertDoesNotThrow(() -> insertProject("abc", null));
    }

    @Test
    void projectNameMaxIs80Characters() {
        assertDoesNotThrow(() -> insertProject("a".repeat(80), null));
        assertThrows(DataAccessException.class,
                     () -> insertProject("a".repeat(81), null));
    }

    @Test
    void projectDescriptionMaxIs1000Characters() {
        assertDoesNotThrow(() -> insertProject("Long1000", "d".repeat(1000)));
        assertThrows(DataAccessException.class,
                     () -> insertProject("Long1001", "d".repeat(1001)));
    }

    // ---------- issues ----------

    @Test
    void canInsertIssueForProject() {
        long projectId = insertProject("Tracker", null);
        long issueId = insertIssue(projectId, "Fix login bug");

        var row = jdbc.queryForMap(
            "SELECT project_id, title FROM issues WHERE id = ?", issueId);
        assertEquals(projectId, ((Number)row.get("project_id")).longValue());
        assertEquals("Fix login bug", row.get("title"));
    }

    @Test
    void issueDefaultsToOpenStatusAndMediumPriority() {
        long issueId =
            insertIssue(insertProject("Defaults", null), "Check defaults");

        var row = jdbc.queryForMap(
            "SELECT status, priority FROM issues WHERE id = ?", issueId);
        assertEquals("OPEN", row.get("status"));
        assertEquals("MEDIUM", row.get("priority"));
    }

    @Test
    void issueStatusAndPriorityCanBeSet() {
        long projectId = insertProject("Explicit", null);
        jdbc.update("INSERT INTO issues (project_id, title, status, " +
                    "priority) VALUES (?, ?, ?, ?)",
                    projectId, "Urgent thing", "IN_PROGRESS", "HIGH");

        var row = jdbc.queryForMap(
            "SELECT status, priority FROM issues WHERE title = ?",
            "Urgent thing");
        assertEquals("IN_PROGRESS", row.get("status"));
        assertEquals("HIGH", row.get("priority"));
    }

    @Test
    void issueCreatedAtIsFilledAutomatically() {
        long issueId =
            insertIssue(insertProject("IssueTime", null), "Has timestamp");

        Object createdAt =
            jdbc.queryForObject("SELECT created_at FROM issues WHERE id = ?",
                                Object.class, issueId);
        assertNotNull(createdAt);
    }

    @Test
    void issueTitleMustBeAtLeast5Characters() {
        long projectId = insertProject("TitleRule", null);
        assertThrows(DataAccessException.class,
                     () -> insertIssue(projectId, "abcd"));
        assertDoesNotThrow(() -> insertIssue(projectId, "abcde"));
    }

    @Test
    void issueTitleMaxIs120Characters() {
        long projectId = insertProject("TitleMax", null);
        assertDoesNotThrow(() -> insertIssue(projectId, "t".repeat(120)));
        assertThrows(DataAccessException.class,
                     () -> insertIssue(projectId, "t".repeat(121)));
    }

    @Test
    void issueDescriptionIsOptionalAndAllowsUpTo5000Characters() {
        long projectId = insertProject("DescRule", null);
        assertDoesNotThrow(
            ()
                -> jdbc.update("INSERT INTO issues (project_id, title, " +
                               "description) VALUES (?, ?, ?)",
                               projectId, "Big description", "d".repeat(5000)));
        assertDoesNotThrow(() -> insertIssue(projectId, "No description"));
    }

    // ---------- relationships ----------

    @Test
    void issueCannotReferenceMissingProject() {
        assertThrows(DataAccessException.class,
                     () -> insertIssue(999_999_999L, "Orphan issue"));
    }

    @Test
    void issueRequiresAProject() {
        assertThrows(
            DataAccessException.class,
            ()
                -> jdbc.update(
                    "INSERT INTO issues (project_id, title) VALUES (NULL, ?)",
                    "No project"));
    }

    @Test
    void deletingProjectDeletesItsIssuesOnly() {
        long doomed = insertProject("Doomed", null);
        long safe = insertProject("Survivor", null);
        insertIssue(doomed, "Goes away 1");
        insertIssue(doomed, "Goes away 2");
        insertIssue(safe, "Stays put");

        jdbc.update("DELETE FROM projects WHERE id = ?", doomed);

        assertEquals(0, jdbc.queryForObject(
                            "SELECT COUNT(*) FROM issues WHERE project_id = ?",
                            Integer.class, doomed));
        assertEquals(1, jdbc.queryForObject(
                            "SELECT COUNT(*) FROM issues WHERE project_id = ?",
                            Integer.class, safe));
    }

    @Test
    void projectCanHaveManyIssues() {
        long projectId = insertProject("Busy", null);
        for (int i = 0; i < 5; i++)
            insertIssue(projectId, "Issue number " + i);

        assertEquals(5, jdbc.queryForObject(
                            "SELECT COUNT(*) FROM issues WHERE project_id = ?",
                            Integer.class, projectId));
    }

    // ---------- structure ----------

    @Test
    void statusIndexExists() throws Exception {
        boolean found = false;
        try (var con = dataSource.getConnection();
             var rs = con.getMetaData().getIndexInfo(con.getCatalog(), null,
                                                     "issues", false, false)) {
            while (rs.next()) {
                if ("idx_issues_project_status".equalsIgnoreCase(
                        rs.getString("INDEX_NAME")))
                    found = true;
            }
        }
        assertTrue(found, "Missing index idx_issues_project_status");
    }

    @Test
    void testsStartWithCleanTables() {
        // Relies on @Transactional rollback: nothing from other tests should be
        // visible.
        assertEquals(0, count("issues"));
        assertEquals(0, count("projects"));
    }
}
