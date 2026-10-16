package com.example.permissionhub.service;

import com.example.permissionhub.dto.DashboardOverview;
import com.example.permissionhub.repository.DashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 管理台概览查询。 */
@Service
@RequiredArgsConstructor
public class DashboardService {
    private final DashboardRepository repository;

    public DashboardOverview overview() {
        return repository.overview();
    }
}
