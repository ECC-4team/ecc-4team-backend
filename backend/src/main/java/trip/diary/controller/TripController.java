package trip.diary.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import trip.diary.dto.*;
import trip.diary.service.TripService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    // 여행 생성
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> createTrip(
            @RequestBody @Valid TripCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long tripId = tripService.createTrip(request, userDetails.getUsername());

        // { success: true, data: { "tripId": 1 } }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(Map.of("tripId", tripId)));
    }

    // 여행 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<TripDto>>> getTrips(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<TripDto> trips = tripService.getTrips(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(trips));
    }

    // 여행 상세 조회
    @GetMapping("/{tripId}")
    public ResponseEntity<ApiResponse<TripDetailDto>> getTripDetail(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails userDetails) {

        TripDetailDto data = tripService.getTripDetail(tripId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    // 여행 수정
    @PatchMapping("/{tripId}")
    public ResponseEntity<ApiResponse<TripDetailDto>> updateTrip(
            @PathVariable Long tripId,
            @RequestBody TripUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        TripDetailDto data = tripService.updateTrip(tripId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    // 여행 삭제
    @DeleteMapping("/{tripId}")
    public ResponseEntity<ApiResponse<Void>> deleteTrip(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails userDetails) {

        tripService.deleteTrip(tripId, userDetails.getUsername());

        // { success: true, data: null }
        return ResponseEntity.ok(ApiResponse.ok());
    }
}