package ca.humanbeans.sourbeans.service;
import ca.humanbeans.sourbeans.entity.*;
import ca.humanbeans.sourbeans.exception.ResourceNotFoundException;
import ca.humanbeans.sourbeans.repository.*;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {
    private final IssueRepository issueRepo;
    private final ProjectRepository projectRepo;
    public ProjectService(IssueRepository inIssueRepo,
                          ProjectRepository inProjectRepo) {
        this.issueRepo = inIssueRepo;
        this.projectRepo = inProjectRepo;
    };

    /////////////////
    // ProjectRepo //
    /////////////////

    // GET
    public List<Project> getAllProjects() {
        // Bs I have to do to transform it from an iterable.
        List<Project> returnList = new ArrayList<>();
        projectRepo.findAll().forEach(returnList::add);
        return returnList;
    }
    public Project getProjectById(Long id) {
        Project result = projectRepo.findById(id).orElseThrow(
            () -> new ResourceNotFoundException());
        return result;
    }
    // POST
    public void addProject(Project projectToAdd) {}
    // PUT
    public void updateProject(Project projectToUpdate) {}
    // DELETE
    public void deleteProject(Project projectToDelete) {}

    ///////////////
    // issueRepo //
    ///////////////

    // GET
    public List<Issue> getAllIssues() {
        // Bs I have to do to transform it from an iterable.
        List<Issue> returnList = new ArrayList<>();
        issueRepo.findAll().forEach(returnList::add);
        return returnList;
    }
    public List<Issue> getAllIssuesFromProject(Project inProject) {
        return inProject.getIssues();
    }
    // POST
    public void addIssue(Project parent, Issue issueToAdd) {}
    // PUT
    public void updateIssue(Project parent, Issue issueToUpdate) {}
    // DELETE
    public void deleteIssue(Project parent, Issue issueToDelete) {}
    /////////////
    // Helpers //
    /////////////
}
