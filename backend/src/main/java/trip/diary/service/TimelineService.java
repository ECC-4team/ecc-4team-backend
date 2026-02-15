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
import trip.diary.repository.PlaceRepository;
import trip.diary.repository.TimelineItemRepository;
import trip.diary.repository.TripDayRepository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.time.LocalTime;


@Service
@RequiredArgsConstructor
public class TimelineService {

    private final TripDayRepository tripDayRepository;
    private final TimelineItemRepository timelineItemRepository;
    private final PlaceRepository placeRepository;
    private final TripAuthorizationService tripAuthorizationService;

    //타임라인 별 아이템들 조회
    @Transactional(readOnly = true)
    public TimelineDto.TimelineListResponse getTimeline(Long tripId, String userId){
        tripAuthorizationService.getAuthorizedTrip(tripId, userId);

        //타임라인들 가져오기
        List<TripDay> days = tripDayRepository.findByTrip_IdOrderByDayDateAsc(tripId);
        if (days.isEmpty()) {
            return new TimelineDto.TimelineListResponse(List.of());
        }

        //일정 아이템들 조회
        List<Long> dayIds = days.stream().map(TripDay::getId).toList();
        List<TimelineItem> items = timelineItemRepository
                .findByDay_IdInOrderByDay_IdAscStartTimeAsc(dayIds);

        // 각각의 아이템의 placeIds 모으기
        Set<Long> placeIds = items.stream()
                .map(TimelineItem::getPlace)
                .filter(Objects::nonNull)
                .map(Place::getId)
                .collect(Collectors.toSet());
        //placeIds 모은걸로 한꺼번에 조회하기(쿼리 횟수 줄이기위해)
        Map<Long, String> placeNameMap = new HashMap<>();
        if (!placeIds.isEmpty()) {
            List<Place> places = placeRepository.findByIdInAndTrip_Id(placeIds, tripId);
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
    public Long addTimelineItem(Long tripId, TimelineDto.TimelineItemCreateRequest request, String userId){
        tripAuthorizationService.getAuthorizedTrip(tripId, userId);

        //예외 처리
        if (request == null) throw new IllegalArgumentException("요청이 비어있습니다");
        if (request.dayDate() == null) throw new IllegalArgumentException("dayDate는 필수입니다");

        LocalTime start = request.startTime();
        LocalTime end = request.endTime();

        if (start == null || end == null)
            throw new IllegalArgumentException("startTime/endTime은 필수입니다");
        if (!start.isBefore(end))
            throw new IllegalArgumentException("startTime은 endTime보다 빨라야 합니다");


        // 사용자가 선택한 day 찾기
        TripDay day = tripDayRepository.findByTrip_IdAndDayDate(tripId, request.dayDate())
                .orElseThrow(() -> new IllegalArgumentException("day not found"));


        // 겹침 체크 (같은 day 안에서의)
        boolean overlapped = timelineItemRepository
                .existsByDay_IdAndStartTimeLessThanAndEndTimeGreaterThan(day.getId(), end, start);

        if (overlapped) {
            throw new IllegalArgumentException("이미 해당 시간대에 일정이 존재합니다");
        }

        Place place = null;
        if (request.placeId() != null) {
            // 사용자가 선택한 장소 찾기
            place = placeRepository.findByIdAndTrip_Id(request.placeId(), tripId)
                    .orElseThrow(() -> new IllegalArgumentException("place not found"));
        }

        //저장
        TimelineItem item = TimelineItem.create(day, request.startTime(), request.endTime(), place);
        TimelineItem saved = timelineItemRepository.save(item);

        return saved.getId();
    }

    //아이템 삭제
    @Transactional
    public void deleteTimelineItem(Long timelineItemId, String userId) {
        TimelineItem item = timelineItemRepository.findById(timelineItemId)
                .orElseThrow(() -> new IllegalArgumentException("timeline item not found"));
        Long tripId = item.getDay().getTrip().getId();
        tripAuthorizationService.getAuthorizedTrip(tripId, userId);

        timelineItemRepository.delete(item);
    }


    @Transactional
    public void updateTripDays(Long tripId, TripDayBulkUpdateRequest req, String userId){
        tripAuthorizationService.getAuthorizedTrip(tripId, userId);

        //제대로 값이 들어왔는지 확인
        if (req == null || req.getDays() == null || req.getDays().isEmpty()) {
            throw new IllegalArgumentException("days is required");
        }

        //null이 아닌 dayId들 리스트 생성
        List<Long> dayIds = req.getDays().stream()
                .map(TripDayBulkUpdateRequest.TripDayUpdateItem::getDayId)
                .filter(Objects::nonNull)
                .toList();

        //모든 day가 다 들어왔는지 확인
        if (dayIds.size() != req.getDays().size()) {
            throw new IllegalArgumentException("dayId is required for all items");
        }

        //TripDay 리포지토리 조회
        List<TripDay> found = tripDayRepository.findAllByTrip_IdAndIdIn(tripId, dayIds);

        //id랑 찾은 TripDay를 묶은 map 만들기
        Map<Long, TripDay> map = found.stream()
                .collect(Collectors.toMap(TripDay::getId, Function.identity()));


        for (TripDayBulkUpdateRequest.TripDayUpdateItem item : req.getDays()) {

            TripDay day = map.get(item.getDayId());

            //디비에 존재하는건지 확인
            if (day == null) {
                throw new NoSuchElementException("TripDay not found: " + item.getDayId());
            }

            //수정하기-> 알아서 저장됨(dirty checking)
            day.setThemeTitle(item.getThemeTitle());
            day.setDayNote(item.getDayNote());
            day.setBudgetPlanned(item.getBudgetPlanned());
            day.setBudgetSpent(item.getBudgetSpent());
        }
    }

    @Transactional
    public void updateTimelineItem(Long tripId, Long timelineId, TimelineItemUpdateRequest request, String userId){
        tripAuthorizationService.getAuthorizedTrip(tripId, userId);
        if (request.dayDate()== null) throw new IllegalArgumentException("dayDate는 필수입니다");
        if (request.startTime() == null || request.endTime() == null) throw new IllegalArgumentException("startTime/endTime은 필수입니다");
        if (!request.startTime().isBefore(request.endTime())) {
            throw new IllegalArgumentException("startTime은 endTime보다 빨라야 합니다");
        }

        // 수정 대상 item 조회
        TimelineItem item = timelineItemRepository.findById(timelineId)
                .orElseThrow(() -> new IllegalArgumentException("timeline item not found"));

        // 소속 검증: timelineId가 이 trip의 일정이 맞는지
        Long itemTripId = item.getDay().getTrip().getId();
        if (!itemTripId.equals(tripId)) {
            throw new IllegalArgumentException("trip mismatch");
        }

        // target day 조회 (날짜 이동 허용)
        TripDay targetDay = tripDayRepository.findByTrip_IdAndDayDate(tripId, request.dayDate())
                .orElseThrow(() -> new IllegalArgumentException("day not found"));

        // 겹침 체크 (자기 자신 제외)
        boolean overlapped = timelineItemRepository
                .existsByDay_IdAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
                        targetDay.getId(), timelineId, request.endTime(), request.startTime()
                );
        if (overlapped) {
            throw new IllegalArgumentException("이미 해당 시간대에 일정이 존재합니다");
        }

        //장소 존재하는지 확인하고 가져오기
        Place place = null;
        if (request.placeId() != null) {
            place = placeRepository.findByIdAndTrip_Id(request.placeId(), tripId)
                    .orElseThrow(() -> new IllegalArgumentException("place not found"));
        }

        // 반영-> 저장 안해도 됨(dirty checking)
        item.setDay(targetDay);
        item.setStartTime(request.startTime());
        item.setEndTime(request.endTime());
        item.setPlace(place);
    }

}
