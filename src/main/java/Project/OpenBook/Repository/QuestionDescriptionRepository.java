package Project.OpenBook.Repository;

import Project.OpenBook.Domain.QuestionDescription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionDescriptionRepository extends JpaRepository<QuestionDescription, Long> {
}
