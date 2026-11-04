package com.handoverai.connector;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ConnectorFactory {

    private final Map<String, Connector> connectors;

    public ConnectorFactory(List<Connector> connectorList) {
        this.connectors = connectorList.stream()
                .collect(Collectors.toMap(c -> c.getServiceName().toUpperCase(), Function.identity()));
    }

    public Optional<Connector> getConnector(String serviceName) {
        if (serviceName == null) return Optional.empty();
        return Optional.ofNullable(connectors.get(serviceName.toUpperCase()));
    }
}
