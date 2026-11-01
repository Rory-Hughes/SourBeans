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

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional // every test rolls back, so sourbeans_test stays clean
class EntityMappingTests {

    @Autowired EntityManager entityManager;
    @Autowired JdbcTemplate jdbc;
    @Autowired Validator validator;

    // TODO: Make sure we only define useful ones..
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

    // TODO: Fill with useful test data..
    // ---------- test data ----------

    static Stream<String> invalidNames() {
        return Stream.of(null, "", "     ", "a", "ab", "a".repeat(81));
    }

    static Stream<String> validNames() {
        return Stream.of("abc", "abcd", "a".repeat(79), "a".repeat(80));
    }

    // ---------- Test Project Creation and Rules ----------

    @Test
    void validProjectHasNoViolations() {
        assertTrue(
            validator.validate(newProject("Website", "Rebuild")).isEmpty());
    }
    // ---------- Test Retrieval of all Projects ----------
    // ---------- Test Updating Existing Projects ----------
    // ---------- Test Deletion of Projects ----------
    // ---- Test Lookup and update attempts with nonexistent identifiers ----
    // ---------- Verfiy persistence across application restarts ----------
}
