package com.kkutudio.admin.auth;

public record AdminUserResponse(Long id, String email, String name) {

    public static AdminUserResponse from(AdminUser adminUser) {
        return new AdminUserResponse(adminUser.getId(), adminUser.getEmail(), adminUser.getName());
    }
}
