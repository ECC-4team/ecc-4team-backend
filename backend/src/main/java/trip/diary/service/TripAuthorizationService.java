package trip.diary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import trip.diary.entity.Trip;
import trip.diary.global.exception.ForbiddenException;
import trip.diary.global.exception.NotFoundException;
import trip.diary.repository.TripRepository;

@Component
@RequiredArgsConstructor
public class TripAuthorizationService {

    private final TripRepository tripRepository;

    public Trip getAuthorizedTrip(Long tripId, String userId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException("trip not found"));

        if (!trip.getUser().getUserId().equals(userId)) {
            throw new ForbiddenException("권한이 없습니다.");
        }
        return trip;
    }
}
