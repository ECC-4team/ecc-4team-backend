package trip.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLogoutResponse {
    private int code;
    private String message;
}