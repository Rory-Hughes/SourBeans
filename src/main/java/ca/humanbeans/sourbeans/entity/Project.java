package ca.humanbeans.sourbeans.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
public class Project {

    /////////////////
    // Constructor //
    /////////////////

    public Project() {}

    ////////////////
    // Properties //
    ////////////////

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long projectId;

    @NotBlank
    @Size(min = 3, max = 80)
    @Column(name = "name", nullable = false, length = 80)
    private String projectName;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String projectDescription;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime projectCreationTime;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime projectLastUpdated;

    @OneToMany(mappedBy = "project")
    private List<Issue> issues = new ArrayList<>();

    /////////////////////////
    // Getters and Setters //
    /////////////////////////

    // ID
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    // Project Name
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    // Project Description
    public String getProjectDescription() { return projectDescription; }
    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }
    // Project Creation Time
    public LocalDateTime getProjectCreationTime() {
        return projectCreationTime;
    }
    public void setProjectCreationTime(LocalDateTime projectCreationTime) {
        this.projectCreationTime = projectCreationTime;
    }
    // Project Update Time
    public LocalDateTime getProjectLastUpdated() { return projectLastUpdated; }
    public void setProjectLastUpdated(LocalDateTime projectLastUpdated) {
        this.projectLastUpdated = projectLastUpdated;
    }
    // Issues
    public List<Issue> getIssues() { return issues; }
    public void setIssues(List<Issue> issues) { this.issues = issues; }

    /////////////
    // Methods //
    /////////////

    @Override
    public String toString() {
        return "Project{id=" + projectId + ", name=" + projectName + "}";
    }
}
