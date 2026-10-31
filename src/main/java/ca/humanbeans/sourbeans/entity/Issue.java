package ca.humanbeans.sourbeans.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.EnumMap;

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
    int id;
    // TODO: MAKE THIS A JOIN COLUMN
    @Column(name = "project_id", nullable = false) int projectId;
    @Column(name = "title", nullable = false) String issueTitle;
    @Column(name = "description") String issueDescription;
    // TODO: MAKE THIS USE ENUMS
    @Column(name = "status", nullable = false) IssueStatus issueStatus;
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
