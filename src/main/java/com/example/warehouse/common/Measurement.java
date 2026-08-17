package com.example.warehouse.common;

import java.time.Instant;
import java.util.Objects;

public record Measurement(
        String warehouseId,
        String sensorId,
        SensorType sensorType,
        double value,
        Instant capturedAt
) {
    public Measurement {
        Objects.requireNonNull(warehouseId, "warehouseId");
        Objects.requireNonNull(sensorId, "sensorId");
        Objects.requireNonNull(sensorType, "sensorType");
        Objects.requireNonNull(capturedAt, "capturedAt");
    }

    public String toWireFormat() {
        return "warehouse_id=" + warehouseId
                + ";sensor_id=" + sensorId
                + ";type=" + sensorType.name().toLowerCase()
                + ";value=" + value
                + ";captured_at=" + capturedAt;
    }
}
