package trip.diary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import trip.diary.entity.TimelineItem;

import java.util.Collection;
import java.util.List;

public interface TimelineItemRepository extends JpaRepository<TimelineItem,Long> {
    List<TimelineItem> findByDayIdInOrderByDayIdAscStartTimeAsc(Collection<Long> dayIds);

    List<TimelineItem> findByDayIdOrderByStartTimeAsc(Long dayId);
}
