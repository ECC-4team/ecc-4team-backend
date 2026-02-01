package trip.diary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import trip.diary.dto.PlaceDetailResponse;
import trip.diary.dto.PlaceListResponse;
import trip.diary.entity.Place;
import trip.diary.entity.PlacePhoto;
import trip.diary.repository.PlacePhotoRepository;
import trip.diary.repository.PlaceRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripPlaceService {

    private final PlaceRepository placeRepository;
    private final PlacePhotoRepository placePhotoRepository;


    public List<PlaceListResponse> getPlaces(Long tripId) {
        // 장소 목록 조회
        List<Place> places = placeRepository.findByTripId(tripId);

        // 장소가 없으면 바로 반환
        if (places.isEmpty()) return List.of();

        // placeId 리스트 뽑기
        List<Long> placeIds = places.stream()
                .map(Place::getId)
                .toList();

        //대표이미지를 조회하고 placeId와 대표이미지를 묶는 맵 생성
        Map<Long,String> coverMap= placePhotoRepository.findByPlaceIdInAndIsCoverTrue(placeIds).stream()
                .collect(Collectors.toMap(pp->pp.getPlace().getId(), PlacePhoto::getImageUrl,(a, b) -> a));

        return places.stream()
                .map(p-> new PlaceListResponse(
                        p.getId(),
                        p.getTripId(),
                        p.getName(),
                        p.getDescription(),
                        p.getCategory(),
                        coverMap.get(p.getId()),
                        p.getCreatedAt(),
                        p.getUpdatedAt()
                )).toList();

    }


    public PlaceDetailResponse getPlace(Long tripId, Long placeId) {
        Place place = placeRepository.findByIdAndTripId(placeId, tripId)
                .orElseThrow(() -> new IllegalArgumentException("place not found"));


        String coverImageUrl = placePhotoRepository
                .findByPlaceIdAndIsCoverTrue(place.getId())
                .map(PlacePhoto::getImageUrl)
                .orElse(null);

        List<String> imageUrls = placePhotoRepository.findByPlaceId(place.getId())
                .stream()
                .map(PlacePhoto::getImageUrl)
                .toList();

        return new PlaceDetailResponse(
                place.getId(),
                place.getTripId(),
                place.getName(),
                place.getDescription(),
                place.getCategory(),
                coverImageUrl,
                imageUrls,
                place.getCreatedAt(),
                place.getUpdatedAt()
        );
    }


}
