package demo.repositories;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import demo.domain.Blog;

public interface BlogRepository extends CrudRepository<Blog, Long> {
	List<Blog> findByTitle(String title);
}
