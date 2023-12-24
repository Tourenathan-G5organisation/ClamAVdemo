package com.hombee.clamavdemo.exception;


import com.hombee.clamavdemo.common.ErrorCodes;
import com.hombee.clamavdemo.dto.response.ApiError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;



@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    Logger LOG = LogManager.getLogger();
    @ExceptionHandler({ClamAvException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleClamAVException(ClamAvException exception, WebRequest request) {
        ApiError apiError =
                new ApiError(HttpStatus.BAD_REQUEST, exception.getLocalizedMessage(), ErrorCodes.FAIL);

        return new ResponseEntity<>(apiError, apiError.getStatus());
    }



    // Default Exception Handler
    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
        ApiError apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), "error occurred");
        apiError.setErc(ErrorCodes.FAIL);
        LOG.info("An error occurred: {}", ex.getMessage());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }
}
