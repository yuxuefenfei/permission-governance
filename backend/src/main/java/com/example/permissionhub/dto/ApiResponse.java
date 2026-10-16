package com.example.permissionhub.dto;

/** 统一响应体；业务成功与失败都通过明确的 HTTP 状态码区分。 */
public record ApiResponse<T>(int code, String message, T data) {
    public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(0, "操作成功", data); }
    public static ApiResponse<Void> ok() { return new ApiResponse<>(0, "操作成功", null); }
    public static ApiResponse<Void> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
