package trip.diary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trip.diary.dto.TimelineDto;
import trip.diary.entity.Place;
import trip.diary.entity.TimelineItem;
import trip.diary.entity.Trip;
import trip.diary.entity.TripDay;
import trip.diary.repository.PlaceRepository;
import trip.diary.repository.TimelineItemRepository;
import trip.diary.repository.TripDayRepository;
import trip.diary.repository.TripRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimelineService {
    private final TripRepository tripRepository;
    private final TripDayRepository tripDayRepository;
    private final TimelineItemRepository timelineItemRepository;
    private final PlaceRepository placeRepository;

    //타임라인 별 아이템들 조회
    @Transactional(readOnly = true)
    public TimelineDto.TimelineListResponse getTimeline(Long tripId){
        //trip 있는지 확인
        tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("trip not found"));

        //타임라인들 가져오기
        List<TripDay> days = tripDayRepository.findByTripIdOrderByDayDateAsc(tripId);
        if (days.isEmpty()) {
            return new TimelineDto.TimelineListResponse(List.of());
        }

        //일정 아이템들 조회
        List<Long> dayIds = days.stream().map(TripDay::getId).toList();
        List<TimelineItem> items = timelineItemRepository
                .findByDayIdInOrderByDayIdAscStartTimeAsc(dayIds);

        // 각각의 아이템의 placeIds 모으기
        Set<Long> placeIds = items.stream()
                .map(TimelineItem::getPlace)
                .filter(Objects::nonNull)
                .map(Place::getId)
                .collect(Collectors.toSet());
        //placeIds 모은걸로 한꺼번에 조회하기(쿼리 횟수 줄이기위해)
        Map<Long, String> placeNameMap = new HashMap<>();
        if (!placeIds.isEmpty()) {
            List<Place> places = placeRepository.findByIdInAndTripId(placeIds, tripId);
            placeNameMap = places.stream()
                    .collect(Collectors.toMap(Place::getId, Place::getName, (a, b) -> a));
        }

        // dayId -> items 매핑(itemsByDayId)
        Map<Long, List<TimelineDto.TimelineItemResponse>> itemsByDayId = new HashMap<>();

        for (TimelineItem it : items) {
            Long dayId = it.getDay().getId();

            Long placeId = (it.getPlace() == null) ? null : it.getPlace().getId();
            String placeName = (placeId == null) ? null : placeNameMap.get(placeId);

            TimelineDto.TimelineItemResponse resp = new TimelineDto.TimelineItemResponse(
                    it.getId(),
                    it.getStartTime(),
                    it.getEndTime(),
                    placeId,
                    placeName
            );

            itemsByDayId.computeIfAbsent(dayId, k -> new ArrayList<>()).add(resp);
        }


        // days 응답 구성
        List<TimelineDto.TripDayTimelineResponse> dayResponses = days.stream()
                .map(d -> new TimelineDto.TripDayTimelineResponse(
                        d.getId(),
                        d.getDayDate(),
                        d.getDayIndex(),
                        d.getThemeTitle(),
                        d.getDayNote(),
                        d.getBudgetPlanned(),
                        d.getBudgetSpent(),
                        itemsByDayId.getOrDefault(d.getId(), List.of())
                ))
                .toList();

        return new TimelineDto.TimelineListResponse(dayResponses);


    }

    //아이템 추가
    @Transactional
    public Long addTimelineItem(Long tripId, TimelineDto.TimelineItemCreateRequest request){
        //예외 처리
        if (request == null) throw new IllegalArgumentException("요청이 비어있습니다");
        if (request.dayDate() == null) throw new IllegalArgumentException("dayDate는 필수입니다");
        if (request.startTime() == null || request.endTime() == null)
            throw new IllegalArgumentException("startTime/endTime은 필수입니다");
        if (!request.startTime().isBefore(request.endTime()))
            throw new IllegalArgumentException("startTime은 endTime보다 빨라야 합니다");

        // trip 존재 체크
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("trip not found"));

        // 사용자가 선택한 day 찾기
        TripDay day = tripDayRepository.findByTripIdAndDayDate(tripId, request.dayDate())
                .orElseThrow(() -> new IllegalArgumentException("day not found"));


        Place place = null;
        if (request.placeId() != null) {
            // 사용자가 선택한 장소 찾기
            place = placeRepository.findByIdAndTripId(request.placeId(), tripId)
                    .orElseThrow(() -> new IllegalArgumentException("place not found"));
        }

        TimelineItem item = TimelineItem.create(day, request.startTime(), request.endTime(), place);
        TimelineItem saved = timelineItemRepository.save(item);

        return saved.getId();
    }

    //아이템 삭제
    @Transactional
    public void deleteTimelineItem(Long timelineItemId) {
        TimelineItem item = timelineItemRepository.findById(timelineItemId)
                .orElseThrow(() -> new IllegalArgumentException("timeline item not found"));

        timelineItemRepository.delete(item);
    }

}
