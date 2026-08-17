package com.example.warehouse.simulator;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public final class SensorSimulator {

    private static final int TEMPERATURE_PORT = 3344;
    private static final int HUMIDITY_PORT = 3355;

    private SensorSimulator() {
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 3) {
            System.out.println("Usage: SensorSimulator <temperature|humidity> <sensor-id> <value>");
            System.out.println("Example: SensorSimulator temperature t1 36");
            return;
        }

        String sensorType = args[0].toLowerCase();
        String sensorId = args[1];
        double value = Double.parseDouble(args[2]);

        int port;

        if ("temperature".equals(sensorType)) {
            port = TEMPERATURE_PORT;
        } else if ("humidity".equals(sensorType)) {
            port = HUMIDITY_PORT;
        } else {
            System.out.println("Invalid sensor type: " + sensorType);
            return;
        }

        String message = "sensor_id=" + sensorId + "; value=" + value;
        byte[] data = message.getBytes(StandardCharsets.UTF_8);

        InetAddress address = InetAddress.getLoopbackAddress();

        DatagramPacket packet = new DatagramPacket(
                data,
                data.length,
                address,
                port
        );

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.send(packet);
        }

        System.out.println(
                "Sent UDP message to port " + port + ": " + message
        );
    }
}