package trip.diary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import trip.diary.dto.PlaceRequest;
import trip.diary.dto.PlaceDetailResponse;
import trip.diary.dto.PlaceListResponse;
import trip.diary.entity.Place;
import trip.diary.entity.PlacePhoto;
import trip.diary.global.image.ImageStorageService;
import trip.diary.repository.PlacePhotoRepository;
import trip.diary.repository.PlaceRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripPlaceService {

    private final PlaceRepository placeRepository;
    private final PlacePhotoRepository placePhotoRepository;
    private final ImageStorageService imageStorageService;


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

        //특정 장소 조회
        Place place = placeRepository.findByIdAndTripId(placeId, tripId)
                .orElseThrow(() -> new IllegalArgumentException("place not found"));

        //대표 이미지 조회
        String coverImageUrl = placePhotoRepository
                .findByPlaceIdAndIsCoverTrue(place.getId())
                .map(PlacePhoto::getImageUrl)
                .orElse(null);

        //이미지들 조회
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

    @Transactional
    public void createPlace(Long tripId, PlaceRequest request, List<MultipartFile> images){

        //입력 확인
        if(request.name() == null ||request.category() == null ){
            throw new IllegalArgumentException("필수 입력칸이 비어있습니다");
        }

        Place place = Place.create(tripId, request.name(), request.description(), request.category());

        Place savedPlace= placeRepository.save(place);

        //이미지가 있다면 저장
        if (images != null) {
            savePhotos(savedPlace, images, request.coverIndex());
        }

    }

    @Transactional
    public void updatePlace(Long tripId,Long placeId,PlaceRequest request,List<MultipartFile> images){
        //장소 게시물 조회
        Place place=placeRepository.findByIdAndTripId(placeId,tripId)
                .orElseThrow(() -> new IllegalArgumentException("place not found"));;

        if (request == null) {
            throw new IllegalArgumentException("요청이 비어있습니다");
        }

        // null 아닌 것만 수정
        if (request.name() != null) {
            place.setName(request.name());
        }
        if (request.description() != null) {
            place.setDescription(request.description());
        }
        if (request.category() != null) {
            place.setCategory(request.category());
        }

        // 이미지가 오면 → 기존 삭제 후 전체 교체
        if (images != null) {
            placePhotoRepository.deleteByPlaceId(placeId);

            if (!images.isEmpty()) {
                savePhotos(place, images, request.coverIndex());
            }
        }
    }

    @Transactional
    public void deletePlace(Long tripId,Long placeId){
        //장소 게시물 조회
        Place place= placeRepository.findByIdAndTripId(placeId,tripId)
                .orElseThrow(() -> new IllegalArgumentException("place not found"));
        //사진 삭제
        placePhotoRepository.deleteByPlaceId(placeId);
        //장소 게시물 삭제
        placeRepository.delete(place);

    }



    private void savePhotos(Place place,List<MultipartFile> images,Integer coverIndex){
        //올바른 값인지 확인
        int cover = (coverIndex == null) ? 0 : coverIndex;
        if (cover < 0 || cover >= images.size()) cover = 0;

        //이미지 각각 저장하기
        for (int i = 0; i < images.size(); i++) {
            MultipartFile file = images.get(i);
            if (file == null || file.isEmpty()) continue;

            String imageUrl = imageStorageService.upload(file);

            Boolean isCover= i==cover;
            PlacePhoto photo=PlacePhoto.create(place,imageUrl,isCover);

            placePhotoRepository.save(photo);
        }
    }
}
