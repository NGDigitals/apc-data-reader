package com.ngdigitals.apc.data.reader.payload.response;

import lombok.Data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Data
@JsonDeserialize(as = ApiResponse.class)
public class ApiResponse<T> {

    private Boolean success;
    private String message;

    public ApiResponse() {
        super();
    }

    public ApiResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}