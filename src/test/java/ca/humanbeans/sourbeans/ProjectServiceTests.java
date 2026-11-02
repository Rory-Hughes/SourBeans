package ca.humanbeans.sourbeans;

import static org.junit.jupiter.api.Assertions.*;

import ca.humanbeans.sourbeans.entity.Issue;
import ca.humanbeans.sourbeans.entity.IssuePriority;
import ca.humanbeans.sourbeans.entity.IssueStatus;
import ca.humanbeans.sourbeans.entity.Project;
import ca.humanbeans.sourbeans.exception.ResourceConflictException;
import ca.humanbeans.sourbeans.exception.ResourceNotFoundException;
import ca.humanbeans.sourbeans.service.ProjectService;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional // every test rolls back, so sourbeans_test stays clean
class ProjectServiceTests {

    private static final Long MISSING_LONG_ID = Long.MAX_VALUE;
    private static final int MISSING_INT_ID = 999_999_999;
    private static final String MISSING_STRING_ID = "999999999";

    @Autowired ProjectService service;
    @Autowired EntityManager entityManager;

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
        flushAndClear();
        return project;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private int projectCount() { return service.getAllProjects().size(); }

    private static Throwable rootCause(Throwable t) {
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        return t;
    }

    private void assertRejectedByValidation(Runnable action) {
        Throwable thrown = assertThrows(Exception.class, () -> {
            action.run();
            entityManager.flush();
        });
        assertInstanceOf(ConstraintViolationException.class, rootCause(thrown),
                         "Expected a bean-validation failure but got: " +
                             thrown);
    }

    /** Persists a project with one issue and returns the (detached) project. */
    private Project persistProjectWithIssue(String issueTitle) {
        Project project = newProject("Has issues", "desc");
        entityManager.persist(project);
        Issue issue = new Issue();
        issue.setProject(project);
        issue.setIssueTitle(issueTitle);
        issue.setIssueStatus(IssueStatus.OPEN);
        issue.setIssuePriority(IssuePriority.MEDIUM);
        entityManager.persist(issue);
        project.getIssues().add(issue);
        flushAndClear();
        return project;
    }

    private long issueCountWithTitle(String title) {
        return entityManager
            .createQuery("select count(i) from Issue i where i.issueTitle = :t",
                         Long.class)
            .setParameter("t", title)
            .getSingleResult();
    }

    // ---------- test data ----------

    static Stream<String> invalidNames() {
        return Stream.of(null, "", "     ", "a", "ab", "a".repeat(81));
    }

    static Stream<String> validNames() {
        return Stream.of("abc", "abcd", "a".repeat(79), "a".repeat(80));
    }

    // ---------- Test Project Creation and Rules ----------

    @Test
    void addProject_validProject_isPersistedAndRetrievable() {
        int before = projectCount();

        Project project = newProject("Website", "Rebuild");
        service.addProject(project);
        flushAndClear();

        assertNotNull(project.getProjectId(), "id should be generated");
        assertEquals(before + 1, projectCount());

        Project found = service.getProjectById(project.getProjectId());
        assertEquals("Website", found.getProjectName());
        assertEquals("Rebuild", found.getProjectDescription());
    }

    @Test
    void addProject_twoNewProjects_getDistinctIds() {
        Project a = newProject("Project A", "first");
        Project b = newProject("Project B", "second");

        service.addProject(a);
        service.addProject(b);
        flushAndClear();

        assertNotNull(a.getProjectId());
        assertNotNull(b.getProjectId());
        assertNotEquals(a.getProjectId(), b.getProjectId());
    }

    @ParameterizedTest
    @MethodSource("validNames")
    void addProject_validNames_areAccepted(String name) {
        int before = projectCount();

        service.addProject(newProject(name, "desc"));
        flushAndClear();

        assertEquals(before + 1, projectCount());
    }

    @ParameterizedTest
    @MethodSource("invalidNames")
    void addProject_invalidNames_areRejected(String name) {
        assertRejectedByValidation(
            () -> service.addProject(newProject(name, "desc")));
    }

    @Test
    void addProject_alreadyExisting_throwsConflict() {
        Project existing = persistProject("Website", "Rebuild");

        assertThrows(ResourceConflictException.class,
                     () -> service.addProject(existing));
    }

    @Test
    void addProject_alreadyExisting_doesNotOverwriteStoredData() {
        Project existing = persistProject("Original", "Original desc");

        existing.setProjectName("Overwritten");
        existing.setProjectDescription("Overwritten desc");
        assertThrows(ResourceConflictException.class,
                     () -> service.addProject(existing));
        entityManager.clear();

        Project stored = service.getProjectById(existing.getProjectId());
        assertEquals("Original", stored.getProjectName());
        assertEquals("Original desc", stored.getProjectDescription());
    }

    @Test
    void addProject_alreadyExisting_doesNotChangeProjectCount() {
        Project existing = persistProject("Website", "Rebuild");
        int before = projectCount();

        assertThrows(ResourceConflictException.class,
                     () -> service.addProject(existing));

        assertEquals(before, projectCount());
    }

    @Test
    void addProject_null_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                     () -> service.addProject(null));
    }

    // ---------- Test Retrieval of all Projects ----------

    @Test
    void getAllProjects_neverReturnsNull() {
        assertNotNull(service.getAllProjects());
    }

    @Test
    void getAllProjects_includesNewlyPersistedProjects() {
        int before = projectCount();
        Project a = persistProject("Project A", "first");
        Project b = persistProject("Project B", "second");

        List<Project> all = service.getAllProjects();

        assertEquals(before + 2, all.size());
        assertTrue(all.stream().anyMatch(
            p -> p.getProjectId().equals(a.getProjectId())));
        assertTrue(all.stream().anyMatch(
            p -> p.getProjectId().equals(b.getProjectId())));
    }

    @Test
    void getAllProjects_hasNoDuplicates() {
        persistProject("Project A", "first");
        persistProject("Project B", "second");

        List<Project> all = service.getAllProjects();

        assertEquals(
            all.size(),
            all.stream().map(Project::getProjectId).distinct().count());
    }

    @Test
    void getAllProjects_reflectsDeletions() {
        Project a = persistProject("Project A", "first");
        persistProject("Project B", "second");
        int before = projectCount();

        service.deleteProject(a.getProjectId());
        flushAndClear();

        List<Project> after = service.getAllProjects();
        assertEquals(before - 1, after.size());
        assertTrue(after.stream().noneMatch(
            p -> p.getProjectId().equals(a.getProjectId())));
    }

    @Test
    void getProjectById_longIntAndString_returnSameProject() {
        Project saved = persistProject("Website", "Rebuild");
        Long id = saved.getProjectId();

        Project byLong = service.getProjectById(id);
        Project byInt = service.getProjectById(id.intValue());
        Project byString = service.getProjectById(String.valueOf(id));

        for (Project p : List.of(byLong, byInt, byString)) {
            assertEquals(id, p.getProjectId());
            assertEquals("Website", p.getProjectName());
            assertEquals("Rebuild", p.getProjectDescription());
        }
    }

    @Test
    void getProjectById_returnsTheRequestedProject_notAnother() {
        Project a = persistProject("Project A", "first");
        Project b = persistProject("Project B", "second");

        assertEquals("Project A",
                     service.getProjectById(a.getProjectId()).getProjectName());
        assertEquals("Project B",
                     service.getProjectById(b.getProjectId()).getProjectName());
    }

    // ---------- Test Updating Existing Projects ----------

    @Test
    void updateProject_changesArePersisted() {
        Project saved = persistProject("Website", "Rebuild");

        Project loaded = service.getProjectById(saved.getProjectId());
        loaded.setProjectName("Website v2");
        loaded.setProjectDescription("Rebuild, again");
        service.updateProject(loaded);
        flushAndClear();

        Project reloaded = service.getProjectById(saved.getProjectId());
        assertEquals("Website v2", reloaded.getProjectName());
        assertEquals("Rebuild, again", reloaded.getProjectDescription());
    }

    @Test
    void updateProject_detachedInstance_isPersisted() {
        Project saved = persistProject("Website", "Rebuild"); // detached
        saved.setProjectName("Detached rename");

        service.updateProject(saved);
        flushAndClear();

        assertEquals(
            "Detached rename",
            service.getProjectById(saved.getProjectId()).getProjectName());
    }

    @Test
    void updateProject_doesNotChangeProjectCount() {
        Project saved = persistProject("Website", "Rebuild");
        int before = projectCount();

        Project loaded = service.getProjectById(saved.getProjectId());
        loaded.setProjectName("Renamed");
        service.updateProject(loaded);
        flushAndClear();

        assertEquals(before, projectCount());
    }

    @Test
    void updateProject_doesNotTouchOtherProjects() {
        Project target = persistProject("Target", "to change");
        Project bystander = persistProject("Bystander", "must not change");

        Project loaded = service.getProjectById(target.getProjectId());
        loaded.setProjectName("Target renamed");
        service.updateProject(loaded);
        flushAndClear();

        Project untouched = service.getProjectById(bystander.getProjectId());
        assertEquals("Bystander", untouched.getProjectName());
        assertEquals("must not change", untouched.getProjectDescription());
    }

    @ParameterizedTest
    @MethodSource("validNames")
    void updateProject_validNames_areAccepted(String name) {
        Project saved = persistProject("Website", "Rebuild");

        Project loaded = service.getProjectById(saved.getProjectId());
        loaded.setProjectName(name);
        service.updateProject(loaded);
        flushAndClear();

        assertEquals(
            name,
            service.getProjectById(saved.getProjectId()).getProjectName());
    }

    @ParameterizedTest
    @MethodSource("invalidNames")
    void updateProject_invalidNames_areRejected(String name) {
        Project saved = persistProject("Website", "Rebuild");

        Project loaded = service.getProjectById(saved.getProjectId());
        loaded.setProjectName(name);

        assertRejectedByValidation(() -> service.updateProject(loaded));
    }

    @Test
    void updateProject_null_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                     () -> service.updateProject(null));
    }

    @Test
    void updateProject_neverSavedProjectWithoutId_throwsIllegalArgument() {
        Project unsaved = newProject("Never saved", "no id yet");

        assertThrows(IllegalArgumentException.class,
                     () -> service.updateProject(unsaved));
    }

    // ---------- Test Deletion of Projects ----------

    @Test
    void deleteProject_byLong_removesProject() {
        Project saved = persistProject("Website", "Rebuild");
        int before = projectCount();

        service.deleteProject(saved.getProjectId());
        flushAndClear();

        assertEquals(before - 1, projectCount());
        assertThrows(ResourceNotFoundException.class,
                     () -> service.getProjectById(saved.getProjectId()));
    }

    @Test
    void deleteProject_byInt_removesProject() {
        Project saved = persistProject("Website", "Rebuild");
        int before = projectCount();

        service.deleteProject(saved.getProjectId().intValue());
        flushAndClear();

        assertEquals(before - 1, projectCount());
        assertThrows(ResourceNotFoundException.class,
                     () -> service.getProjectById(saved.getProjectId()));
    }

    @Test
    void deleteProject_byString_removesProject() {
        Project saved = persistProject("Website", "Rebuild");
        int before = projectCount();

        service.deleteProject(String.valueOf(saved.getProjectId()));
        flushAndClear();

        assertEquals(before - 1, projectCount());
        assertThrows(ResourceNotFoundException.class,
                     () -> service.getProjectById(saved.getProjectId()));
    }

    @Test
    void deleteProject_byDetachedEntity_removesProject() {
        Project saved = persistProject("Website", "Rebuild"); // detached
        int before = projectCount();

        service.deleteProject(saved);
        flushAndClear();

        assertEquals(before - 1, projectCount());
        assertThrows(ResourceNotFoundException.class,
                     () -> service.getProjectById(saved.getProjectId()));
    }

    @Test
    void deleteProject_onlyRemovesTheTargetedProject() {
        Project keep = persistProject("Keep me", "stays");
        Project remove = persistProject("Remove me", "goes");

        service.deleteProject(remove.getProjectId());
        flushAndClear();

        assertEquals(
            "Keep me",
            service.getProjectById(keep.getProjectId()).getProjectName());
    }

    @Test
    void deleteProject_twice_secondAttemptThrowsNotFound() {
        Project saved = persistProject("Website", "Rebuild");

        service.deleteProject(saved.getProjectId());
        flushAndClear();

        assertThrows(ResourceNotFoundException.class,
                     () -> service.deleteProject(saved.getProjectId()));
    }

    @Test
    void deleteProject_withIssues_throwsConflict() {
        Project project = persistProjectWithIssue("conflict-check-1");

        assertThrows(ResourceConflictException.class,
                     () -> service.deleteProject(project.getProjectId()));
    }

    @Test
    void deleteProject_withIssues_keepsTheProjectAndItsIssues() {
        String title = "keep-check-" + UUID.randomUUID();
        Project project = persistProjectWithIssue(title);
        int before = projectCount();

        assertThrows(ResourceConflictException.class,
                     () -> service.deleteProject(project.getProjectId()));
        flushAndClear();

        assertEquals(before, projectCount());
        assertEquals(
            "Has issues",
            service.getProjectById(project.getProjectId()).getProjectName());
        assertEquals(1, issueCountWithTitle(title));
    }

    @Test
    void deleteProject_withIssues_conflictAppliesToEveryOverload() {
        Project project = persistProjectWithIssue("overload-check");
        Long id = project.getProjectId();

        assertThrows(ResourceConflictException.class,
                     () -> service.deleteProject(id.intValue()));
        assertThrows(ResourceConflictException.class,
                     () -> service.deleteProject(String.valueOf(id)));
        assertThrows(ResourceConflictException.class,
                     () -> service.deleteProject(project));
    }

    @Test
    void deleteProject_withIssues_doesNotAffectOtherProjects() {
        Project blocked = persistProjectWithIssue("blocked-by-issue");
        Project free = persistProject("Free project", "no issues");

        assertThrows(ResourceConflictException.class,
                     () -> service.deleteProject(blocked.getProjectId()));
        service.deleteProject(free.getProjectId());
        flushAndClear();

        assertThrows(ResourceNotFoundException.class,
                     () -> service.getProjectById(free.getProjectId()));
        assertNotNull(service.getProjectById(blocked.getProjectId()));
    }

    @Test
    void deleteProject_afterItsIssuesAreRemoved_succeeds() {
        Project project = persistProjectWithIssue("removable-issue");
        entityManager
            .createQuery("delete from Issue i where i.project.projectId = :id")
            .setParameter("id", project.getProjectId())
            .executeUpdate();
        flushAndClear();

        service.deleteProject(project.getProjectId());
        flushAndClear();

        assertThrows(ResourceNotFoundException.class,
                     () -> service.getProjectById(project.getProjectId()));
    }

    @Test
    void deleteProject_nullEntity_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                     () -> service.deleteProject((Project)null));
    }

    @Test
    void deleteProject_nullLongId_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                     () -> service.deleteProject((Long)null));
    }

    // ---- Test Lookup and update attempts with nonexistent identifiers ----

    @Test
    void getProjectById_nonexistent_throwsNotFound() {
        assertThrows(ResourceNotFoundException.class,
                     () -> service.getProjectById(MISSING_LONG_ID));
        assertThrows(ResourceNotFoundException.class,
                     () -> service.getProjectById(MISSING_INT_ID));
        assertThrows(ResourceNotFoundException.class,
                     () -> service.getProjectById(MISSING_STRING_ID));
    }

    @Test
    void getProjectById_zeroAndNegativeIds_throwNotFound() {
        assertThrows(ResourceNotFoundException.class,
                     () -> service.getProjectById(0L));
        assertThrows(ResourceNotFoundException.class,
                     () -> service.getProjectById(-1L));
        assertThrows(ResourceNotFoundException.class,
                     () -> service.getProjectById(0));
        assertThrows(ResourceNotFoundException.class,
                     () -> service.getProjectById(-1));
        assertThrows(ResourceNotFoundException.class,
                     () -> service.getProjectById("0"));
        assertThrows(ResourceNotFoundException.class,
                     () -> service.getProjectById("-1"));
    }

    @Test
    void getProjectById_nullLongId_throwsIllegalArgument_notNotFound() {
        assertThrows(IllegalArgumentException.class,
                     () -> service.getProjectById((Long)null));
    }

    @Test
    void getProjectById_nullStringId_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                     () -> service.getProjectById((String)null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "", "  ", "12.5", "1 2", " 7",
                            "99999999999999999999"})
    void stringIdOverloads_malformedIds_throwIllegalArgument(String badId) {
        // NumberFormatException is an IllegalArgumentException, so either is ok
        assertThrows(IllegalArgumentException.class,
                     () -> service.getProjectById(badId));
        assertThrows(IllegalArgumentException.class,
                     () -> service.deleteProject(badId));
    }

    @Test
    void updateProject_nonexistent_throwsNotFound() {
        Project saved = persistProject("Website", "Rebuild");
        service.deleteProject(saved.getProjectId());
        flushAndClear();

        assertThrows(ResourceNotFoundException.class,
                     () -> service.updateProject(saved));
    }

    @Test
    void updateProject_nonexistent_doesNotCreateARow() {
        Project saved = persistProject("Website", "Rebuild");
        service.deleteProject(saved.getProjectId());
        flushAndClear();
        int before = projectCount();

        assertThrows(ResourceNotFoundException.class,
                     () -> service.updateProject(saved));
        flushAndClear();

        assertEquals(before, projectCount(),
                     "update must never silently insert a new project");
        assertThrows(ResourceNotFoundException.class,
                     () -> service.getProjectById(saved.getProjectId()));
    }

    @Test
    void deleteProject_nonexistent_throwsNotFound() {
        assertThrows(ResourceNotFoundException.class,
                     () -> service.deleteProject(MISSING_LONG_ID));
        assertThrows(ResourceNotFoundException.class,
                     () -> service.deleteProject(MISSING_INT_ID));
        assertThrows(ResourceNotFoundException.class,
                     () -> service.deleteProject(MISSING_STRING_ID));
    }

    @Test
    void deleteProject_nonexistent_doesNotChangeProjectCount() {
        persistProject("Website", "Rebuild");
        int before = projectCount();

        assertThrows(ResourceNotFoundException.class,
                     () -> service.deleteProject(MISSING_LONG_ID));

        assertEquals(before, projectCount());
    }

    @Test
    void deleteProject_entityAlreadyDeleted_throwsNotFound() {
        Project saved = persistProject("Website", "Rebuild");
        service.deleteProject(saved.getProjectId());
        flushAndClear();

        assertThrows(ResourceNotFoundException.class,
                     () -> service.deleteProject(saved));
    }

    // ---------- Verify persistence across transactions ----------

    @Test
    void addProject_survivesCommitAndNewTransaction() {
        Long id = null;
        try {
            Project project = newProject("Durable", "Committed for real");
            service.addProject(project);
            id = project.getProjectId();

            TestTransaction.flagForCommit();
            TestTransaction.end();

            TestTransaction.start();
            entityManager.clear();
            Project found = service.getProjectById(id);
            assertEquals("Durable", found.getProjectName());
            assertEquals("Committed for real", found.getProjectDescription());
        } finally {
            // This test commits so clean up is needed
            if (id != null) {
                if (!TestTransaction.isActive()) {
                    TestTransaction.start();
                }
                service.deleteProject(id);
                TestTransaction.flagForCommit();
                TestTransaction.end();
            }
        }
    }

    @Test
    void updateProject_survivesCommitAndNewTransaction() {
        Long id = null;
        try {
            Project project = newProject("Before", "Before desc");
            service.addProject(project);
            id = project.getProjectId();
            TestTransaction.flagForCommit();
            TestTransaction.end();

            TestTransaction.start();
            Project loaded = service.getProjectById(id);
            loaded.setProjectName("After");
            service.updateProject(loaded);
            TestTransaction.flagForCommit();
            TestTransaction.end();

            TestTransaction.start();
            entityManager.clear();
            assertEquals("After", service.getProjectById(id).getProjectName());
        } finally {
            if (id != null) {
                if (!TestTransaction.isActive()) {
                    TestTransaction.start();
                }
                service.deleteProject(id);
                TestTransaction.flagForCommit();
                TestTransaction.end();
            }
        }
    }
}
