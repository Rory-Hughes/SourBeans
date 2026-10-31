package ca.humanbeans.sourbeans.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Project {

    /////////////////
    // Constructor //
    /////////////////

    public Project(){};
    ////////////////
    // Properties //
    ////////////////

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    public int projectId;
    @OneToMany(mappedBy = "issueId", orphanRemoval = true)
    public List<Issue> issues;
    @Column(name = "name") public String projectName;
    @Column(name = "description") String projectDescription;
    @Column(name = "created_at", nullable = false)
    public LocalDateTime projectCreationTime;

    /////////////////////////
    // Getters and Setters //
    /////////////////////////

    // Name
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    // ID
    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }
    // Issues
    public List<Issue> getIssues() { return issues; }
    public void setIssues(List<Issue> issues) { this.issues = issues; }
    // Project Creation Time
    public LocalDateTime getProjectCreationTime() {
        return projectCreationTime;
    }
    public void setProjectCreationTime(LocalDateTime projectCreationTime) {
        this.projectCreationTime = projectCreationTime;
    }
    // Project Description
    public String getProjectDescription() { return projectDescription; }
    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    /////////////
    // Methods //
    /////////////
    @Override
    public String toString() {
        return "";
    }
}
