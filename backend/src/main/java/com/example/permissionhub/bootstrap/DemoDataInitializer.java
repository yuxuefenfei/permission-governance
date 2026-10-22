package com.example.permissionhub.bootstrap;

import com.example.permissionhub.repository.DemoDataRepository;
import com.example.permissionhub.service.PermissionRegistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/** 本地演示初始化；仅填充空表，生产环境应移除此组件并改用正式交付流程。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DemoDataInitializer {

    private final PermissionRegistryService registry;
    private final DemoDataRepository repository;
    private final PasswordEncoder encoder;
    private final TransactionTemplate transactions;

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        registry.synchronize(false); // 启动时 upsert，不标废弃，避免滚动部署误报。
        transactions.executeWithoutResult(tx -> {
            repository.seedRolesAndMenus();
            if (!repository.hasUsers()) {
                repository.seedUsers(
                        encoder.encode("Admin@123456"),
                        encoder.encode("Operator@123456")
                );
            }
        });
        log.info("演示数据已就绪；默认账号仅供本地学习使用，部署前务必修改或删除");
    }
}
