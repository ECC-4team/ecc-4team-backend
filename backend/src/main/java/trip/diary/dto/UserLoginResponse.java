package trip.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLoginResponse {
    private int code;
    private String message;
    private LoginData data;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class LoginData {
        private Long id;
        private String userId;
        private String accessToken;
    }
}