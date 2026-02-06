package trip.diary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import trip.diary.entity.TripDay;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TripDayRepository extends JpaRepository<TripDay, Long> {

    List<TripDay> findByTripIdOrderByDayDateAsc(Long tripId);

    Optional<TripDay> findByTripIdAndDayDate(Long tripId, LocalDate dayDate);

    Optional<TripDay> findByTripIdAndDayIndex(Long tripId, Integer dayIndex);
}
