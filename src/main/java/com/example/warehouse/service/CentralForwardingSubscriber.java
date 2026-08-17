package com.example.warehouse.service;

import com.example.warehouse.common.Measurement;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.Flow;

public final class CentralForwardingSubscriber implements Flow.Subscriber<Measurement> {

    private final HttpClient httpClient;
    private final URI centralUri;
    private Flow.Subscription subscription;

    public CentralForwardingSubscriber(URI centralUri) {
        this.centralUri = centralUri;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        this.subscription.request(1);
    }

    @Override
    public void onNext(Measurement measurement) {
        String body = measurement.toWireFormat();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(centralUri)
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "text/plain; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .whenComplete((response, error) -> {

                    if (error != null) {
                        System.err.println(
                                "Error sending measurement to Central Service: "
                                        + error.getMessage()
                        );
                    } else {
                        System.out.println(
                                "Measurement sent: "
                                        + measurement.sensorType()
                                        + " | sensor=" + measurement.sensorId()
                                        + " | value=" + measurement.value()
                                        + " | status=" + response.statusCode()
                        );
                    }

                    subscription.request(1);
                });
    }

    @Override
    public void onError(Throwable throwable) {
        System.err.println(
                "Error in measurement flow: " + throwable.getMessage()
        );
    }

    @Override
    public void onComplete() {
        System.out.println("Measurement flow completed.");
    }
}