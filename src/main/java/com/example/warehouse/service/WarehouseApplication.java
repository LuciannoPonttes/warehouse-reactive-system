package com.example.warehouse.service;

import com.example.warehouse.common.Measurement;
import com.example.warehouse.common.SensorType;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SubmissionPublisher;

public final class WarehouseApplication {

    private static final int TEMPERATURE_UDP_PORT = 3344;
    private static final int HUMIDITY_UDP_PORT = 3355;

    private WarehouseApplication() {
    }

    public static void main(String[] args) throws Exception {

        String warehouseId = "warehouse-1";
        String centralUrl = "http://localhost:8080/measurements";

        if (args.length > 0) {
            warehouseId = args[0];
        }

        if (args.length > 1) {
            centralUrl = args[1];
        }

        URI centralUri = URI.create(centralUrl);

        ExecutorService executor =
                Executors.newVirtualThreadPerTaskExecutor();

        SubmissionPublisher<Measurement> publisher =
                new SubmissionPublisher<>(executor, 256);

        CentralForwardingSubscriber subscriber =
                new CentralForwardingSubscriber(centralUri);

        publisher.subscribe(subscriber);

        UdpSensorListener temperatureListener =
                new UdpSensorListener(
                        warehouseId,
                        SensorType.TEMPERATURE,
                        TEMPERATURE_UDP_PORT,
                        publisher::submit
                );

        UdpSensorListener humidityListener =
                new UdpSensorListener(
                        warehouseId,
                        SensorType.HUMIDITY,
                        HUMIDITY_UDP_PORT,
                        publisher::submit
                );

        Thread temperatureThread =
                Thread.startVirtualThread(temperatureListener);

        Thread humidityThread =
                Thread.startVirtualThread(humidityListener);

        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    temperatureListener.close();
                    humidityListener.close();
                    publisher.close();
                    executor.shutdown();
                })
        );

        System.out.println(
                "Warehouse Service '" + warehouseId
                        + "' started. Central=" + centralUri
        );

        temperatureThread.join();
        humidityThread.join();
    }
}