package com.qizhifu.jiaoxuepeiyu.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTests {

    @Test
    void usesBusinessExceptionCodeAsHttpStatusWhenItIsAValidErrorStatus() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ApiResponse<Void>> response =
                handler.handleBusinessException(new BusinessException(401, "Missing admin identity"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().getCode());
        assertEquals("Missing admin identity", response.getBody().getMessage());
    }

    @Test
    void fallsBackToBadRequestForInvalidBusinessExceptionStatusCode() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ApiResponse<Void>> response =
                handler.handleBusinessException(new BusinessException(0, "Invalid business state"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(0, response.getBody().getCode());
    }
}
