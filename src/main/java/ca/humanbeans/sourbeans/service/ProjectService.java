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
    public Project getProjectById(Long projectIdToFind) {
        Project result =
            projectRepo.findById(projectIdToFind)
                .orElseThrow(() -> new ResourceNotFoundException());
        return result;
    }
    // POST
    public void addProject(Long projectIdToAdd) {}
    // PUT
    public void updateProject(Long projectIdToUpdate) {}
    // DELETE
    public void deleteProject(Long projectIdToDelete) {}

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
    public List<Issue> getAllIssuesFromProject(Long projectId) {
        return getProjectById(projectId).getIssues();
    }
    // POST
    public void addIssue(Long parentId, Issue issueToAdd) {}
    // PUT
    public void updateIssue(Long parentId, Issue issueToUpdate) {}
    // DELETE
    public void deleteIssue(Long parentId, Issue issueToDelete) {}
    /////////////
    // Helpers //
    /////////////
}
