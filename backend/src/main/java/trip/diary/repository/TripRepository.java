package trip.diary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import trip.diary.entity.Trip;

public interface TripRepository extends JpaRepository<Trip, Long> {
}