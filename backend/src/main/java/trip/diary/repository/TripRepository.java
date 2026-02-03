package trip.diary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import trip.diary.entity.Trip;

import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip,Long> {

}
