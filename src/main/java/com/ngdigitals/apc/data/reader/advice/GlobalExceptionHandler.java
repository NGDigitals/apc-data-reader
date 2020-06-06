package com.ngdigitals.apc.data.reader.advice;

import java.util.List;
import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.WebUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.ngdigitals.apc.data.reader.payload.response.ApiResponse;
import com.ngdigitals.apc.data.reader.exception.FileImportException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({FileImportException.class})
    public final ResponseEntity<?> handleException(Exception exception, WebRequest request) {
        HttpHeaders headers = new HttpHeaders();
        if (exception instanceof FileImportException) {
            HttpStatus status = HttpStatus.OK;
            FileImportException fileImportException = (FileImportException) exception;
            return handleFileImportException(fileImportException, headers, status, request);
        }else {
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return handleExceptionInternal(exception, null, headers, status, request);
        }
    }

    protected ResponseEntity<?> handleFileImportException(FileImportException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<String> messages = Collections.singletonList(ex.getMessage());
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);
        apiResponse.setMessage(messages.get(0));
        return handleExceptionInternal(ex, apiResponse, headers, status, request);
    }

    protected ResponseEntity<?> handleExceptionInternal(Exception ex, ApiResponse body,
                                                        HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
        }
//        return ResponseEntity.ok(body);
        return new ResponseEntity<>(body, headers, status);
    }
}