package com.example.warehouse;

import com.example.warehouse.central.AlarmEvaluator;
import com.example.warehouse.central.ThresholdConfig;
import com.example.warehouse.common.Measurement;
import com.example.warehouse.common.MeasurementParser;
import com.example.warehouse.common.SensorType;

public final class SelfTest {

    private SelfTest() {
    }

    public static void main(String[] args) {
        testParser();
        testThresholds();

        System.out.println("All tests passed.");
    }

    private static void testParser() {
        Measurement measurement = MeasurementParser.fromSensorUdp(
                "warehouse-1",
                SensorType.TEMPERATURE,
                "sensor_id=t1; value=30"
        );

        require(
                "t1".equals(measurement.sensorId()),
                "Sensor id was not parsed correctly"
        );

        require(
                measurement.value() == 30.0,
                "Sensor value was not parsed correctly"
        );

        require(
                measurement.sensorType() == SensorType.TEMPERATURE,
                "Sensor type was not parsed correctly"
        );
    }

    private static void testThresholds() {
        ThresholdConfig config = new ThresholdConfig(35, 50);
        AlarmEvaluator evaluator = new AlarmEvaluator(config);

        Measurement normalTemperature = MeasurementParser.fromSensorUdp(
                "warehouse-1",
                SensorType.TEMPERATURE,
                "sensor_id=t1; value=35"
        );

        Measurement highTemperature = MeasurementParser.fromSensorUdp(
                "warehouse-1",
                SensorType.TEMPERATURE,
                "sensor_id=t1; value=36"
        );

        Measurement highHumidity = MeasurementParser.fromSensorUdp(
                "warehouse-1",
                SensorType.HUMIDITY,
                "sensor_id=h1; value=51"
        );

        require(
                !evaluator.shouldRaiseAlarm(normalTemperature),
                "Temperature 35C should not generate an alarm"
        );

        require(
                evaluator.shouldRaiseAlarm(highTemperature),
                "Temperature 36C should generate an alarm"
        );

        require(
                evaluator.shouldRaiseAlarm(highHumidity),
                "Humidity 51% should generate an alarm"
        );
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}