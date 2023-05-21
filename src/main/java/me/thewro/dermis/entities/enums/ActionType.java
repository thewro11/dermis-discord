package me.thewro.dermis.entities.enums;

public enum ActionType {
    REQUEST_RECEIVED("📩  request received"),
    REQUEST_APPROVED("✔  request approved"),
    REQUEST_DENIED("❌  request denied");

    public final String NAME;

    ActionType(String name) {
        this.NAME = name;
    }
    
}
