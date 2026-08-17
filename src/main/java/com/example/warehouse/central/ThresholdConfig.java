package com.example.warehouse.central;

import com.example.warehouse.common.SensorType;

import java.util.EnumMap;
import java.util.Map;

public final class ThresholdConfig {
    private final Map<SensorType, Double> thresholds = new EnumMap<>(SensorType.class);

    public ThresholdConfig(double temperatureThreshold, double humidityThreshold) {
        thresholds.put(SensorType.TEMPERATURE, temperatureThreshold);
        thresholds.put(SensorType.HUMIDITY, humidityThreshold);
    }

    public double thresholdFor(SensorType type) {
        return thresholds.get(type);
    }
}
