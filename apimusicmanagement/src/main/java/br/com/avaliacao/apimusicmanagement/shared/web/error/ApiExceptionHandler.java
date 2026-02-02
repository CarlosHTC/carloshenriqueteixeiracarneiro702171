package br.com.avaliacao.apimusicmanagement.shared.web.error;

import br.com.avaliacao.apimusicmanagement.domain.exception.ConflictException;
import br.com.avaliacao.apimusicmanagement.domain.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.HashMap;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        pd.setTitle("Validation error");
        pd.setInstance(URI.create(request.getRequestURI()));
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation error");
        pd.setInstance(URI.create(request.getRequestURI()));

        var errors = new HashMap<String, String>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        pd.setProperty("errors", errors);
        return pd;
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException exception, HttpServletRequest request) {
        var pb = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        pb.setInstance(URI.create(request.getRequestURI()));
        return pb;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException exception, HttpServletRequest request) {
        var pb = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pb.setTitle("Data integrity violation");
        pb.setDetail("Conflito de integridade de dados (provavel violação de constraint).");
        pb.setInstance(URI.create(request.getRequestURI()));
        return pb;
    }
}
