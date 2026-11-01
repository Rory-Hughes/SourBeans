package ca.humanbeans.sourbeans.service;
import ca.humanbeans.sourbeans.entity.*;
import ca.humanbeans.sourbeans.exception.ResourceConflictException;
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
        try {
            List<Project> returnList = new ArrayList<>();
            projectRepo.findAll().forEach(returnList::add);
            return returnList;
        } catch (Exception e) {
            // TODO: This probably needs to be an actually defined exception
            throw e;
        }
    }
    public Project getProjectById(Long projectIdToFind) {
        // TODO:I will need to ask how we should really be dealing with this
        try {
            Project result =
                projectRepo.findById(projectIdToFind)
                    .orElseThrow(() -> new ResourceNotFoundException());
            return result;
        } catch (ResourceNotFoundException rnfe) {
            throw rnfe;
        } catch (Exception e) {
            throw e;
        }
    }
    public Project getProjectById(int projectIdToFind) {
        // TODO:I will need to ask how we should really be dealing with this
        try {
            String strId = String.valueOf(projectIdToFind);
            Long parsedId = Long.parseLong(strId);
            Project result = projectRepo.findById(parsedId).orElseThrow(
                () -> new ResourceNotFoundException());
            return result;
        } catch (ResourceNotFoundException rnfe) {
            throw rnfe;
        } catch (Exception e) {
            // TODO: handle exception
            throw e;
        }
    }
    public Project getProjectById(String projectIdToFind) {
        try {
            Long parsedId = Long.parseLong(projectIdToFind);
            Project result = projectRepo.findById(parsedId).orElseThrow(
                () -> new ResourceNotFoundException());
            return result;

        } catch (ResourceNotFoundException rnfe) {
            throw rnfe;
        } catch (Exception e) {
            // TODO: handle exception
            throw e;
        }
    }
    // POST
    public void addProject(Project projectToAdd) {
        try {
            if (projectExists(projectToAdd)) {
                throw new ResourceConflictException();
            }
            projectRepo.save(projectToAdd);
        } catch (ResourceConflictException rce) {
            // TODO: This may be the way to do this.
            throw rce;
        } catch (Exception e) {
            // TODO: handle exception
            throw e;
        }
    }
    // PUT
    public void updateProject(Project projectToUpdate) {
        try {
            if (!projectExists(projectToUpdate)) {
                throw new ResourceNotFoundException();
            }
            // NOTE: Im not so sure if this is how this works..
            projectRepo.save(projectToUpdate);
        } catch (ResourceNotFoundException rnf) {
            // TODO: This may be the way to do this.
            throw rnf;
        } catch (Exception e) {
            // TODO: handle exception
            throw e;
        }
    }
    // DELETE
    public void deleteProject(Long projectIdToDelete) {
        try {
            if (!projectExists(projectIdToDelete)) {
                throw new ResourceNotFoundException();
            }
            // NOTE: Im not so sure if this is how this works..
            projectRepo.deleteById(projectIdToDelete);
        } catch (ResourceNotFoundException rnf) {
            // TODO: This may be the way to do this.
            throw rnf;
        } catch (Exception e) {
            // TODO: handle exception
            throw e;
        }
    }
    public void deleteProject(int projectIdToDelete) {
        try {
            String strId = String.valueOf(projectIdToDelete);
            Long parsedId = Long.parseLong(strId);
            if (!projectExists(parsedId)) {
                throw new ResourceConflictException();
            }
            // NOTE: Im not so sure if this is how this works..
            projectRepo.deleteById(parsedId);
        } catch (ResourceConflictException rce) {
            // TODO: This may be the way to do this.
            throw rce;
        } catch (Exception e) {
            // TODO: handle exception
            throw e;
        }
    }
    public void deleteProject(String projectIdToDelete) {
        try {
            Long parsedId = Long.parseLong(projectIdToDelete);
            if (!projectExists(parsedId)) {
                throw new ResourceNotFoundException();
            }
            // NOTE: Im not so sure if this is how this works..
            projectRepo.deleteById(parsedId);
        } catch (ResourceNotFoundException rnf) {
            // TODO: This may be the way to do this.
            throw rnf;
        } catch (Exception e) {
            // TODO: handle exception
            throw e;
        }
    }
    public void deleteProject(Project projectToDelete) {
        try {
            if (!projectExists(projectToDelete)) {
                throw new ResourceNotFoundException();
            }
            // NOTE: Im not so sure if this is how this works..
            projectRepo.delete(projectToDelete);
        } catch (ResourceNotFoundException rnf) {
            // TODO: This may be the way to do this.
            throw rnf;
        } catch (Exception e) {
            // TODO: handle exception
            throw e;
        }
    }

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
    private boolean projectExists(Project p) {
        return projectRepo.existsById(p.getProjectId());
    }
    private boolean projectExists(Long projectId) {
        return projectRepo.existsById(projectId);
    }
    private boolean projectExists(int projectId) {
        String strId = String.valueOf(projectId);
        Long parsedId = Long.parseLong(strId);
        return projectRepo.existsById(parsedId);
    }
    private boolean projectExists(String projectId) {
        Long parsedId = Long.parseLong(projectId);
        return projectRepo.existsById(parsedId);
    }
}
