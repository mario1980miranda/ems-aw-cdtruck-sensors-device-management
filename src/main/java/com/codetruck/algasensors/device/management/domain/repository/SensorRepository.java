package com.codetruck.algasensors.device.management.domain.repository;

import com.codetruck.algasensors.device.management.domain.model.Sensor;
import com.codetruck.algasensors.device.management.domain.model.SensorId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<Sensor, SensorId> {
}
