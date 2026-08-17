package com.example.warehouse.central;

import com.example.warehouse.common.Measurement;

public final class AlarmEvaluator {

    private final ThresholdConfig config;

    public AlarmEvaluator(ThresholdConfig config) {
        this.config = config;
    }

    public boolean shouldRaiseAlarm(Measurement measurement) {
        double threshold = config.thresholdFor(measurement.sensorType());
        return measurement.value() > threshold;
    }

    public String alarmMessage(Measurement measurement) {
        double threshold = config.thresholdFor(measurement.sensorType());

        return String.format(
                "ALARM | warehouse=%s | sensor=%s | type=%s | value=%.1f | threshold=%.1f",
                measurement.warehouseId(),
                measurement.sensorId(),
                measurement.sensorType(),
                measurement.value(),
                threshold
        );
    }
}