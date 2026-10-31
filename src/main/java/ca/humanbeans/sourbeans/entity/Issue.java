package ca.humanbeans.sourbeans.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class Issue {

    /////////////////
    // Constructor //
    /////////////////

    public Issue(){};

    ////////////////
    // Properties //
    ////////////////

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    public int issueId;
    @JoinColumn(name = "project_id", nullable = false)
    @ManyToOne
    public Project project;
    @Column(name = "title", nullable = false) public String issueTitle;
    @Column(name = "description") public String issueDescription;
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    public IssueStatus issueStatus;
    @Column(name = "priority", nullable = false)
    @Enumerated(EnumType.STRING)
    public IssuePriority issuePriority;
    @Column(name = "created_at", nullable = false)
    public LocalDateTime issueCreationTime;

    /////////////////////////
    // Getters and Setters //
    /////////////////////////

    // ID
    public int getIssueId() { return issueId; }
    public void setIssueId(int issueId) { this.issueId = issueId; }
    // Project Ref
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    // Title
    public String getIssueTitle() { return issueTitle; }
    public void setIssueTitle(String issueTitle) {
        this.issueTitle = issueTitle;
    }
    // Status
    public IssueStatus getIssueStatus() { return issueStatus; }
    public void setIssueStatus(IssueStatus issueStatus) {
        this.issueStatus = issueStatus;
    }
    // Priority
    public IssuePriority getIssuePriority() { return issuePriority; }
    public void setIssuePriority(IssuePriority issuePriority) {
        this.issuePriority = issuePriority;
    }
    // Description
    public String getIssueDescription() { return issueDescription; }
    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
    }
    // Creation Time
    public LocalDateTime getIssueCreationTime() { return issueCreationTime; }
    public void setIssueCreationTime(LocalDateTime issueCreationTime) {
        this.issueCreationTime = issueCreationTime;
    }

    /////////////
    // Methods //
    /////////////
    @Override
    public String toString() {
        return "";
    }
}
