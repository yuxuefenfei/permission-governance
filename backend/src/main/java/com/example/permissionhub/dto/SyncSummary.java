package com.example.permissionhub.dto;

/** 一次权限同步的统计结果。 */
public record SyncSummary(int discovered, int endpoints, int deprecated) { }
