package com.codetruck.algasensors.device.management.api.controller;

import com.codetruck.algasensors.device.management.api.client.SensorMonitoringClient;
import com.codetruck.algasensors.device.management.api.model.SensorDetailOutput;
import com.codetruck.algasensors.device.management.api.model.SensorInput;
import com.codetruck.algasensors.device.management.api.model.SensorMonitoringOutput;
import com.codetruck.algasensors.device.management.api.model.SensorOutput;
import com.codetruck.algasensors.device.management.common.IdGenerator;
import com.codetruck.algasensors.device.management.domain.model.Sensor;
import com.codetruck.algasensors.device.management.domain.model.SensorId;
import com.codetruck.algasensors.device.management.domain.repository.SensorRepository;
import io.hypersistence.tsid.TSID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
public class SensorController {

    private final SensorRepository sensorRepository;
    private final SensorMonitoringClient sensorMonitoringClient;

    @GetMapping
    public Page<SensorOutput> search(@PageableDefault Pageable pageable) {
        Page<Sensor> sensors = sensorRepository.findAll(pageable);
        return sensors.map(this::convertToModel);
    }

    @GetMapping("{sensorId}")
    public SensorOutput getOne(@PathVariable TSID sensorId) {
        Sensor sensor = sensorRepository.findById(new SensorId(sensorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return convertToModel(sensor);
    }

    @GetMapping("{sensorId}/detail")
    public SensorDetailOutput getOneWithDetail(@PathVariable TSID sensorId) {
        Sensor sensor = sensorRepository.findById(new SensorId(sensorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        SensorMonitoringOutput monitoringOutput = sensorMonitoringClient.getDetail(sensorId);

        SensorOutput sensorOutput = convertToModel(sensor);

        return SensorDetailOutput.builder()
                .monitoring(monitoringOutput)
                .sensor(sensorOutput)
                .build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SensorOutput create(@RequestBody SensorInput input) {
        Sensor sensor = Sensor.builder()
                .id(new SensorId(IdGenerator.generateTSID()))
                .name(input.getName())
                .ip(input.getIp())
                .location(input.getLocation())
                .protocol(input.getProtocol())
                .model(input.getModel())
                .enabled(Boolean.FALSE)
                .build();
        sensor = sensorRepository.saveAndFlush(sensor);
        return convertToModel(sensor);
    }

    @PutMapping("{sensorId}")
    @ResponseStatus(HttpStatus.OK)
    public SensorOutput update(@PathVariable TSID sensorId, @RequestBody SensorInput input) {
        final Sensor sensorFound = sensorRepository.findById(new SensorId(sensorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Sensor sensor = Sensor.builder()
                .id(sensorFound.getId())
                .name(input.getName())
                .ip(input.getIp())
                .location(input.getLocation())
                .protocol(input.getProtocol())
                .model(input.getModel())
                .enabled(Boolean.FALSE)
                .build();
        sensor = sensorRepository.saveAndFlush(sensor);
        return convertToModel(sensor);
    }

    @DeleteMapping("{sensorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable TSID sensorId) {
        final Sensor sensorFound = sensorRepository.findById(new SensorId(sensorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        sensorRepository.deleteById(sensorFound.getId());
    }

    @PutMapping("{sensorId}/enable")
    @ResponseStatus(HttpStatus.OK)
    public void enableSensor(@PathVariable TSID sensorId) {
        final Sensor sensor = sensorRepository.findById(new SensorId(sensorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        sensor.setEnabled(Boolean.TRUE);

        sensorRepository.save(sensor);

        sensorMonitoringClient.enableMonitoring(sensorId);
    }

    @DeleteMapping("{sensorId}/disable")
    @ResponseStatus(HttpStatus.OK)
    public void disableSensor(@PathVariable TSID sensorId) {
        final Sensor sensor = sensorRepository.findById(new SensorId(sensorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        sensor.setEnabled(Boolean.FALSE);

        sensorRepository.save(sensor);

        sensorMonitoringClient.disableMonitoring(sensorId);

    }

    private SensorOutput convertToModel(Sensor sensor) {
        return SensorOutput.builder()
                .id(sensor.getId().getValue())
                .name(sensor.getName())
                .ip(sensor.getIp())
                .location(sensor.getLocation())
                .protocol(sensor.getProtocol())
                .model(sensor.getModel())
                .enabled(sensor.getEnabled())
                .build();
    }
}
