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
    int issueId;
    @JoinColumn(name = "projectId", nullable = false)
    @ManyToOne
    Project project;
    @Column(name = "title", nullable = false) String issueTitle;
    @Column(name = "description") String issueDescription;
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    IssueStatus issueStatus;
    @Column(name = "priority", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    IssuePriority issuePriority;
    @Column(name = "created_at", nullable = false)
    LocalDateTime issueCreationTime;

    /////////////
    // Methods //
    /////////////
    @Override
    public String toString() {
        return "";
    }
}
