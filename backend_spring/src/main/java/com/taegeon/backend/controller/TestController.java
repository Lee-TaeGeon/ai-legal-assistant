package com.taegeon.backend.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taegeon.backend.dto.ApiResponse;
import com.taegeon.backend.dto.TestRequest;
import com.taegeon.backend.exception.CustomException;
import com.taegeon.backend.exception.ErrorCode;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/test")
    public ApiResponse<Map<String, String>> test() {
        return ApiResponse.success(
                "AI 법률도우미 서버 실행 성공",
                Map.of("status", "UP")
        );
    }

    @GetMapping("/test/error")
    public ApiResponse<Void> error() {
        throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @PostMapping("/test/validation")
    public ApiResponse<Map<String, String>> validation(
            @Valid @RequestBody TestRequest request
    ) {
        return ApiResponse.success(
                "입력값 검증 성공",
                Map.of("name", request.name())
        );
    }
}