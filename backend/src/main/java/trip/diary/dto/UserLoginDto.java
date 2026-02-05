package trip.diary.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserLoginDto {
    private Long id;
    private String userId;
    private String accessToken;
}