package ca.humanbeans.sourbeans.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Project {

    ////////////////
    // Properties //
    ////////////////

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    int projectId;
    @Column(name = "name", nullable = false) String projectName;
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
