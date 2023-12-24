package com.hombee.clamavdemo.dto.response;

import com.hombee.clamavdemo.common.ErrorCodes;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

@Data
public class ApiError extends ResponseDTO {
    private HttpStatus status;
    private List<String> errors;

    public ApiError(HttpStatus status, String message, int errorCode, List<String> errors) {
        super();
        this.status = status;
        setMessage(message);
        setErc(errorCode);
        this.errors = errors;
    }

    public ApiError(HttpStatus status, String message, int errorCode) {
        this(status, message, errorCode, new ArrayList<>());
    }

    public ApiError(HttpStatus status, String message) {
        this(status, message, ErrorCodes.FAIL, new ArrayList<>());
    }

    public ApiError(HttpStatus status, String message, List<String> errors) {
        this(status, message, ErrorCodes.FAIL, errors);
    }

    public ApiError(HttpStatus status, String message, String errors) {
        this(status, message, ErrorCodes.FAIL, List.of(errors));
    }
}
