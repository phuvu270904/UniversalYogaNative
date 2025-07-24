package com.example.universalyoganative;

public enum SyncStatus {
    EDITED("edited"),
    DELETED("deleted"),
    SYNC("sync");

    private final String value;

    SyncStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SyncStatus fromString(String text) {
        for (SyncStatus status : SyncStatus.values()) {
            if (status.value.equalsIgnoreCase(text)) {
                return status;
            }
        }
        return EDITED; // Default to EDITED if unknown
    }

    public static SyncStatus fromInt(int value) {
        switch (value) {
            case 0:
                return EDITED;
            case 1:
                return SYNC;
            case 2:
                return DELETED;
            default:
                return EDITED;
        }
    }

    public int toInt() {
        switch (this) {
            case EDITED:
                return 0;
            case SYNC:
                return 1;
            case DELETED:
                return 2;
            default:
                return 0;
        }
    }
} 