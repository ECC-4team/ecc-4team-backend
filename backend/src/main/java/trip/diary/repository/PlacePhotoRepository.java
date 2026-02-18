package trip.diary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import trip.diary.entity.PlacePhoto;

import java.util.*;

public interface PlacePhotoRepository extends JpaRepository<PlacePhoto,Long> {

    Optional<PlacePhoto> findByPlace_IdAndIsCoverTrue(Long placeId);

    List<PlacePhoto> findByPlace_Id(Long placeId);

    List<PlacePhoto> findByPlace_IdInAndIsCoverTrue(List<Long> placeIds);

    void deleteByPlace_Trip_Id(Long tripId);

    void deleteByPlace_Id(Long placeId);
}
