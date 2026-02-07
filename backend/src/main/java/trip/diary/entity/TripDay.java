package trip.diary.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Table(
        name = "trip_days",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_trip_days_trip_date", columnNames = {"trip_id", "day_date"}),
                @UniqueConstraint(name = "uk_trip_days_trip_index", columnNames = {"trip_id", "day_index"})
        }
)
public class TripDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "day_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(name = "day_date", nullable = false)
    private LocalDate dayDate;

    @Column(name = "day_index", nullable = false)
    private Integer dayIndex;

    @Column(name = "theme_title", length = 100)
    private String themeTitle;

    @Lob
    @Column(name = "day_note")
    private String dayNote;

    @Column(name = "budget_planned")
    private Integer budgetPlanned;

    @Column(name = "budget_spent")
    private Integer budgetSpent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static TripDay create(Trip trip, LocalDate dayDate, int dayIndex) {
        TripDay d = new TripDay();
        d.setTrip(trip);
        d.setDayDate(dayDate);
        d.setDayIndex(dayIndex);
        return d;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
