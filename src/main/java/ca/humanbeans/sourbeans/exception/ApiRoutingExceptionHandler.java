package ca.humanbeans.sourbeans.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Routing can fail before a controller exists, so package selectors cannot cover it. */
@RestControllerAdvice
public class ApiRoutingExceptionHandler {
    private final ApiExceptionHandler handler;

    public ApiRoutingExceptionHandler(ApiExceptionHandler handler) {
        this.handler = handler;
    }

    @ExceptionHandler({HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class, HttpMediaTypeNotAcceptableException.class,
            NoHandlerFoundException.class, NoResourceFoundException.class})
    ResponseEntity<Object> routing(Exception ex, ServletWebRequest request) throws Exception {
        String path = request.getRequest().getRequestURI()
                .substring(request.getRequest().getContextPath().length());
        if (!path.equals("/api") && !path.startsWith("/api/")) {
            throw ex; // Let normal MVC resolvers handle browser/static-resource requests.
        }
        return handler.handleException(ex, request);
    }
}
