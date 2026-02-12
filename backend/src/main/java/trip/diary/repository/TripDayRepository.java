package trip.diary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import trip.diary.entity.TripDay;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TripDayRepository extends JpaRepository<TripDay, Long> {

    List<TripDay> findByTrip_IdOrderByDayDateAsc(Long tripId);

    Optional<TripDay> findByTrip_IdAndDayDate(Long tripId, LocalDate dayDate);

    Optional<TripDay> findByTrip_IdAndDayIndex(Long tripId, Integer dayIndex);
}
