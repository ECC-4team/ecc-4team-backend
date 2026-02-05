package trip.diary.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trip.diary.dto.ApiResponse;
import trip.diary.dto.UserLoginDto;
import trip.diary.dto.UserLoginRequest;
import trip.diary.dto.UserSignupRequest;
import trip.diary.entity.User;
import trip.diary.service.UserService;
import java.util.Map;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Map<String, String>>> signup(@RequestBody @Valid UserSignupRequest request) {
        User savedUser = userService.signup(request);

        // { success: true, data: { "userId": "...", "message": "..." } }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of(
                        "userId", savedUser.getUserId(),
                        "message", "회원가입이 완료되었습니다."
                )));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserLoginDto>> login(@RequestBody @Valid UserLoginRequest request) {
        UserLoginDto loginDto = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success(loginDto));
    }

    // 로그아웃
    /*
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(ApiResponse.ok("로그아웃 되었습니다."));
    }
     */
    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        userService.logout(authHeader);
        return ApiResponse.success("로그아웃 되었습니다.");
    }

    // 내 정보 확인 (테스트용)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> myInfo() {
        return ResponseEntity.ok(ApiResponse.success("인증된 유저입니다!"));
    }
}