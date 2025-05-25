package com.mathotech.autopartshub.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        
        List<ApiSubError> subErrors = new ArrayList<>();
        
        // Extract field errors
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            ApiValidationError error = new ApiValidationError(
                    fieldError.getObjectName(),
                    fieldError.getField(),
                    fieldError.getRejectedValue(),
                    fieldError.getDefaultMessage()
            );
            subErrors.add(error);
        });
        
        // Extract global errors
        ex.getBindingResult().getGlobalErrors().forEach(globalError -> {
            ApiValidationError error = new ApiValidationError(
                    globalError.getObjectName(),
                    globalError.getDefaultMessage()
            );
            subErrors.add(error);
        });
        
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST);
        apiError.setMessage("Validation error");
        apiError.setDebugMessage("Invalid method arguments");
        apiError.setSubErrors(subErrors);
        
        return buildResponseEntity(apiError);
    }
    
    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex) {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND);
        apiError.setMessage("Resource not found");
        apiError.setDebugMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }
    
    @ExceptionHandler(EmailAlreadyExistsException.class)
    protected ResponseEntity<Object> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        ApiError apiError = new ApiError(HttpStatus.CONFLICT);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex) {
        ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED);
        apiError.setMessage("Invalid username or password");
        apiError.setDebugMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }
    
    @ExceptionHandler(AuthenticationException.class)
    protected ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex) {
        ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED);
        apiError.setMessage("Authentication failed");
        apiError.setDebugMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        ApiError apiError = new ApiError(HttpStatus.FORBIDDEN);
        apiError.setMessage("Access denied");
        apiError.setDebugMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        List<ApiSubError> subErrors = ex.getConstraintViolations().stream()
                .map(violation -> new ApiValidationError(
                        violation.getRootBeanClass().getSimpleName(),
                        violation.getPropertyPath().toString(),
                        violation.getInvalidValue(),
                        violation.getMessage()
                ))
                .collect(Collectors.toList());
        
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST);
        apiError.setMessage("Validation error");
        apiError.setDebugMessage(ex.getMessage());
        apiError.setSubErrors(subErrors);
        
        return buildResponseEntity(apiError);
    }
    
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleAllExceptions(Exception ex) {
        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR);
        apiError.setMessage("Unexpected error occurred");
        apiError.setDebugMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }
    
    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}
