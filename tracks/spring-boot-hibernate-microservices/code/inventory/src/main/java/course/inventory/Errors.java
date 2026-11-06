package course.inventory;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
class Errors {
  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<ProblemDetail> bad(IllegalArgumentException e) {
    return ResponseEntity.badRequest()
        .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage()));
  }

  @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
  ResponseEntity<ProblemDetail> constraint(Exception e) {
    return ResponseEntity.status(409)
        .body(
            ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, "request conflicts with stored data"));
  }

  @ExceptionHandler(Conflict.class)
  ResponseEntity<ProblemDetail> conflict(Conflict e) {
    return ResponseEntity.status(409)
        .body(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage()));
  }

  @ExceptionHandler(Missing.class)
  ResponseEntity<ProblemDetail> missing(Missing e) {
    return ResponseEntity.status(404)
        .body(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage()));
  }
}

class Conflict extends RuntimeException {
  Conflict(String m) {
    super(m);
  }
}

class Missing extends RuntimeException {
  Missing(String m) {
    super(m);
  }
}
