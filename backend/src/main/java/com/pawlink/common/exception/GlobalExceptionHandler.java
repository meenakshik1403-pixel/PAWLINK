package com.pawlink.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized Global Exception Handler.
 * Intercepts application exceptions and returns uniform JSON: { "error": "message" }
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, String> errorPayload(String message) {
        Map<String, String> body = new HashMap<>();
        body.put("error", message != null ? message : "An unexpected error occurred.");
        return body;
    }

    // 409 Conflict: Slot Unavailable / Duplicate Email / Duplicate Record
    @ExceptionHandler(SlotUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleSlotUnavailable(SlotUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorPayload(ex.getMessage()));
    }

    @ExceptionHandler(AccountPendingApprovalException.class)
    public ResponseEntity<Map<String, String>> handleAccountPendingApproval(AccountPendingApprovalException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorPayload(ex.getMessage()));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorPayload(ex.getMessage()));
    }

    // 400 Bad Request: Invalid Pet Data / Unverified Sitter / Payment Failed / Illegal Arguments
    @ExceptionHandler(InvalidPetDataException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPetData(InvalidPetDataException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorPayload(ex.getMessage()));
    }

    @ExceptionHandler(UnverifiedSitterException.class)
    public ResponseEntity<Map<String, String>> handleUnverifiedSitter(UnverifiedSitterException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorPayload(ex.getMessage()));
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<Map<String, String>> handlePaymentFailed(PaymentFailedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorPayload(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorPayload(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        StringBuilder sb = new StringBuilder();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(error.getField()).append(": ").append(error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorPayload(sb.toString()));
    }

    // 401 Unauthorized: Authentication Failed / Missing X-User-Id
    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<Map<String, String>> handleAuthFailed(AuthenticationFailedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorPayload(ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorPayload(ex.getMessage()));
    }

    // 404 Not Found: Entity missing
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorPayload(ex.getMessage()));
    }

    // 500 Internal Server Error: Catch-all fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorPayload("Internal Server Error: " + ex.getMessage()));
    }
}
