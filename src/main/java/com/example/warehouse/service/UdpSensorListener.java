package com.example.warehouse.service;

import com.example.warehouse.common.Measurement;
import com.example.warehouse.common.MeasurementParser;
import com.example.warehouse.common.SensorType;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public final class UdpSensorListener implements Runnable, AutoCloseable {

    private final String warehouseId;
    private final SensorType sensorType;
    private final int port;
    private final Consumer<Measurement> downstream;

    private volatile boolean running = true;
    private DatagramSocket socket;

    public UdpSensorListener(
            String warehouseId,
            SensorType sensorType,
            int port,
            Consumer<Measurement> downstream) {

        this.warehouseId = warehouseId;
        this.sensorType = sensorType;
        this.port = port;
        this.downstream = downstream;
    }

    @Override
    public void run() {

        try (DatagramSocket datagramSocket = new DatagramSocket(port)) {

            this.socket = datagramSocket;

            System.out.println(
                    "Listening for " + sensorType
                            + " sensor messages on UDP " + port
            );

            byte[] buffer = new byte[2048];

            while (running) {

                DatagramPacket packet =
                        new DatagramPacket(buffer, buffer.length);

                datagramSocket.receive(packet);

                String payload = new String(
                        packet.getData(),
                        packet.getOffset(),
                        packet.getLength(),
                        StandardCharsets.UTF_8
                );

                processMessage(payload);
            }

        } catch (SocketException ex) {

            if (running) {
                throw new RuntimeException(
                        "Could not open UDP port " + port,
                        ex
                );
            }

        } catch (IOException ex) {

            throw new RuntimeException(
                    "Error receiving UDP message on port " + port,
                    ex
            );
        }
    }

    private void processMessage(String payload) {

        try {
            Measurement measurement = MeasurementParser.fromSensorUdp(
                    warehouseId,
                    sensorType,
                    payload
            );

            downstream.accept(measurement);

        } catch (IllegalArgumentException ex) {

            System.err.println(
                    "Invalid UDP message on port " + port
                            + ": " + ex.getMessage()
                            + " | payload=" + payload
            );
        }
    }

    @Override
    public void close() {
        running = false;

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}