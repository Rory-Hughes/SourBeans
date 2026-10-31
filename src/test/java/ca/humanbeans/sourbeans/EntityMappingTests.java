package ca.humanbeans.sourbeans;

import static org.junit.jupiter.api.Assertions.*;

import ca.humanbeans.sourbeans.entity.Issue;
import ca.humanbeans.sourbeans.entity.IssuePriority;
import ca.humanbeans.sourbeans.entity.IssueStatus;
import ca.humanbeans.sourbeans.entity.Project;
import jakarta.persistence.EntityManager;
import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests that the Project and Issue entities (a) carry the agreed validation
 * rules and (b) map correctly onto the tables in schema.sql. They use the
 * EntityManager directly, so they do not need a repository or a service.
 *
 * Running with ddl-auto=validate, the application context only starts if the
 * mappings already match the schema, so a failure of every test in this class
 * usually means a mapping mismatch (column name, column type, enum type).
 *
 * ASSUMED API (adjust if different):
 *   Project  setProjectName, setProjectDescription, setIssues, getIssues,
 *            getProjectId, getProjectName, getProjectDescription,
 *            getProjectCreationTime
 *   Issue    setProject, setIssueTitle, setIssueDescription, setIssueStatus,
 *            setIssuePriority, getIssueTitle, getIssueStatus, getIssuePriority
 *   IssueStatus  TODO, IN_PROGRESS, DONE
 *   IssuePriority  UNDEFINED, LOW, MEDIUM, HIGH
 * Requires spring-boot-starter-validation (jakarta.validation) on the
 * classpath.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional // every test rolls back, so sourbeans_test stays clean
class EntityMappingTests {

    @Autowired EntityManager entityManager;
    @Autowired JdbcTemplate jdbc;
    @Autowired Validator validator;

    // ---------- helpers ----------

    private Project newProject(String name, String description) {
        Project project = new Project();
        project.setProjectName(name);
        project.setProjectDescription(description);
        project.setIssues(new ArrayList<>());
        return project;
    }

    private Project persistProject(String name, String description) {
        Project project = newProject(name, description);
        entityManager.persist(project);
        return project;
    }

    private Issue newIssue(Project project, String title) {
        Issue issue = new Issue();
        issue.setProject(project);
        issue.setIssueTitle(title);
        issue.setIssueStatus(IssueStatus.TODO);
        issue.setIssuePriority(IssuePriority.MEDIUM);
        return issue;
    }

    private Issue persistIssue(Project project, String title) {
        Issue issue = newIssue(project, title);
        entityManager.persist(issue);
        project.getIssues().add(issue);
        return issue;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private boolean hasViolationOn(Project project, String property) {
        return validator.validate(project).stream().anyMatch(
            v -> v.getPropertyPath().toString().equals(property));
    }

    private int count(String table) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table,
                                   Integer.class);
    }

    // ---------- test data ----------

    static Stream<String> invalidNames() {
        return Stream.of(null, "", "     ", "a", "ab", "a".repeat(81));
    }

    static Stream<String> validNames() {
        return Stream.of("abc", "abcd", "a".repeat(79), "a".repeat(80));
    }

    // ---------- Project validation rules ----------

    @Test
    void validProjectHasNoViolations() {
        assertTrue(
            validator.validate(newProject("Website", "Rebuild")).isEmpty());
    }

    @ParameterizedTest(name = "[{index}]")
    @MethodSource("invalidNames")
    void invalidProjectNamesBreakTheNameRule(String name) {
        assertTrue(hasViolationOn(newProject(name, null), "projectName"));
    }

    @ParameterizedTest(name = "[{index}]")
    @MethodSource("validNames")
    void validProjectNamesPassTheNameRule(String name) {
        assertFalse(hasViolationOn(newProject(name, null), "projectName"));
    }

    @Test
    void projectDescriptionIsOptional() {
        assertFalse(
            hasViolationOn(newProject("Website", null), "projectDescription"));
    }

    @Test
    void projectDescriptionMaxIs1000Characters() {
        assertFalse(hasViolationOn(newProject("Website", "d".repeat(1000)),
                                   "projectDescription"));
        assertTrue(hasViolationOn(newProject("Website", "d".repeat(1001)),
                                  "projectDescription"));
    }

    // ---------- Project mapping ----------

    @Test
    void bothEntitiesAreKnownToTheEntityManager() {
        assertDoesNotThrow(
            () -> entityManager.getMetamodel().entity(Project.class));
        assertDoesNotThrow(
            () -> entityManager.getMetamodel().entity(Issue.class));
    }

    @Test
    void persistingAProjectGeneratesAnId() {
        Project project = persistProject("Website", null);
        entityManager.flush();

        assertTrue(project.getProjectId() > 0);
    }

    @Test
    void projectsGetDifferentIds() {
        Project first = persistProject("Alpha", null);
        Project second = persistProject("Bravo", null);
        entityManager.flush();

        assertNotEquals(first.getProjectId(), second.getProjectId());
    }

    @Test
    void projectFieldsSurviveARoundTrip() {
        Project project = persistProject("Website", "Rebuild the site");
        long id = project.getProjectId();
        flushAndClear();

        Project found = entityManager.find(Project.class, id);
        assertNotNull(found);
        assertEquals("Website", found.getProjectName());
        assertEquals("Rebuild the site", found.getProjectDescription());
    }

    @Test
    void missingDescriptionRoundTripsAsNull() {
        long id = persistProject("NoDesc", null).getProjectId();
        flushAndClear();

        assertNull(
            entityManager.find(Project.class, id).getProjectDescription());
    }

    @Test
    void projectIsStoredInTheProjectsTable() {
        long id = persistProject("Stored", "In the table").getProjectId();
        entityManager.flush();

        assertEquals("Stored", jdbc.queryForObject(
                                   "SELECT name FROM projects WHERE id = ?",
                                   String.class, id));
        assertEquals(
            "In the table",
            jdbc.queryForObject("SELECT description FROM projects WHERE id = ?",
                                String.class, id));
    }

    @Test
    void projectCreationTimeIsFilledByTheDatabase() {
        long id = persistProject("Timestamped", null).getProjectId();
        flushAndClear();

        assertNotNull(
            entityManager.find(Project.class, id).getProjectCreationTime());
    }

    @Test
    void specialCharactersSurviveARoundTrip() {
        String name = "Caf\u00e9 & Co. \u2014 v2.0";
        long id =
            persistProject(name, "Cr\u00e8me br\u00fbl\u00e9e").getProjectId();
        flushAndClear();

        Project found = entityManager.find(Project.class, id);
        assertEquals(name, found.getProjectName());
        assertEquals("Cr\u00e8me br\u00fbl\u00e9e",
                     found.getProjectDescription());
    }

    @Test
    void projectWithoutANameCannotBeStored() {
        assertThrows(Exception.class, () -> {
            entityManager.persist(newProject(null, "No name"));
            entityManager.flush();
        });
    }

    // ---------- Issue mapping ----------

    @Test
    void issueIsStoredWithItsProjectId() {
        Project project = persistProject("Tracker", null);
        persistIssue(project, "Fix login bug");
        entityManager.flush();

        Long storedProjectId =
            jdbc.queryForObject("SELECT project_id FROM issues WHERE title = ?",
                                Long.class, "Fix login bug");
        assertEquals(project.getProjectId(), storedProjectId.longValue());
    }

    @Test
    void issueStatusAndPriorityAreStoredAsText() {
        Project project = persistProject("Enums", null);
        Issue issue = newIssue(project, "Urgent thing");
        issue.setIssueStatus(IssueStatus.IN_PROGRESS);
        issue.setIssuePriority(IssuePriority.HIGH);
        entityManager.persist(issue);
        entityManager.flush();

        var row = jdbc.queryForMap(
            "SELECT status, priority FROM issues WHERE title = ?",
            "Urgent thing");
        assertEquals("IN_PROGRESS", row.get("status"));
        assertEquals("HIGH", row.get("priority"));
    }

    @Test
    void issueStatusAndPriorityComeBackAsEnums() {
        Project project = persistProject("EnumsBack", null);
        Issue issue = newIssue(project, "Round trip");
        issue.setIssueStatus(IssueStatus.DONE);
        issue.setIssuePriority(IssuePriority.LOW);
        entityManager.persist(issue);
        project.getIssues().add(issue);
        long id = project.getProjectId();
        flushAndClear();

        Issue found = entityManager.find(Project.class, id).getIssues().get(0);
        assertEquals(IssueStatus.DONE, found.getIssueStatus());
        assertEquals(IssuePriority.LOW, found.getIssuePriority());
    }

    @Test
    void issueDescriptionIsOptional() {
        Project project = persistProject("OptionalDesc", null);
        Issue issue = newIssue(project, "No description");
        issue.setIssueDescription(null);

        assertDoesNotThrow(() -> {
            entityManager.persist(issue);
            entityManager.flush();
        });
    }

    @Test
    void issueCreationTimeIsFilledByTheDatabase() {
        Project project = persistProject("IssueTime", null);
        persistIssue(project, "Has timestamp");
        entityManager.flush();

        assertNotNull(
            jdbc.queryForObject("SELECT created_at FROM issues WHERE title = ?",
                                Object.class, "Has timestamp"));
    }

    @Test
    void issueWithoutAProjectCannotBeStored() {
        assertThrows(Exception.class, () -> {
            entityManager.persist(newIssue(null, "Orphan issue"));
            entityManager.flush();
        });
    }

    @Test
    void issueWithoutATitleCannotBeStored() {
        Project project = persistProject("TitleRule", null);

        assertThrows(Exception.class, () -> {
            entityManager.persist(newIssue(project, null));
            entityManager.flush();
        });
    }

    // ---------- Project to Issue relationship ----------

    @Test
    void projectLoadsItsIssues() {
        Project project = persistProject("Busy", null);
        persistIssue(project, "Issue number 1");
        persistIssue(project, "Issue number 2");
        long id = project.getProjectId();
        flushAndClear();

        Project found = entityManager.find(Project.class, id);
        assertEquals(2, found.getIssues().size());
    }

    @Test
    void projectWithNoIssuesHasAnEmptyList() {
        long id = persistProject("Quiet", null).getProjectId();
        flushAndClear();

        assertTrue(entityManager.find(Project.class, id).getIssues().isEmpty());
    }

    @Test
    void issuesBelongToTheirOwnProjectOnly() {
        Project first = persistProject("First", null);
        Project second = persistProject("Second", null);
        persistIssue(first, "Belongs to first");
        persistIssue(second, "Belongs to second");
        long firstId = first.getProjectId();
        flushAndClear();

        Project found = entityManager.find(Project.class, firstId);
        assertEquals(1, found.getIssues().size());
        assertEquals("Belongs to first",
                     found.getIssues().get(0).getIssueTitle());
    }

    @Test
    void removingAnIssueFromTheListDeletesItButKeepsTheProject() {
        Project project = persistProject("Parent", null);
        persistIssue(project, "Child number 1");
        persistIssue(project, "Child number 2");
        long id = project.getProjectId();
        flushAndClear();

        Project found = entityManager.find(Project.class, id);
        found.getIssues().remove(0);
        flushAndClear();

        assertEquals(1, count("issues"));
        assertEquals(1, count("projects"));
    }

    @Test
    void removingAProjectRemovesItsIssues() {
        Project project = persistProject("Doomed", null);
        persistIssue(project, "Goes away 1");
        persistIssue(project, "Goes away 2");
        Project survivor = persistProject("Survivor", null);
        persistIssue(survivor, "Stays put");
        long id = project.getProjectId();
        flushAndClear();

        entityManager.remove(entityManager.find(Project.class, id));
        flushAndClear();

        assertEquals(1, count("projects"));
        assertEquals(1, count("issues"));
    }
}
