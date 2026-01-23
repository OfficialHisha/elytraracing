package com.hishacorp.elytraracing;

public enum Permissions {
    SETUP("elytraracing.admin.setup"),
    TOOL("elytraracing.admin.tool"),
    CREATE("elytraracing.admin.create"),
    DELETE("elytraracing.admin.delete"),
    TIME("elytraracing.admin.time"),
    SETSPAWN("elytraracing.admin.setspawn"),
    STATS("elytraracing.admin.stats"),
    START("elytraracing.admin.start"),
    END("elytraracing.admin.end"),
    RINGS("elytraracing.admin.rings");

    private final String permission;

    Permissions(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
