package uk.ac.ed.acp.cw2.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the ILP REST service.
 * Handles common exceptions and returns appropriate HTTP responses.
 * droneDetails/{id} returns 404 for non-existent IDs.
 */
@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

    /**
     * Handle validation errors (e.g., @Valid annotation failures)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        logger.warn("Validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Handle malformed JSON in request body
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleJsonParseError(
            HttpMessageNotReadableException ex) {

        logger.warn("JSON parse error: {}", ex.getMessage());

        Map<String, String> error = new HashMap<>();
        error.put("error", "Malformed JSON request");
        error.put("message", "Unable to parse request body");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle type mismatch in path variables (e.g., String instead of Integer)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        logger.warn("Type mismatch: {} - expected {}",
                ex.getValue(), ex.getRequiredType());

        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid parameter type");
        error.put("parameter", ex.getName());
        assert ex.getRequiredType() != null;
        error.put("expectedType", ex.getRequiredType().getSimpleName());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Handles date/time parsing errors
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<Map<String, String>> handleDateTimeParseError(
            DateTimeParseException ex) {

        logger.warn("Date/time parse error: {}", ex.getMessage());

        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid date or time format");
        error.put("message", "Expected format: date='YYYY-MM-DD', time='HH:mm'");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Handles IllegalArgumentException (e.g., from DroneNavigation)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(
            IllegalArgumentException ex) {

        logger.warn("Illegal argument: {}", ex.getMessage());

        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid argument");
        error.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Handles all other unexpected exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericError(Exception ex) {

        logger.error("Unexpected error", ex);

        Map<String, String> error = new HashMap<>();
        error.put("error", "Internal server error");
        error.put("message", "An unexpected error occurred");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
