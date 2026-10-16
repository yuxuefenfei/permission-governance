-- MySQL 8 / utf8mb4 版本。Demo 默认 H2，不会自动执行本脚本。
-- 生产建议由 Flyway/Liquibase 维护 schema 版本，而非运行时执行 init SQL。
CREATE TABLE IF NOT EXISTS permission_definitions (
    code VARCHAR(160) NOT NULL PRIMARY KEY COMMENT '权限码，全系统唯一（单系统 Demo 模型）',
    app_code VARCHAR(40) NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    module_name VARCHAR(100) NOT NULL,
    kind VARCHAR(20) NOT NULL COMMENT 'PAGE ACTION ADMIN',
    description VARCHAR(500) NOT NULL DEFAULT '',
    source VARCHAR(20) NOT NULL DEFAULT 'CODE',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    first_seen_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_seen_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_perm_module(module_name, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS permission_endpoints (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    permission_code VARCHAR(160) NOT NULL,
    http_method VARCHAR(12) NOT NULL,
    url_path VARCHAR(255) NOT NULL,
    handler VARCHAR(255) NOT NULL,
    CONSTRAINT fk_endpoint_permission FOREIGN KEY(permission_code) REFERENCES permission_definitions(code),
    CONSTRAINT uq_permission_endpoint UNIQUE(permission_code, http_method, url_path),
    INDEX idx_perm_endpoints(permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_menus (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT NULL,
    display_name VARCHAR(100) NOT NULL,
    route_path VARCHAR(160) NOT NULL DEFAULT '',
    icon VARCHAR(60) NOT NULL DEFAULT '',
    menu_type VARCHAR(20) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_menu_parent FOREIGN KEY(parent_id) REFERENCES sys_menus(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS menu_permissions (
    menu_id BIGINT NOT NULL,
    permission_code VARCHAR(160) NOT NULL,
    PRIMARY KEY(menu_id,permission_code),
    FOREIGN KEY(menu_id) REFERENCES sys_menus(id) ON DELETE CASCADE,
    FOREIGN KEY(permission_code) REFERENCES permission_definitions(code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_roles (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(64) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL DEFAULT '',
    built_in TINYINT(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL,
    permission_code VARCHAR(160) NOT NULL,
    PRIMARY KEY(role_id,permission_code),
    FOREIGN KEY(role_id) REFERENCES sys_roles(id) ON DELETE CASCADE,
    FOREIGN KEY(permission_code) REFERENCES permission_definitions(code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    super_admin TINYINT(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY(user_id,role_id),
    FOREIGN KEY(user_id) REFERENCES sys_users(id) ON DELETE CASCADE,
    FOREIGN KEY(role_id) REFERENCES sys_roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    actor VARCHAR(64) NOT NULL,
    action VARCHAR(64) NOT NULL,
    resource_type VARCHAR(32) NOT NULL,
    resource_id VARCHAR(160) NOT NULL,
    detail VARCHAR(1000) NOT NULL DEFAULT '',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_created_at(created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
