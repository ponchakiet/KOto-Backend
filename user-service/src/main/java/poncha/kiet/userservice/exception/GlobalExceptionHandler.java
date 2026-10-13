package poncha.kiet.userservice.exception;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import poncha.kiet.userservice.dto.response.Response;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<Response> handleStatusRuntimeException(StatusRuntimeException ex, HttpServletRequest req) {
        Status.Code code = ex.getStatus().getCode();
        String message = ex.getStatus().getDescription();
        HttpStatus httpStatus;

        switch (code) {
            case NOT_FOUND:
                httpStatus = HttpStatus.NOT_FOUND;
                break;
            case UNAVAILABLE:
                httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
                break;
            case DEADLINE_EXCEEDED:
                httpStatus = HttpStatus.GATEWAY_TIMEOUT;
                break;
            case INVALID_ARGUMENT:
                httpStatus = HttpStatus.BAD_REQUEST;
                break;
            default:
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        Response<?> errorResponse = Response.builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .errors(httpStatus.value())
                .build();
        return new ResponseEntity<>(errorResponse, httpStatus);
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Response<?>> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        Response<?> errorResponse = Response.builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .errors(HttpStatus.NOT_FOUND.getReasonPhrase())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Response<?>> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        Response<?> errorResponse = Response.builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .errors(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Response<?>> handleDuplicateResourceException(DuplicateResourceException ex, HttpServletRequest request) {
        Response<?> errorResponse = Response.builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .errors(HttpStatus.CONFLICT.getReasonPhrase())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response<?>> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        Response<?> errorResponse = Response.builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .errors(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<?>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = "Validation failed";
        java.util.List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        Response<?> errorResponse = Response.builder()
                .success(false)
                .message(message)
                .errors(details)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<?>> handleGlobalException(Exception ex, HttpServletRequest request) {
        Response<?> errorResponse = Response.builder()
                .success(false)
                .message(ex.getMessage() != null ? ex.getMessage() : "Internal Server Error")
                .timestamp(LocalDateTime.now())
                .errors(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
