package trip.diary.repository;

import trip.diary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // user_id로 유저 찾기
    Optional<User> findByUserId(String userId);
}