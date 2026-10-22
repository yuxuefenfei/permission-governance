package com.example.permissionhub.dto;

/**
 * 管理台概览指标。
 */
public record DashboardOverview(int permissionCount, int deprecatedCount, int roleCount,
                                int userCount, int menuCount) {
}
