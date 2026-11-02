package ca.humanbeans.sourbeans.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "issues")
public class Issue {

    /////////////////
    // Constructor //
    /////////////////

    public Issue() {}

    ////////////////
    // Properties //
    ////////////////

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long issueId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    @NotNull
    private Project project;

    @Column(name = "title", nullable = false, length = 120)
    @NotBlank
    @Size(min = 5, max = 120)
    private String issueTitle;

    @Size(max = 5000)
    @Column(name = "description", length = 5000)
    private String issueDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private IssueStatus issueStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 50)
    private IssuePriority issuePriority;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime issueCreationTime;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime issueLastUpdated;

    /////////////////////////
    // Getters and Setters //
    /////////////////////////

    public Long getIssueId() { return issueId; }
    public void setIssueId(Long issueId) { this.issueId = issueId; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public String getIssueTitle() { return issueTitle; }
    public void setIssueTitle(String issueTitle) {
        this.issueTitle = issueTitle;
    }

    public String getIssueDescription() { return issueDescription; }
    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
    }

    public IssueStatus getIssueStatus() { return issueStatus; }
    public void setIssueStatus(IssueStatus issueStatus) {
        this.issueStatus = issueStatus;
    }

    public IssuePriority getIssuePriority() { return issuePriority; }
    public void setIssuePriority(IssuePriority issuePriority) {
        this.issuePriority = issuePriority;
    }

    public LocalDateTime getIssueCreationTime() { return issueCreationTime; }
    public void setIssueCreationTime(LocalDateTime issueCreationTime) {
        this.issueCreationTime = issueCreationTime;
    }
    public LocalDateTime getIssueLastUpdated() { return issueLastUpdated; }

    public void setIssueLastUpdated(LocalDateTime issueLastUpdated) {
        this.issueLastUpdated = issueLastUpdated;
    }
    /////////////
    // Methods //
    /////////////

    @Override
    public String toString() {
        return "Issue{id=" + issueId + ", title=" + issueTitle + "}";
    }
}
