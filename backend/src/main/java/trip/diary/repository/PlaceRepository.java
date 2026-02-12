package trip.diary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import trip.diary.entity.Place;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    // 장소 목록 조회
    List<Place> findByTrip_Id(Long tripId);

    // 장소 단건 조회
    Optional<Place> findByIdAndTrip_Id(Long id, Long tripId);

    // 특정 장소 목록 조회
    List<Place> findByIdInAndTrip_Id(Set<Long> placeIds, Long tripId);
}

