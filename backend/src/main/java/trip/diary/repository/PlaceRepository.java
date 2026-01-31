package trip.diary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import trip.diary.entity.Place;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place,Long> {

    // 장소 목록 조회
    List<Place> findByTripId(Long tripId);
    // 장소 단건 조회
    Optional<Place> findByIdAndTripId(Long id,Long tripId);

}
