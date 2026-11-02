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
            throw e;
        }
    }
    public Project getProjectById(Long projectIdToFind) {
        try {
            if (projectIdToFind == null) {
                // NOTE: This seems like a case for a custom exception
                throw new IllegalArgumentException();
            }
            Project result =
                projectRepo.findById(projectIdToFind)
                    .orElseThrow(() -> new ResourceNotFoundException());
            return result;
        } catch (Exception e) {
            throw e;
        }
    }
    public Project getProject(Project inProject) {
        try {
            if (inProject == null) {
                throw new IllegalArgumentException();
            }
            return getProjectById(inProject.getProjectId());
        } catch (Exception e) {
            throw e;
        }
    }
    // POST
    public void addProject(Project projectToAdd) {
        try {
            if (projectToAdd == null) {
                // NOTE: This seems like a case for a custom exception
                throw new IllegalArgumentException();
            }
            if (projectExists(projectToAdd.getProjectId())) {
                throw new ResourceConflictException();
            }
            projectRepo.save(projectToAdd);
        } catch (Exception e) {
            throw e;
        }
    }
    // PUT
    public void updateProject(Project projectToUpdate) {
        try {
            if (projectToUpdate == null) {
                throw new IllegalArgumentException();
            }
            // interesting you need to do it like this
            Project originalProj =
                getProjectById(projectToUpdate.getProjectId());
            originalProj = projectToUpdate;
            projectRepo.save(originalProj);
        } catch (Exception e) {
            throw e;
        }
    }
    // DELETE
    public void deleteProject(Long projectIdToDelete) {
        try {
            if (projectIdToDelete == null) {
                // NOTE: This seems like a case for a custom exception
                throw new IllegalArgumentException();
            }
            if (!projectExists(projectIdToDelete)) {
                throw new ResourceNotFoundException();
            }
            projectRepo.deleteById(projectIdToDelete);
        } catch (Exception e) {
            throw e;
        }
    }

    /////////////
    // HELPERS //
    /////////////
    private boolean projectExists(Long id) {
        try {
            // Just doing it, if theres no throw then it passes.
            getProjectById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    ///////////////
    // issueRepo //
    ///////////////

    // GET
    // POST
    // PUT
    // DELETE

    /////////////
    // Mirrors //
    /////////////

    public Project getProjectById(int projectIdToFind) {
        try {
            return getProjectById((long)projectIdToFind);
        } catch (Exception e) { // Since this is delegated to the original
                                // No need to catch or throw specifically
            throw e;
        }
    }
    public Project getProjectById(String projectIdToFind) {
        try {
            return getProjectById(Long.valueOf(projectIdToFind));
        } catch (Exception e) { // Since this is delegated to the original
                                // No need to catch or throw specifically
            throw e;
        }
    }
    public void deleteProject(int projectIdToDelete) {
        try {
            deleteProject((long)projectIdToDelete);
        } catch (Exception e) {
            throw e;
        }
    }
    public void deleteProject(String projectIdToDelete) {
        try {
            deleteProject(Long.valueOf(projectIdToDelete));
        } catch (Exception e) {
            throw e;
        }
    }
    public void deleteProject(Project projectToDelete) {
        try {
            if (projectToDelete == null) {
                throw new IllegalArgumentException();
            }
            deleteProject(projectToDelete.getProjectId());
        } catch (Exception e) {
            throw e;
        }
    }
}
