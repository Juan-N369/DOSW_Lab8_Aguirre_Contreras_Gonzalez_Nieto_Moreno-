package edu.eci.dosw.oficioya.exception;

import edu.eci.dosw.oficioya.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(CorreoDuplicadoException.class)
    ResponseEntity<ApiError> duplicado(CorreoDuplicadoException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiError.of(409, "Conflict", "El correo ya está registrado", req.getRequestURI()));
    }

    @ExceptionHandler(RolNoPermitidoException.class)
    ResponseEntity<ApiError> rolNoPermitido(RolNoPermitidoException ex, HttpServletRequest req) {
        return ResponseEntity.badRequest().body(
                ApiError.of(400, "Bad Request", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> datosInvalidos(MethodArgumentNotValidException ex, HttpServletRequest req) {
        return ResponseEntity.badRequest().body(
                ApiError.of(400, "Bad Request", "Datos inválidos", req.getRequestURI()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> cuerpoIlegible(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return ResponseEntity.badRequest().body(
                ApiError.of(400, "Bad Request", "Cuerpo de la petición inválido", req.getRequestURI()));
    }
}
