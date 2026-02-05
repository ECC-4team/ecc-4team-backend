package trip.diary.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import trip.diary.dto.TripCreateRequest;
import trip.diary.dto.TripCreateResponse;
import trip.diary.dto.TripListResponse;
import trip.diary.dto.TripUpdateRequest;
import trip.diary.service.TripService;

import java.util.Map;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<TripCreateResponse> createTrip(
            @RequestBody @Valid TripCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails // 토큰에서 유저 정보(userId) 추출
    ) {
        // userDetails.getUsername()에는 userId(로그인 아이디)가 들어감
        TripCreateResponse response = tripService.createTrip(request, userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<TripListResponse> getTrips(@AuthenticationPrincipal UserDetails userDetails) {
        // 서비스 호출
        TripListResponse response = tripService.getTrips(userDetails.getUsername());

        // 200 OK 응답
        return ResponseEntity.ok(response);
    }

    // 여행 상세 조회
    @GetMapping("/{tripId}")
    public ResponseEntity<Map<String, Object>> getTripDetail(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 서비스 호출
        Map<String, Object> response = tripService.getTripDetail(tripId, userDetails.getUsername());

        return ResponseEntity.ok(response);
    }

    // 여행 수정 API
    @PatchMapping("/{tripId}")
    public ResponseEntity<Map<String, Object>> updateTrip(
            @PathVariable Long tripId,
            @RequestBody TripUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = tripService.updateTrip(tripId, request, userDetails.getUsername());

        return ResponseEntity.ok(response);
    }

    // 여행 삭제 API
    @DeleteMapping("/{tripId}")
    public ResponseEntity<Map<String, Object>> deleteTrip(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = tripService.deleteTrip(tripId, userDetails.getUsername());

        return ResponseEntity.ok(response);
    }
}