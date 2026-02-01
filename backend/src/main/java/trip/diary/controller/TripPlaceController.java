package trip.diary.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import trip.diary.dto.PlaceDetailResponse;
import trip.diary.dto.PlaceListResponse;
import trip.diary.dto.SuccessResponse;
import trip.diary.service.TripPlaceService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/trips/{tripId}/places")
public class TripPlaceController {

    private final TripPlaceService tripPlaceService;

    // GET /trips/{tripId}/places
    @GetMapping
    public SuccessResponse<List<PlaceListResponse>> getPlaces( @PathVariable Long tripId){
        List<PlaceListResponse> places=tripPlaceService.getPlaces(tripId);
        return new SuccessResponse<>(true,places);
    }

    // GET /trips/{tripId}/places/{placeId}
    @GetMapping("/{placeId}")
    public SuccessResponse<PlaceDetailResponse> getPlace(
            @PathVariable Long tripId,
            @PathVariable Long placeId
    ) {
        PlaceDetailResponse place = tripPlaceService.getPlace(tripId, placeId);
        return new SuccessResponse<>(true, place);
    }



}
