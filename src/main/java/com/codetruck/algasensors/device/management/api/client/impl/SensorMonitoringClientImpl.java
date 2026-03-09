package com.codetruck.algasensors.device.management.api.client.impl;

import com.codetruck.algasensors.device.management.api.client.RestClientFactory;
import com.codetruck.algasensors.device.management.api.client.SensorMonitoringClient;
import com.codetruck.algasensors.device.management.api.model.SensorMonitoringOutput;
import io.hypersistence.tsid.TSID;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SensorMonitoringClientImpl implements SensorMonitoringClient {

    private final RestClient restClient;

    public SensorMonitoringClientImpl(RestClientFactory factory) {
        this.restClient = factory.temperatureMonitoringRestClient();
    }

    @Override
    public void enableMonitoring(TSID sensorId) {
        restClient.put()
                .uri("/api/sensors/{sensorID}/monitoring/enable", sensorId)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void disableMonitoring(TSID sensorId) {
        restClient.delete()
                .uri("/api/sensors/{sensorID}/monitoring/disable", sensorId)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public SensorMonitoringOutput getDetail(TSID sensorId) {
        return restClient.get()
                .uri("/api/sensors/{sensorID}/monitoring", sensorId)
                .retrieve()
                .body(SensorMonitoringOutput.class);
    }
}
