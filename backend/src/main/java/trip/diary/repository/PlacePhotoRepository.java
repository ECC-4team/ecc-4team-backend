package trip.diary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import trip.diary.entity.PlacePhoto;

import java.util.*;

public interface PlacePhotoRepository extends JpaRepository<PlacePhoto,Long> {

    Optional<PlacePhoto> findByPlaceIdAndIsCoverTrue(Long placeId);

    List<PlacePhoto> findByPlaceId(Long placeId);

    List<PlacePhoto> findByPlaceIdInAndIsCoverTrue(List<Long> placeIds);

    void deleteByPlaceId(Long placeId);
}
