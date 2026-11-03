package ca.humanbeans.sourbeans;

import static org.junit.jupiter.api.Assertions.*;

import ca.humanbeans.sourbeans.entity.IssuePriority;
import ca.humanbeans.sourbeans.entity.IssueStatus;
import ca.humanbeans.sourbeans.entity.Project;
import ca.humanbeans.sourbeans.repository.ProjectRepository;
import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for ProjectRepository (a CrudRepository) against sourbeans_test.
 *
 * REQUIRES: ProjectRepository must be CrudRepository<Project, Long>. The
 * current declaration uses Integer, which does not match the BIGINT id column
 * or the Long projectId in the entity, and makes findById and deleteById fail
 * with a wrong-id-type error.
 *
 * Only CrudRepository methods are used (save, saveAll, findById, existsById,
 * findAll, count, deleteById, deleteAll). Tests roll back, so Hibernate may
 * not write changes or check validation until a flush. The helpers flush and
 * clear the persistence context so reads really come from the database.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional // every test rolls back, so sourbeans_test stays clean
class ProjectRepositoryTests {

    @Autowired ProjectRepository repository;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManager entityManager;

    // ---------- helpers ----------

    private Project newProject(String name, String description) {
        Project project = new Project();
        project.setProjectName(name);
        project.setProjectDescription(description);
        return project;
    }

    /** Saves a project, forces it into the database, and returns its id. */
    private long saveProject(String name, String description) {
        Project saved = repository.save(newProject(name, description));
        flushAndClear();
        return saved.getProjectId();
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

    private List<Project> allProjects() {
        return StreamSupport.stream(repository.findAll().spliterator(), false)
            .toList();
    }

    private int projectCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM projects",
                                   Integer.class);
    }

    private String storedName(long id) {
        return jdbc.queryForObject("SELECT name FROM projects WHERE id = ?",
                                   String.class, id);
    }

    /** Inserts an issue row directly, bypassing the entity mapping. */
    private void insertIssueRow(long projectId, String title) {
        jdbc.update("INSERT INTO issues (project_id, title, status, priority) "
                        + "VALUES (?, ?, ?, ?)",
                    projectId, title, IssueStatus.OPEN.name(),
                    IssuePriority.MEDIUM.name());
    }

    // ---------- test data ----------

    static Stream<String> invalidNames() {
        return Stream.of(null, "", "     ", "a", "ab", "a".repeat(81));
    }

    static Stream<String> validNames() {
        return Stream.of("abc", "abcd", "a".repeat(79), "a".repeat(80));
    }

    static Stream<Long> missingIds() {
        return Stream.of(0L, -1L, 999_999_999L, Long.MAX_VALUE);
    }

    // ---------- save (create) ----------

    @Test
    void saveReturnsTheProjectWithAGeneratedId() {
        Project saved = repository.save(newProject("Website", "Rebuild"));

        assertTrue(saved.getProjectId() > 0);
        assertEquals("Website", saved.getProjectName());
    }

    @Test
    void savedProjectIsInTheDatabaseTable() {
        long id = saveProject("Stored", "In the table");

        assertEquals("Stored", storedName(id));
        assertEquals(
            "In the table",
            jdbc.queryForObject("SELECT description FROM projects WHERE id = ?",
                                String.class, id));
    }

    @Test
    void savedProjectsGetDifferentIncreasingIds() {
        long first = saveProject("Alpha", null);
        long second = saveProject("Bravo", null);

        assertNotEquals(first, second);
        assertTrue(second > first);
    }

    @Test
    void duplicateNamesAreAllowed() {
        saveProject("Website", null);
        saveProject("Website", null);

        assertEquals(2, projectCount());
    }

    @Test
    void creationTimeIsFilledByTheDatabase() {
        long id = saveProject("Timestamped", null);

        assertNotNull(
            repository.findById(id).orElseThrow().getProjectCreationTime());
    }

    @Test
    void specialCharactersSurviveStorage() {
        String name = "Caf\u00e9 & Co. \u2014 v2.0";
        long id = saveProject(name, "Cr\u00e8me br\u00fbl\u00e9e");

        Project found = repository.findById(id).orElseThrow();
        assertEquals(name, found.getProjectName());
        assertEquals("Cr\u00e8me br\u00fbl\u00e9e",
                     found.getProjectDescription());
    }

    @Test
    void saveAllStoresEveryProject() {
        repository.saveAll(List.of(newProject("One", null),
                                   newProject("Two", null),
                                   newProject("Three", null)));
        entityManager.flush();

        assertEquals(3, projectCount());
    }

    // ---------- validation on save ----------

    @ParameterizedTest(name = "[{index}]")
    @MethodSource("invalidNames")
    void invalidNamesAreRejected(String name) {
        assertSaveRejected(() -> repository.save(newProject(name, null)));

        assertEquals(0, projectCount(), "A rejected save must store nothing");
    }

    @ParameterizedTest(name = "[{index}]")
    @MethodSource("validNames")
    void validNamesAreAccepted(String name) {
        long id = saveProject(name, null);

        assertEquals(name, storedName(id));
    }

    @Test
    void descriptionMaxIs1000Characters() {
        assertDoesNotThrow(() -> saveProject("Long1000", "d".repeat(1000)));
        assertSaveRejected(
            () -> repository.save(newProject("Long1001", "d".repeat(1001))));
    }

    @Test
    void descriptionIsOptional() {
        long id = saveProject("NoDesc", null);

        assertNull(
            repository.findById(id).orElseThrow().getProjectDescription());
    }

    // ---------- findById / existsById ----------

    @Test
    void findByIdReturnsTheSavedProject() {
        long id = saveProject("Findable", "Has a description");

        Project found = repository.findById(id).orElseThrow();
        assertEquals(id, found.getProjectId());
        assertEquals("Findable", found.getProjectName());
        assertEquals("Has a description", found.getProjectDescription());
    }

    @Test
    void findByIdReturnsTheRightProjectAmongMany() {
        saveProject("Project A", null);
        long b = saveProject("Project B", null);
        saveProject("Project C", null);

        assertEquals("Project B",
                     repository.findById(b).orElseThrow().getProjectName());
    }

    @ParameterizedTest(name = "[{index}]")
    @MethodSource("missingIds")
    void findByIdOfAMissingProjectIsEmpty(Long id) {
        Optional<Project> found = repository.findById(id);

        assertTrue(found.isEmpty());
    }

    @Test
    void existsByIdIsTrueOnlyForExistingProjects() {
        long id = saveProject("Exists", null);

        assertTrue(repository.existsById(id));
        assertFalse(repository.existsById(999_999_999L));
    }

    // ---------- findAll / count ----------

    @Test
    void findAllOnAnEmptyTableIsEmpty() {
        assertTrue(allProjects().isEmpty());
        assertEquals(0, repository.count());
    }

    @Test
    void findAllReturnsEveryProjectExactlyOnce() {
        saveProject("One", null);
        saveProject("Two", null);
        saveProject("Three", null);

        List<String> names =
            allProjects().stream().map(Project::getProjectName).toList();
        assertEquals(3, names.size());
        assertTrue(names.containsAll(List.of("One", "Two", "Three")));
        assertEquals(3, repository.count());
    }

    @Test
    void findAllReturnsManyProjects() {
        for (int i = 0; i < 100; i++)
            repository.save(newProject("Project number " + i, null));
        flushAndClear();

        assertEquals(100, allProjects().size());
    }

    // ---------- update (save with an existing id) ----------

    @Test
    void savingAnExistingProjectUpdatesItInsteadOfCreatingANewOne() {
        long id = saveProject("Old name", "Old description");

        Project found = repository.findById(id).orElseThrow();
        found.setProjectName("New name");
        found.setProjectDescription("New description");
        repository.save(found);
        flushAndClear();

        assertEquals(1, projectCount());
        Project updated = repository.findById(id).orElseThrow();
        assertEquals("New name", updated.getProjectName());
        assertEquals("New description", updated.getProjectDescription());
    }

    @Test
    void updatingOneProjectLeavesOthersUnchanged() {
        long a = saveProject("Project A", "First");
        long b = saveProject("Project B", "Second");

        Project found = repository.findById(a).orElseThrow();
        found.setProjectName("Project A2");
        repository.save(found);
        flushAndClear();

        Project other = repository.findById(b).orElseThrow();
        assertEquals("Project B", other.getProjectName());
        assertEquals("Second", other.getProjectDescription());
    }

    @Test
    void updateDoesNotChangeTheCreationTime() {
        long id = saveProject("Timeless", null);
        Timestamp before =
            jdbc.queryForObject("SELECT created_at FROM projects WHERE id = ?",
                                Timestamp.class, id);

        Project found = repository.findById(id).orElseThrow();
        found.setProjectName("Still timeless");
        repository.save(found);
        flushAndClear();

        assertEquals(before, jdbc.queryForObject(
                                 "SELECT created_at FROM projects WHERE id = ?",
                                 Timestamp.class, id));
    }

    @ParameterizedTest(name = "[{index}]")
    @MethodSource("invalidNames")
    void invalidNamesAreRejectedOnUpdate(String name) {
        long id = saveProject("Original name", null);

        Project found = repository.findById(id).orElseThrow();
        found.setProjectName(name);
        assertSaveRejected(() -> repository.save(found));

        assertEquals("Original name", storedName(id));
    }

    // ---------- delete ----------

    @Test
    void deleteByIdRemovesTheProject() {
        long id = saveProject("Short lived", null);

        repository.deleteById(id);
        flushAndClear();

        assertTrue(repository.findById(id).isEmpty());
        assertEquals(0, projectCount());
    }

    @Test
    void deletingOneProjectLeavesOthersUntouched() {
        long a = saveProject("Project A", null);
        long b = saveProject("Project B", null);
        long c = saveProject("Project C", null);

        repository.deleteById(b);
        flushAndClear();

        assertTrue(repository.findById(a).isPresent());
        assertTrue(repository.findById(b).isEmpty());
        assertTrue(repository.findById(c).isPresent());
    }

    @Test
    void deleteByIdOfAMissingProjectDoesNothing() {
        // Spring Data silently ignores a missing id, so the service must check
        // existsById itself to report "not found" (see SET-03).
        long existing = saveProject("Bystander", null);

        assertDoesNotThrow(() -> repository.deleteById(999_999_999L));

        assertTrue(repository.findById(existing).isPresent());
    }

    @Test
    void deleteAllRemovesEveryProject() {
        saveProject("One", null);
        saveProject("Two", null);

        repository.deleteAll();
        flushAndClear();

        assertEquals(0, projectCount());
    }

    @Test
    void deletingAProjectThatHasIssuesIsRejected() {
        long doomed = saveProject("Doomed", null);
        long safe = saveProject("Survivor", null);
        insertIssueRow(doomed, "Stays 1");
        insertIssueRow(doomed, "Stays 2");
        insertIssueRow(safe, "Stays put");

        assertSaveRejected(() -> repository.deleteById(doomed));

        assertEquals(2, projectCount());
        assertEquals(2, jdbc.queryForObject(
                            "SELECT COUNT(*) FROM issues WHERE project_id = ?",
                            Integer.class, doomed));
        assertEquals(1, jdbc.queryForObject(
                            "SELECT COUNT(*) FROM issues WHERE project_id = ?",
                            Integer.class, safe));
    }

    @Test
    void updatedAtIsFilledByTheDatabase() {
        long id = saveProject("Timestamped", null);

        assertNotNull(jdbc.queryForObject(
            "SELECT updated_at FROM projects WHERE id = ?", Object.class, id));
    }
}
