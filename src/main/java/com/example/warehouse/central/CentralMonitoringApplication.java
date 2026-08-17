package com.example.warehouse.central;

import com.example.warehouse.common.Measurement;
import com.example.warehouse.common.MeasurementParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public final class CentralMonitoringApplication {

    private static final int DEFAULT_PORT = 8080;
    private static final double TEMPERATURE_THRESHOLD = 35.0;
    private static final double HUMIDITY_THRESHOLD = 50.0;

    private CentralMonitoringApplication() {
    }

    public static void main(String[] args) throws IOException {
        int port = DEFAULT_PORT;

        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        ThresholdConfig thresholdConfig =
                new ThresholdConfig(TEMPERATURE_THRESHOLD, HUMIDITY_THRESHOLD);

        AlarmEvaluator alarmEvaluator = new AlarmEvaluator(thresholdConfig);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext(
                "/measurements",
                exchange -> handleMeasurement(exchange, alarmEvaluator)
        );

        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();

        System.out.println(
                "Central Monitoring Service started at http://localhost:"
                        + port + "/measurements"
        );

        System.out.println(
                "Temperature threshold: " + TEMPERATURE_THRESHOLD + " C"
                        + " | Humidity threshold: " + HUMIDITY_THRESHOLD + "%"
        );
    }

    private static void handleMeasurement(
            HttpExchange exchange,
            AlarmEvaluator evaluator
    ) throws IOException {

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            respond(exchange, 405, "Method Not Allowed");
            return;
        }

        String body = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        try {
            Measurement measurement = MeasurementParser.fromWireFormat(body);

            System.out.println(
                    "MEASUREMENT | warehouse=" + measurement.warehouseId()
                            + " | sensor=" + measurement.sensorId()
                            + " | type=" + measurement.sensorType()
                            + " | value=" + measurement.value()
            );

            if (evaluator.shouldRaiseAlarm(measurement)) {
                System.out.println(evaluator.alarmMessage(measurement));
            }

            respond(exchange, 202, "accepted");

        } catch (IllegalArgumentException ex) {
            System.err.println("Rejected measurement: " + ex.getMessage());
            respond(exchange, 400, "invalid measurement: " + ex.getMessage());
        }
    }

    private static void respond(
            HttpExchange exchange,
            int status,
            String text
    ) throws IOException {

        byte[] response = text.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(status, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}