package trip.diary.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(IllegalArgumentException e) {
        return new ErrorResponse(e.getMessage());
    }

    // @Valid 검증 실패 시 (NOT NULL 등)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldError().getDefaultMessage();
        return new ErrorResponse(errorMessage);
    }

    // DTO에 없는 필드가 들어왔을 때 발생하는 에러
    @ExceptionHandler(UnrecognizedPropertyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnrecognizedProperty(UnrecognizedPropertyException e) {
        String propertyName = e.getPropertyName(); // 문제가 된 필드 이름

        // 날짜 필드가 들어온 경우
        if ("startDate".equals(propertyName) || "endDate".equals(propertyName)) {
            return new ErrorResponse("날짜는 수정할 수 없습니다. 여행을 삭제하고 다시 생성해주세요.");
        }

        // 2. 그 외 모르는 필드가 들어온 경우
        return new ErrorResponse("허용되지 않은 입력값입니다: " + propertyName);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception e) {
        e.printStackTrace(); // 콘솔에 로그 출력
        // "서버 오류가 발생했습니다" 대신, 실제 에러 메시지를 반환하도록 수정
        return new ErrorResponse("서버 오류: " + e.toString() + " / " + e.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbidden(ForbiddenException e) {
        return new ErrorResponse(e.getMessage());
    }
}

