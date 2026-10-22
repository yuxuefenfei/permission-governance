package com.example.permissionhub.repository;

import com.example.permissionhub.dto.AuditEntry;
import com.example.permissionhub.entity.AuditLog;
import com.example.permissionhub.mapper.AuditLogMapper;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.permissionhub.entity.table.AuditLogTableDef.AUDIT_LOG;

/**
 * 审计持久化；保留最近 200 条查询及稳定排序。
 */
@Repository
@RequiredArgsConstructor
public class AuditRepository {

    private final AuditLogMapper mapper;

    public void insert(String actor, String action, String resourceType, String resourceId, String detail) {
        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setAction(action);
        log.setResourceType(resourceType);
        log.setResourceId(resourceId);
        log.setDetail(detail);
        mapper.insertSelective(log);
    }

    public List<AuditEntry> recent() {
        return mapper.selectListByQuery(QueryWrapper.create()
                        .orderBy(AUDIT_LOG.CREATED_AT.desc(), AUDIT_LOG.ID.desc()).limit(200)).stream()
                .map(log -> new AuditEntry(log.getId(), log.getActor(), log.getAction(), log.getResourceType(),
                        log.getResourceId(), log.getDetail(), log.getCreatedAt())).toList();
    }
}
