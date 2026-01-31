package trip.diary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import trip.diary.dto.PlaceDetailResponse;
import trip.diary.dto.PlaceListResponse;
import trip.diary.entity.Place;
import trip.diary.repository.PlaceRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripPlaceService {
    private final PlaceRepository placeRepository;


    public List<PlaceListResponse> getPlaces(Long tripId) {
        return placeRepository.findByTripId(tripId).stream()
                .map(this::toListResponse)
                .toList();
    }


    public PlaceDetailResponse getPlace(Long tripId, Long placeId) {
        Place place = placeRepository.findByIdAndTripId(placeId, tripId)
                .orElseThrow(() -> new IllegalArgumentException("place not found"));

        return toDetailResponse(place);
    }

    private PlaceListResponse toListResponse(Place p) {
        return new PlaceListResponse(
                p.getId(),                     // placeId
                p.getTripId(),
                p.getName(),
                p.getDescription(),
                p.getCategory(),
                p.getCoverImageUrl(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

private PlaceDetailResponse toDetailResponse(Place p) {
    return new PlaceDetailResponse(
            p.getId(),                     // placeId
            p.getTripId(),
            p.getName(),
            p.getDescription(),
            p.getCategory(),
            p.getCoverImageUrl(),
            p.getImageUrls(),
            p.getCreatedAt(),
            p.getUpdatedAt()
    );
}

}
