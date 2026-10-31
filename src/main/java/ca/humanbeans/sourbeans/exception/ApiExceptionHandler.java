package ca.humanbeans.sourbeans.exception;

import java.net.URI;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Shared JSON error boundary for API controllers; HTML controllers retain MVC handling. */
@RestControllerAdvice(basePackages = "ca.humanbeans.sourbeans.controller.api")
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<Object> notFound(ResourceNotFoundException ex, WebRequest request) {
        return response(HttpStatus.NOT_FOUND, ex.getMessage(), new HttpHeaders(), request);
    }

    @ExceptionHandler(ResourceConflictException.class)
    ResponseEntity<Object> conflict(ResourceConflictException ex, WebRequest request) {
        return response(HttpStatus.CONFLICT, ex.getMessage(), new HttpHeaders(), request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Object> unexpected(Exception ex, WebRequest request) {
        // Do not log arbitrary exception messages or request values that may contain secrets.
        log.error("Unexpected API failure ({})", ex.getClass().getName());
        return response(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred.", new HttpHeaders(), request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        // Discard framework exception details, which may contain rejected values or Java types.
        return response(status, detail(status), headers, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = problem(status, "Request validation failed.", request);
        problem.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldViolation(error.getField(),
                        error.getDefaultMessage() == null ? "Invalid value." : error.getDefaultMessage()))
                .distinct()
                .sorted(Comparator.comparing(FieldViolation::field).thenComparing(FieldViolation::message))
                .toList());
        return response(problem, headers, status);
    }

    private ResponseEntity<Object> response(HttpStatusCode status, String detail,
            HttpHeaders headers, WebRequest request) {
        return response(problem(status, detail, request), headers, status);
    }

    private ResponseEntity<Object> response(ProblemDetail body, HttpHeaders headers, HttpStatusCode status) {
        HttpHeaders result = new HttpHeaders();
        result.putAll(headers); // Preserve protocol headers such as Allow and Accept.
        result.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        return new ResponseEntity<>(body, result, status);
    }

    private ProblemDetail problem(HttpStatusCode status, String detail, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("about:blank"));
        problem.setInstance(URI.create(((ServletWebRequest) request).getRequest().getRequestURI()));
        return problem;
    }

    private String detail(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "The request is invalid or malformed.";
            case 404 -> "The requested resource was not found.";
            case 405 -> "The HTTP method is not supported for this resource.";
            case 406 -> "The requested response format is not supported.";
            case 415 -> "The request content type is not supported.";
            default -> status.is5xxServerError()
                    ? "An unexpected server error occurred." : "The request could not be completed.";
        };
    }

    public record FieldViolation(String field, String message) { }
}
