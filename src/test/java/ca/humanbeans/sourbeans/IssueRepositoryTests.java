package ca.humanbeans.sourbeans;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import ca.humanbeans.sourbeans.entity.Issue;
import ca.humanbeans.sourbeans.entity.IssuePriority;
import ca.humanbeans.sourbeans.entity.IssueStatus;
import ca.humanbeans.sourbeans.entity.Project;
import ca.humanbeans.sourbeans.repository.IssueRepository;
import ca.humanbeans.sourbeans.repository.ProjectRepository;
import jakarta.persistence.EntityManager;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for IssueRepository (a CrudRepository) against sourbeans_test.
 *
 * REQUIRES: IssueRepository and ProjectRepository must use Long ids
 * (CrudRepository<Issue, Long> and CrudRepository<Project, Long>) and the
 * entities must use Long ids too, to match the BIGINT columns.
 *
 * Title length rules (5 to 120) and the 5000 character description limit are
 * enforced by the database only, because Issue has no validation annotations.
 * Those tests need MySQL 8.0.16+ or MariaDB 10.2.1+ and strict SQL mode. Adding
 * @NotBlank and @Size(min = 5, max = 120) to Issue.issueTitle would give clear
 * Java-side errors as well.
 *
 * Status and priority tests use the enum constants through values(), so they
 * keep working when constants are added or renamed.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional // every test rolls back, so sourbeans_test stays clean
class IssueRepositoryTests {

    @Autowired IssueRepository issueRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManager entityManager;

    // ---------- helpers ----------

    /** Saves a project and returns its id. */
    private long saveProject(String name) {
        Project project = new Project();
        project.setProjectName(name);
        Project saved = projectRepository.save(project);
        entityManager.flush();
        return saved.getProjectId();
    }

    private Issue newIssue(long projectId, String title) {
        Issue issue = new Issue();
        issue.setProject(entityManager.getReference(Project.class, projectId));
        issue.setIssueTitle(title);
        issue.setIssueStatus(IssueStatus.values()[0]);
        issue.setIssuePriority(IssuePriority.values()[0]);
        return issue;
    }

    /** Saves an issue, forces it into the database, and returns its id. */
    private long saveIssue(long projectId, String title) {
        Issue saved = issueRepository.save(newIssue(projectId, title));
        flushAndClear();
        return saved.getIssueId();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * Passes if saving fails. The flush is included because the rules may only
     * be checked when Hibernate writes to the database.
     */
    private void assertSaveRejected(Executable save) {
        assertThrows(Exception.class, () -> {
            save.execute();
            entityManager.flush();
        });
        entityManager.clear();
    }

    private int issueCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM issues",
                                   Integer.class);
    }

    private int projectCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM projects",
                                   Integer.class);
    }

    private String storedTitle(long issueId) {
        return jdbc.queryForObject("SELECT title FROM issues WHERE id = ?",
                                   String.class, issueId);
    }

    // ---------- test data ----------

    static Stream<String> invalidTitles() {
        return Stream.of(null, "", "a", "abcd", "t".repeat(121));
    }

    static Stream<String> validTitles() {
        return Stream.of("abcde", "abcdef", "t".repeat(119), "t".repeat(120));
    }

    static Stream<Long> missingIds() {
        return Stream.of(0L, -1L, 999_999_999L, Long.MAX_VALUE);
    }

    // ---------- save (create) ----------

    @Test
    void saveReturnsTheIssueWithAGeneratedId() {
        long projectId = saveProject("Tracker");

        Issue saved =
            issueRepository.save(newIssue(projectId, "Fix login bug"));

        assertTrue(saved.getIssueId() > 0);
        assertEquals("Fix login bug", saved.getIssueTitle());
    }

    @Test
    void savedIssueIsInTheDatabaseTableWithItsProjectId() {
        long projectId = saveProject("Tracker");
        long issueId = saveIssue(projectId, "Fix login bug");

        assertEquals("Fix login bug", storedTitle(issueId));
        assertEquals(
            projectId,
            jdbc.queryForObject("SELECT project_id FROM issues WHERE id = ?",
                                Long.class, issueId));
    }

    @Test
    void savedIssuesGetDifferentIncreasingIds() {
        long projectId = saveProject("Tracker");
        long first = saveIssue(projectId, "First issue");
        long second = saveIssue(projectId, "Second issue");

        assertNotEquals(first, second);
        assertTrue(second > first);
    }

    @Test
    void creationTimeIsFilledByTheDatabase() {
        long issueId = saveIssue(saveProject("Tracker"), "Has timestamp");

        assertNotNull(issueRepository.findById(issueId)
                          .orElseThrow()
                          .getIssueCreationTime());
    }

    @Test
    void descriptionIsOptional() {
        long issueId = saveIssue(saveProject("Tracker"), "No description");

        assertNull(issueRepository.findById(issueId)
                       .orElseThrow()
                       .getIssueDescription());
    }

    @Test
    void descriptionMaxIs5000Characters() {
        long projectId = saveProject("Tracker");

        Issue fits = newIssue(projectId, "Big description");
        fits.setIssueDescription("d".repeat(5000));
        assertDoesNotThrow(() -> {
            issueRepository.save(fits);
            entityManager.flush();
        });

        Issue tooBig = newIssue(projectId, "Too big description");
        tooBig.setIssueDescription("d".repeat(5001));
        assertSaveRejected(() -> issueRepository.save(tooBig));
    }

    @Test
    void specialCharactersSurviveStorage() {
        String title = "Caf\u00e9 crash \u2014 fix & retest";
        long issueId = saveIssue(saveProject("Tracker"), title);

        assertEquals(
            title,
            issueRepository.findById(issueId).orElseThrow().getIssueTitle());
    }

    @Test
    void aProjectCanHaveManyIssues() {
        long projectId = saveProject("Busy");
        for (int i = 0; i < 5; i++)
            issueRepository.save(newIssue(projectId, "Issue number " + i));
        flushAndClear();

        assertEquals(5, issueRepository.count());
        assertEquals(5, jdbc.queryForObject(
                            "SELECT COUNT(*) FROM issues WHERE project_id = ?",
                            Integer.class, projectId));
    }

    // ---------- status and priority ----------

    @ParameterizedTest(name = "[{index}] {0}")
    @EnumSource(IssueStatus.class)
    void everyStatusIsStoredAsItsNameAndReadBack(IssueStatus status) {
        Issue issue = newIssue(saveProject("Statuses"), "Status check");
        issue.setIssueStatus(status);
        long issueId = issueRepository.save(issue).getIssueId();
        flushAndClear();

        assertEquals(
            status.name(),
            jdbc.queryForObject("SELECT status FROM issues WHERE id = ?",
                                String.class, issueId));
        assertEquals(
            status,
            issueRepository.findById(issueId).orElseThrow().getIssueStatus());
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @EnumSource(IssuePriority.class)
    void everyPriorityIsStoredAsItsNameAndReadBack(IssuePriority priority) {
        Issue issue = newIssue(saveProject("Priorities"), "Priority check");
        issue.setIssuePriority(priority);
        long issueId = issueRepository.save(issue).getIssueId();
        flushAndClear();

        assertEquals(
            priority.name(),
            jdbc.queryForObject("SELECT priority FROM issues WHERE id = ?",
                                String.class, issueId));
        assertEquals(
            priority,
            issueRepository.findById(issueId).orElseThrow().getIssuePriority());
    }

    @Test
    void issueWithoutAStatusIsRejected() {
        Issue issue = newIssue(saveProject("Tracker"), "No status");
        issue.setIssueStatus(null);

        assertSaveRejected(() -> issueRepository.save(issue));
        assertEquals(0, issueCount());
    }

    @Test
    void issueWithoutAPriorityIsRejected() {
        Issue issue = newIssue(saveProject("Tracker"), "No priority");
        issue.setIssuePriority(null);

        assertSaveRejected(() -> issueRepository.save(issue));
        assertEquals(0, issueCount());
    }

    // ---------- validation on save ----------

    @ParameterizedTest(name = "[{index}]")
    @MethodSource("invalidTitles")
    void invalidTitlesAreRejected(String title) {
        long projectId = saveProject("Tracker");

        assertSaveRejected(
            () -> issueRepository.save(newIssue(projectId, title)));

        assertEquals(0, issueCount(), "A rejected save must store nothing");
    }

    @ParameterizedTest(name = "[{index}]")
    @MethodSource("validTitles")
    void validTitlesAreAccepted(String title) {
        long issueId = saveIssue(saveProject("Tracker"), title);

        assertEquals(title, storedTitle(issueId));
    }

    // ---------- relationship rules ----------

    @Test
    void issueWithoutAProjectIsRejected() {
        Issue issue = newIssue(saveProject("Tracker"), "Orphan issue");
        issue.setProject(null);

        assertSaveRejected(() -> issueRepository.save(issue));
        assertEquals(0, issueCount());
    }

    @Test
    void issueForAMissingProjectIsRejected() {
        assertSaveRejected(()
                               -> issueRepository.save(
                                   newIssue(999_999_999L, "Ghost project")));

        assertEquals(0, issueCount());
    }

    @Test
    void issueKnowsItsProject() {
        long projectId = saveProject("Parent");
        long issueId = saveIssue(projectId, "Child issue");

        Issue found = issueRepository.findById(issueId).orElseThrow();
        assertEquals(projectId, found.getProject().getProjectId());
        assertEquals("Parent", found.getProject().getProjectName());
    }

    @Test
    void projectListsItsOwnIssuesOnly() {
        long first = saveProject("First");
        long second = saveProject("Second");
        saveIssue(first, "Belongs to first");
        saveIssue(second, "Belongs to second");

        Project found = projectRepository.findById(first).orElseThrow();
        assertEquals(1, found.getIssues().size());
        assertEquals("Belongs to first",
                     found.getIssues().get(0).getIssueTitle());
    }

    // ---------- findById / existsById ----------

    @Test
    void findByIdReturnsTheSavedIssue() {
        long issueId = saveIssue(saveProject("Tracker"), "Findable issue");

        Issue found = issueRepository.findById(issueId).orElseThrow();
        assertEquals(issueId, found.getIssueId());
        assertEquals("Findable issue", found.getIssueTitle());
    }

    @ParameterizedTest(name = "[{index}]")
    @MethodSource("missingIds")
    void findByIdOfAMissingIssueIsEmpty(Long id) {
        assertTrue(issueRepository.findById(id).isEmpty());
    }

    @Test
    void existsByIdIsTrueOnlyForExistingIssues() {
        long issueId = saveIssue(saveProject("Tracker"), "Exists issue");

        assertTrue(issueRepository.existsById(issueId));
        assertFalse(issueRepository.existsById(999_999_999L));
    }

    // ---------- findAll / count ----------

    @Test
    void findAllOnAnEmptyTableIsEmpty() {
        assertFalse(issueRepository.findAll().iterator().hasNext());
        assertEquals(0, issueRepository.count());
    }

    @Test
    void findAllReturnsEveryIssueExactlyOnce() {
        long projectId = saveProject("Tracker");
        saveIssue(projectId, "Issue one");
        saveIssue(projectId, "Issue two");
        saveIssue(projectId, "Issue three");

        long found =
            StreamSupport.stream(issueRepository.findAll().spliterator(), false)
                .count();
        assertEquals(3, found);
        assertEquals(3, issueRepository.count());
    }

    // ---------- update (save with an existing id) ----------

    @Test
    void savingAnExistingIssueUpdatesItInsteadOfCreatingANewOne() {
        long issueId = saveIssue(saveProject("Tracker"), "Old title");

        Issue found = issueRepository.findById(issueId).orElseThrow();
        found.setIssueTitle("New title");
        found.setIssueDescription("Now with a description");
        issueRepository.save(found);
        flushAndClear();

        assertEquals(1, issueCount());
        Issue updated = issueRepository.findById(issueId).orElseThrow();
        assertEquals("New title", updated.getIssueTitle());
        assertEquals("Now with a description", updated.getIssueDescription());
    }

    @Test
    void statusAndPriorityCanBeChanged() {
        IssueStatus[] statuses = IssueStatus.values();
        IssuePriority[] priorities = IssuePriority.values();
        assumeTrue(statuses.length >= 2 && priorities.length >= 2,
                   "Needs at least two status and two priority values");
        long issueId = saveIssue(saveProject("Tracker"), "Change me");

        Issue found = issueRepository.findById(issueId).orElseThrow();
        found.setIssueStatus(statuses[statuses.length - 1]);
        found.setIssuePriority(priorities[priorities.length - 1]);
        issueRepository.save(found);
        flushAndClear();

        Issue updated = issueRepository.findById(issueId).orElseThrow();
        assertEquals(statuses[statuses.length - 1], updated.getIssueStatus());
        assertEquals(priorities[priorities.length - 1],
                     updated.getIssuePriority());
    }

    @ParameterizedTest(name = "[{index}]")
    @MethodSource("invalidTitles")
    void invalidTitlesAreRejectedOnUpdate(String title) {
        long issueId = saveIssue(saveProject("Tracker"), "Original title");

        Issue found = issueRepository.findById(issueId).orElseThrow();
        found.setIssueTitle(title);
        assertSaveRejected(() -> issueRepository.save(found));

        assertEquals("Original title", storedTitle(issueId));
    }

    @Test
    void updatingOneIssueLeavesOthersUnchanged() {
        long projectId = saveProject("Tracker");
        long a = saveIssue(projectId, "Issue A");
        long b = saveIssue(projectId, "Issue B");

        Issue found = issueRepository.findById(a).orElseThrow();
        found.setIssueTitle("Issue A2");
        issueRepository.save(found);
        flushAndClear();

        assertEquals("Issue B", storedTitle(b));
    }

    // ---------- delete ----------

    @Test
    void deleteByIdRemovesTheIssueButKeepsItsProject() {
        long projectId = saveProject("Parent");
        long issueId = saveIssue(projectId, "Short lived");

        issueRepository.deleteById(issueId);
        flushAndClear();

        assertTrue(issueRepository.findById(issueId).isEmpty());
        assertEquals(0, issueCount());
        assertEquals(1, projectCount());
    }

    @Test
    void deletingOneIssueLeavesOthersUntouched() {
        long projectId = saveProject("Tracker");
        long a = saveIssue(projectId, "Issue A");
        long b = saveIssue(projectId, "Issue B");
        long c = saveIssue(projectId, "Issue C");

        issueRepository.deleteById(b);
        flushAndClear();

        assertTrue(issueRepository.findById(a).isPresent());
        assertTrue(issueRepository.findById(b).isEmpty());
        assertTrue(issueRepository.findById(c).isPresent());
    }

    @Test
    void deleteByIdOfAMissingIssueDoesNothing() {
        long existing = saveIssue(saveProject("Tracker"), "Bystander issue");

        assertDoesNotThrow(() -> issueRepository.deleteById(999_999_999L));

        assertTrue(issueRepository.findById(existing).isPresent());
    }

    @Test
    void deleteAllRemovesEveryIssueButNoProjects() {
        long projectId = saveProject("Tracker");
        saveIssue(projectId, "Issue one");
        saveIssue(projectId, "Issue two");

        issueRepository.deleteAll();
        flushAndClear();

        assertEquals(0, issueCount());
        assertEquals(1, projectCount());
    }

    @Test
    void deletingAProjectRemovesItsIssuesOnly() {
        long doomed = saveProject("Doomed");
        long safe = saveProject("Survivor");
        saveIssue(doomed, "Goes away 1");
        saveIssue(doomed, "Goes away 2");
        saveIssue(safe, "Stays put");

        projectRepository.deleteById(doomed);
        flushAndClear();

        assertEquals(1, issueCount());
        assertEquals(1, jdbc.queryForObject(
                            "SELECT COUNT(*) FROM issues WHERE project_id = ?",
                            Integer.class, safe));
    }
}
