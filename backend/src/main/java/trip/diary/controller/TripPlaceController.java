package trip.diary.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import trip.diary.dto.PlaceRequest;
import trip.diary.dto.PlaceDetailResponse;
import trip.diary.dto.PlaceListResponse;
import trip.diary.dto.PlaceResponse;
import trip.diary.service.TripPlaceService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/trips/{tripId}/places")
public class TripPlaceController {

    private final TripPlaceService tripPlaceService;

    // GET /trips/{tripId}/places
    @GetMapping
    public ResponseEntity<List<PlaceListResponse>> getPlaces( @PathVariable Long tripId){
        List<PlaceListResponse> places=tripPlaceService.getPlaces(tripId);
        return ResponseEntity.ok(places);
    }

    // GET /trips/{tripId}/places/{placeId}
    @GetMapping("/{placeId}")
    public ResponseEntity<PlaceDetailResponse> getPlace(@PathVariable Long tripId, @PathVariable Long placeId) {
        PlaceDetailResponse place = tripPlaceService.getPlace(tripId, placeId);
        return ResponseEntity.ok(place);
    }

    //POST /trips/{tripId}/places
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<PlaceResponse> createPlace(@PathVariable Long tripId, @RequestPart("data") PlaceRequest request,
                            @RequestPart(value = "images",required = false) List<MultipartFile> images){
        Long placeId= tripPlaceService.createPlace(tripId,request,images);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PlaceResponse(placeId));
    }

    // PATCH /trips/{tripId}/places/{placeId}
    @PatchMapping(value = "/{placeId}", consumes = "multipart/form-data")
    public ResponseEntity<PlaceResponse> updatePlace(@PathVariable Long tripId, @PathVariable Long placeId,
            @RequestPart("data") PlaceRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        tripPlaceService.updatePlace(tripId, placeId, request, images);
        return ResponseEntity.ok(new PlaceResponse(placeId));
    }

    // DELETE /trips/{tripId}/places/{placeId}
    @DeleteMapping("/{placeId}")
    public ResponseEntity<Void> deletePlace(@PathVariable Long tripId, @PathVariable Long placeId) {
        tripPlaceService.deletePlace(tripId, placeId);
        return ResponseEntity.noContent().build();
    }

}
