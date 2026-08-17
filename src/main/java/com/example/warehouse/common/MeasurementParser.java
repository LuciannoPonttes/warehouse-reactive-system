package com.example.warehouse.common;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public final class MeasurementParser {

    private MeasurementParser() {
    }

    public static Measurement fromSensorUdp(String warehouseId, SensorType type, String payload) {
        Map<String, String> data = parsePairs(payload);

        String sensorId = required(data, "sensor_id");
        String rawValue = required(data, "value");
        double value = parseDouble(rawValue, "value");

        return new Measurement(
                warehouseId,
                sensorId,
                type,
                value,
                Instant.now()
        );
    }

    public static Measurement fromWireFormat(String payload) {
        Map<String, String> data = parsePairs(payload);

        String warehouseId = required(data, "warehouse_id");
        String sensorId = required(data, "sensor_id");
        SensorType type = SensorType.from(required(data, "type"));
        double value = parseDouble(required(data, "value"), "value");
        Instant capturedAt = Instant.parse(required(data, "captured_at"));

        return new Measurement(
                warehouseId,
                sensorId,
                type,
                value,
                capturedAt
        );
    }

    private static Map<String, String> parsePairs(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("Payload is empty");
        }

        Map<String, String> data = new HashMap<>();
        String[] pairs = payload.split(";");

        for (String pair : pairs) {
            String item = pair.trim();

            if (item.isEmpty()) {
                continue;
            }

            int separator = item.indexOf('=');

            if (separator <= 0 || separator == item.length() - 1) {
                throw new IllegalArgumentException("Invalid pair: " + item);
            }

            String key = item.substring(0, separator).trim();
            String value = item.substring(separator + 1).trim();

            data.put(key, value);
        }

        return data;
    }

    private static String required(Map<String, String> data, String key) {
        String value = data.get(key);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing field: " + key);
        }

        return value;
    }

    private static double parseDouble(String value, String field) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Invalid numeric field '" + field + "': " + value,
                    ex
            );
        }
    }
}