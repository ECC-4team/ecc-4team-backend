package trip.diary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import trip.diary.dto.PlaceDetailResponse;
import trip.diary.dto.PlaceListResponse;
import trip.diary.dto.PlaceRequest;
import trip.diary.entity.Place;
import trip.diary.entity.PlacePhoto;
import trip.diary.entity.Trip;
import trip.diary.global.exception.NotFoundException;
import trip.diary.global.image.ImageStorageService;
import trip.diary.repository.PlacePhotoRepository;
import trip.diary.repository.PlaceRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripPlaceService {

    private final PlaceRepository placeRepository;
    private final PlacePhotoRepository placePhotoRepository;
    private final ImageStorageService imageStorageService;
    private final TripAuthorizationService tripAuthorizationService;

    private static final String DEFAULT_PLACE_IMAGE_URL = "https://res.cloudinary.com/dxlycqpyp/image/upload/v1771146721/KakaoTalk_20260215_125901244_nzvsch.png";

    private static final Map<String, String> CATEGORY_DEFAULT_IMAGE_MAP = Map.of(
            "쇼핑", "https://res.cloudinary.com/dxlycqpyp/image/upload/v1771146720/KakaoTalk_20260215_125850088_02_qrxjmg.png",
            "체험", "https://res.cloudinary.com/dxlycqpyp/image/upload/v1771146720/KakaoTalk_20260215_125850088_iarx8o.png",
            "숙소", "https://res.cloudinary.com/dxlycqpyp/image/upload/v1771146720/KakaoTalk_20260215_125850088_03_xifp0v.png",
            "관광", "https://res.cloudinary.com/dxlycqpyp/image/upload/v1771146720/KakaoTalk_20260215_125850088_01_uwezty.png",
            "맛집", "https://res.cloudinary.com/dxlycqpyp/image/upload/v1771146720/KakaoTalk_20260215_125850088_04_a8ixcj.png",
            "카페/디저트", "https://res.cloudinary.com/dxlycqpyp/image/upload/v1771146720/KakaoTalk_20260215_125850088_05_gy3bvu.png"
    );

    public List<PlaceListResponse> getPlaces(Long tripId, String userId) {
        tripAuthorizationService.getAuthorizedTrip(tripId, userId);

        List<Place> places = placeRepository.findByTrip_Id(tripId);
        if (places.isEmpty()) return List.of();

        List<Long> placeIds = places.stream()
                .map(Place::getId)
                .toList();

        Map<Long, String> coverMap = placePhotoRepository.findByPlace_IdInAndIsCoverTrue(placeIds).stream()
                .collect(Collectors.toMap(pp -> pp.getPlace().getId(), PlacePhoto::getImageUrl, (a, b) -> a));

        return places.stream()
                .map(p -> new PlaceListResponse(
                        p.getId(),
                        p.getTripId(),
                        p.getName(),
                        p.getDescription(),
                        p.getCategory(),
                        coverMap.get(p.getId()),
                        p.getCreatedAt(),
                        p.getUpdatedAt()
                ))
                .toList();
    }

    public PlaceDetailResponse getPlace(Long tripId, Long placeId, String userId) {
        tripAuthorizationService.getAuthorizedTrip(tripId, userId);

        Place place = placeRepository.findByIdAndTrip_Id(placeId, tripId)
                .orElseThrow(() -> new NotFoundException("place not found"));

        String coverImageUrl = placePhotoRepository
                .findByPlace_IdAndIsCoverTrue(place.getId())
                .map(PlacePhoto::getImageUrl)
                .orElse(null);

        List<String> imageUrls = placePhotoRepository.findByPlace_Id(place.getId())
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

    @Transactional
    public Long createPlace(Long tripId, PlaceRequest request, List<MultipartFile> images, String userId) {
        Trip trip = tripAuthorizationService.getAuthorizedTrip(tripId, userId);

        if (request == null) {
            throw new IllegalArgumentException("요청이 비어있습니다");
        }
        if (request.name() == null) {
            throw new IllegalArgumentException("필수 입력칸이 비어있습니다");
        }

        Place place = Place.create(trip, request.name(), request.description(), request.category());
        Place savedPlace = placeRepository.save(place);

        if (images != null && !images.isEmpty()) {
            savePhotos(savedPlace, images, request.coverIndex());
        } else {
            saveDefaultCoverPhoto(savedPlace);
        }

        return savedPlace.getId();
    }

    @Transactional
    public void updatePlace(Long tripId, Long placeId, PlaceRequest request, List<MultipartFile> images, String userId) {
        tripAuthorizationService.getAuthorizedTrip(tripId, userId);

        Place place = placeRepository.findByIdAndTrip_Id(placeId, tripId)
                .orElseThrow(() -> new NotFoundException("place not found"));

        if (request == null) {
            throw new IllegalArgumentException("요청이 비어있습니다");
        }

        if (request.name() != null) {
            place.setName(request.name());
        }
        if (request.description() != null) {
            place.setDescription(request.description());
        }
        if (request.category() != null) {
            place.setCategory(request.category());
        }

        if (images != null) {
            placePhotoRepository.deleteByPlace_Id(placeId);

            if (!images.isEmpty()) {
                savePhotos(place, images, request.coverIndex());
            } else {
                saveDefaultCoverPhoto(place);
            }
        }
    }

    @Transactional
    public void deletePlace(Long tripId, Long placeId, String userId) {
        tripAuthorizationService.getAuthorizedTrip(tripId, userId);

        Place place = placeRepository.findByIdAndTrip_Id(placeId, tripId)
                .orElseThrow(() -> new NotFoundException("place not found"));

        placePhotoRepository.deleteByPlace_Id(placeId);
        placeRepository.delete(place);
    }

    private void saveDefaultCoverPhoto(Place place) {
        String category = place.getCategory();
        String defaultImageUrl;

        if (category == null || category.isBlank()) {
            defaultImageUrl = DEFAULT_PLACE_IMAGE_URL;
        } else {
            defaultImageUrl = CATEGORY_DEFAULT_IMAGE_MAP.getOrDefault(category.trim(), DEFAULT_PLACE_IMAGE_URL);
        }

        PlacePhoto defaultPhoto = PlacePhoto.create(place, defaultImageUrl, true);
        placePhotoRepository.save(defaultPhoto);
    }

    private void savePhotos(Place place, List<MultipartFile> images, Integer coverIndex) {
        if (images == null || images.isEmpty()) return;

        int cover = (coverIndex == null) ? 0 : coverIndex;
        if (cover < 0 || cover >= images.size()) cover = 0;

        PlacePhoto firstSaved = null;
        PlacePhoto coverSaved = null;

        for (int i = 0; i < images.size(); i++) {
            MultipartFile file = images.get(i);
            if (file == null || file.isEmpty()) continue;

            String imageUrl = imageStorageService.upload(file);
            boolean isCover = (i == cover);
            PlacePhoto photo = PlacePhoto.create(place, imageUrl, isCover);
            PlacePhoto saved = placePhotoRepository.save(photo);

            if (firstSaved == null) firstSaved = saved;
            if (isCover) coverSaved = saved;
        }

        if (firstSaved != null && coverSaved == null) {
            firstSaved.setIsCover(true);
            placePhotoRepository.save(firstSaved);
        }
    }
}
