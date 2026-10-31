package ca.humanbeans.sourbeans.repository;
import ca.humanbeans.sourbeans.entity.Issue;
import org.springframework.data.repository.CrudRepository;
public interface IssueRepository extends CrudRepository<Issue, Long> {}
