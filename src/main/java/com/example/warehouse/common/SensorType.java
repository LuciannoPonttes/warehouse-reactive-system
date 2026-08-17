package com.example.warehouse.common;

public enum SensorType {
    TEMPERATURE,
    HUMIDITY;

    public static SensorType from(String raw) {
        return SensorType.valueOf(raw.trim().toUpperCase());
    }
}
