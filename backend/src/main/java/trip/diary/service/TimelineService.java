package trip.diary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trip.diary.dto.TimelineDto;
import trip.diary.dto.TimelineItemUpdateRequest;
import trip.diary.dto.TripDayBulkUpdateRequest;
import trip.diary.entity.Place;
import trip.diary.entity.TimelineItem;
import trip.diary.entity.TripDay;
import trip.diary.global.exception.NotFoundException;
import trip.diary.repository.PlaceRepository;
import trip.diary.repository.TimelineItemRepository;
import trip.diary.repository.TripDayRepository;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimelineService {

    private final TripDayRepository tripDayRepository;
    private final TimelineItemRepository timelineItemRepository;
    private final PlaceRepository placeRepository;
    private final TripAuthorizationService tripAuthorizationService;

    @Transactional(readOnly = true)
    public TimelineDto.TimelineListResponse getTimeline(Long tripId, String userId) {
        tripAuthorizationService.getAuthorizedTrip(tripId, userId);

        List<TripDay> days = tripDayRepository.findByTrip_IdOrderByDayDateAsc(tripId);
        if (days.isEmpty()) {
            return new TimelineDto.TimelineListResponse(List.of());
        }

        List<Long> dayIds = days.stream().map(TripDay::getId).toList();
        List<TimelineItem> items = timelineItemRepository
                .findByDay_IdInOrderByDay_IdAscStartTimeAsc(dayIds);

        Set<Long> placeIds = items.stream()
                .map(TimelineItem::getPlace)
                .filter(Objects::nonNull)
                .map(Place::getId)
                .collect(Collectors.toSet());

        Map<Long, String> placeNameMap = new HashMap<>();
        if (!placeIds.isEmpty()) {
            List<Place> places = placeRepository.findByIdInAndTrip_Id(placeIds, tripId);
            placeNameMap = places.stream()
                    .collect(Collectors.toMap(Place::getId, Place::getName, (a, b) -> a));
        }

        Map<Long, List<TimelineDto.TimelineItemResponse>> itemsByDayId = new HashMap<>();
        for (TimelineItem item : items) {
            Long dayId = item.getDay().getId();
            Long placeId = item.getPlace().getId();
            String placeName = placeNameMap.get(placeId);

            TimelineDto.TimelineItemResponse response = new TimelineDto.TimelineItemResponse(
                    item.getId(),
                    item.getStartTime(),
                    item.getEndTime(),
                    placeId,
                    placeName
            );
            itemsByDayId.computeIfAbsent(dayId, k -> new ArrayList<>()).add(response);
        }

        List<TimelineDto.TripDayTimelineResponse> dayResponses = days.stream()
                .map(day -> new TimelineDto.TripDayTimelineResponse(
                        day.getId(),
                        day.getDayDate(),
                        day.getDayIndex(),
                        day.getThemeTitle(),
                        day.getDayNote(),
                        day.getBudgetPlanned(),
                        day.getBudgetSpent(),
                        itemsByDayId.getOrDefault(day.getId(), List.of())
                ))
                .toList();

        return new TimelineDto.TimelineListResponse(dayResponses);
    }

    @Transactional
    public Long addTimelineItem(Long tripId, TimelineDto.TimelineItemCreateRequest request, String userId) {
        tripAuthorizationService.getAuthorizedTrip(tripId, userId);

        if (request == null) throw new IllegalArgumentException("request is required");
        if (request.dayDate() == null) throw new IllegalArgumentException("dayDate is required");
        if (request.startTime() == null || request.endTime() == null) {
            throw new IllegalArgumentException("startTime/endTime is required");
        }
        if (!request.startTime().isBefore(request.endTime())) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }
        if (request.placeId() == null) throw new IllegalArgumentException("placeId is required");

        TripDay day = tripDayRepository.findByTrip_IdAndDayDate(tripId, request.dayDate())
                .orElseThrow(() -> new IllegalArgumentException("day not found"));

        boolean overlapped = timelineItemRepository
                .existsByDay_IdAndStartTimeLessThanAndEndTimeGreaterThan(
                        day.getId(), request.endTime(), request.startTime());
        if (overlapped) {
            throw new IllegalArgumentException("time overlap exists");
        }

        Place place = placeRepository.findByIdAndTrip_Id(request.placeId(), tripId)
                .orElseThrow(() -> new IllegalArgumentException("place not found"));

        TimelineItem item = TimelineItem.create(day, request.startTime(), request.endTime(), place);
        TimelineItem saved = timelineItemRepository.save(item);
        return saved.getId();
    }

    @Transactional
    public void deleteTimelineItem(Long timelineItemId, String userId) {
        TimelineItem item = timelineItemRepository.findById(timelineItemId)
                .orElseThrow(() -> new IllegalArgumentException("timeline item not found"));

        Long tripId = item.getDay().getTrip().getId();
        tripAuthorizationService.getAuthorizedTrip(tripId, userId);
        timelineItemRepository.delete(item);
    }

    @Transactional
    public void updateTripDays(Long tripId, TripDayBulkUpdateRequest request, String userId) {
        tripAuthorizationService.getAuthorizedTrip(tripId, userId);

        if (request == null || request.getDays() == null || request.getDays().isEmpty()) {
            throw new IllegalArgumentException("days is required");
        }

        List<Long> dayIds = request.getDays().stream()
                .map(TripDayBulkUpdateRequest.TripDayUpdateItem::getDayId)
                .filter(Objects::nonNull)
                .toList();

        if (dayIds.size() != request.getDays().size()) {
            throw new IllegalArgumentException("dayId is required for all items");
        }

        List<TripDay> found = tripDayRepository.findAllByTrip_IdAndIdIn(tripId, dayIds);
        Map<Long, TripDay> dayMap = found.stream()
                .collect(Collectors.toMap(TripDay::getId, Function.identity()));

        for (TripDayBulkUpdateRequest.TripDayUpdateItem item : request.getDays()) {
            TripDay day = dayMap.get(item.getDayId());
            if (day == null) {
                throw new NotFoundException("TripDay not found: " + item.getDayId());
            }

            day.setThemeTitle(item.getThemeTitle());
            day.setDayNote(item.getDayNote());
            day.setBudgetPlanned(item.getBudgetPlanned());
            day.setBudgetSpent(item.getBudgetSpent());
        }
    }

    @Transactional
    public void updateTimelineItem(Long tripId, Long timelineId, TimelineItemUpdateRequest request, String userId) {
        tripAuthorizationService.getAuthorizedTrip(tripId, userId);

        if (request.dayDate() == null) throw new IllegalArgumentException("dayDate is required");
        if (request.startTime() == null || request.endTime() == null) {
            throw new IllegalArgumentException("startTime/endTime is required");
        }
        if (!request.startTime().isBefore(request.endTime())) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }
        if (request.placeId() == null) throw new IllegalArgumentException("placeId is required");

        TimelineItem item = timelineItemRepository.findById(timelineId)
                .orElseThrow(() -> new IllegalArgumentException("timeline item not found"));

        Long itemTripId = item.getDay().getTrip().getId();
        if (!itemTripId.equals(tripId)) {
            throw new IllegalArgumentException("trip mismatch");
        }

        TripDay targetDay = tripDayRepository.findByTrip_IdAndDayDate(tripId, request.dayDate())
                .orElseThrow(() -> new IllegalArgumentException("day not found"));

        boolean overlapped = timelineItemRepository
                .existsByDay_IdAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
                        targetDay.getId(), timelineId, request.endTime(), request.startTime());
        if (overlapped) {
            throw new IllegalArgumentException("time overlap exists");
        }

        Place place = placeRepository.findByIdAndTrip_Id(request.placeId(), tripId)
                .orElseThrow(() -> new IllegalArgumentException("place not found"));

        item.setDay(targetDay);
        item.setStartTime(request.startTime());
        item.setEndTime(request.endTime());
        item.setPlace(place);
    }
}
