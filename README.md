# Warehouse Monitoring System

Simple reactive system developed with **Java 21** for monitoring temperature and humidity sensors.

## Architecture

```text
Sensors → UDP → Warehouse Service → HTTP → Central Monitoring
```

* Temperature: UDP `3344` / threshold `35°C`
* Humidity: UDP `3355` / threshold `50%`
* Central Monitoring: `POST http://localhost:8080/measurements`

The Warehouse Service uses the Java Flow API to process and forward measurements. If a threshold is exceeded, the Central Monitoring Service prints an alarm to the console.

## Running

Requirements:

* Java 21
* Maven

Build:

```bash
mvn clean package
```

Start:

1. `CentralMonitoringApplication`
2. `WarehouseApplication`

Sensors can be simulated using Netcat:

```bash
echo "sensor_id=t1; value=36" | ncat -u 127.0.0.1 3344
```

```bash
echo "sensor_id=h1; value=51" | ncat -u 127.0.0.1 3355
```

No external framework was used. Maven is used for build and project management.
