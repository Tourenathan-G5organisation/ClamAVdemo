package com.hombee.clamavdemo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class ResponseDTO<T> {
    private int erc;
    private String message;
    @Nullable
    private T data;
}
