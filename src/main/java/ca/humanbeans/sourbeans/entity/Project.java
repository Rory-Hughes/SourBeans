package ca.humanbeans.sourbeans.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
    int projectId;
    @OneToMany(mappedBy = "issueId", orphanRemoval = true)
    @Column(name = "name", nullable = false)
    List<Issue> issues;
    @Column(name = "description") String projectDescription;
    @Column(name = "created_at", nullable = false)
    LocalDateTime projectCreationTime;

    /////////////////////////
    // Getters and Setters //
    /////////////////////////

    // None yet :)

    /////////////
    // Methods //
    /////////////
    @Override
    public String toString() {
        return "";
    }
}
