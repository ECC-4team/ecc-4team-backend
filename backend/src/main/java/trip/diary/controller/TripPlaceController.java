package trip.diary.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trip.diary.dto.PlaceRequest;
import trip.diary.dto.PlaceDetailResponse;
import trip.diary.dto.PlaceListResponse;
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
    @PostMapping
    public void createPlace(@PathVariable Long tripId, @RequestBody PlaceRequest request){


    }

}
