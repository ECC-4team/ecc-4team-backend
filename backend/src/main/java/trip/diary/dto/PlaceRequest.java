package trip.diary.dto;

public record PlaceRequest(String name,
                           String description,
                           String category,
                           Integer coverIndex) {
}
