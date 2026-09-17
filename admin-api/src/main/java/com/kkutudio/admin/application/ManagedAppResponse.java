package com.kkutudio.admin.application;

public record ManagedAppResponse(Long id, String appCode, String name, String description, String iconUrl,
        AppStatus status) {

    public static ManagedAppResponse from(ManagedApp app) {
        return new ManagedAppResponse(app.getId(), app.getAppCode(), app.getName(), app.getDescription(),
                app.getIconUrl(), app.getStatus());
    }
}
