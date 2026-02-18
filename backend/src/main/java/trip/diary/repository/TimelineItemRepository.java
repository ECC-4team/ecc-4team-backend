package trip.diary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import trip.diary.entity.TimelineItem;

import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface TimelineItemRepository extends JpaRepository<TimelineItem,Long> {

    List<TimelineItem> findByDay_IdInOrderByDay_IdAscStartTimeAsc(Collection<Long> dayIds);

    void deleteByDay_Trip_Id(Long tripId);

    // 같은 day 안에서 시간 겹치는 아이템이 존재하는지
    // overlap 조건: existing.start < newEnd AND existing.end > newStart
    boolean existsByDay_IdAndStartTimeLessThanAndEndTimeGreaterThan(
            Long dayId,
            LocalTime newEnd,
            LocalTime newStart
    );

    //자신을 제외하고 시간 겹치는지 확인
    boolean existsByDay_IdAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
            Long dayId,
            Long excludeTimelineItemId,
            LocalTime newEnd,
            LocalTime newStart
    );
}
