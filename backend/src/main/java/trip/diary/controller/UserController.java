package trip.diary.controller;

import jakarta.validation.Valid;
import trip.diary.dto.UserLoginRequest;
import trip.diary.dto.UserLoginResponse;
import trip.diary.dto.UserSignupRequest;
import trip.diary.entity.User;
import trip.diary.service.UserService;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody @Valid UserSignupRequest request) {
        User savedUser = userService.signup(request);

        // 응답 데이터 (data)
        UserData userData = new UserData(
                savedUser.getId(),
                savedUser.getUserId(),
                savedUser.getCreatedAt()
        );

        // 전체 응답 구조
        SignupResponse response = new SignupResponse(
                201,
                "회원가입이 완료되었습니다.",
                userData
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --- 응답 DTO (내부 클래스) ---
    @Data
    @AllArgsConstructor
    static class SignupResponse {
        private int code;
        private String message;
        private UserData data;
    }

    @Data
    @AllArgsConstructor
    static class UserData {
        private Long id;
        private String userId;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        private LocalDateTime createdAt;
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody @Valid UserLoginRequest request) {
        UserLoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }
}