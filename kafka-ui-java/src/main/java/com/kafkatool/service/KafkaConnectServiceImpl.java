package com.kafkatool.service;

import com.kafkatool.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Implementation of Kafka Connect operations
 */
public class KafkaConnectServiceImpl implements KafkaConnectService {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaConnectServiceImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    
    @Override
    public CompletableFuture<Boolean> testConnectionAsync(String connectUrl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpGet request = new HttpGet(connectUrl + "/");
                try (var response = httpClient.execute(request)) {
                    return response.getCode() == 200;
                }
            } catch (Exception e) {
                logger.error("Failed to test connection to Kafka Connect", e);
                return false;
            }
        });
    }
    
    @Override
    public CompletableFuture<Map<String, Object>> getClusterInfoAsync(String connectUrl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpGet request = new HttpGet(connectUrl + "/");
                try (var response = httpClient.execute(request)) {
                    if (response.getCode() == 200) {
                        String responseBody = EntityUtils.toString(response.getEntity());
                        return objectMapper.readValue(responseBody, Map.class);
                    } else {
                        throw new RuntimeException("Failed to get cluster info: HTTP " + response.getCode());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to get cluster info", e);
                throw new CompletionException(e);
            }
        });
    }
    
    @Override
    public CompletableFuture<List<ConnectorInfo>> getConnectorsAsync(String connectUrl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpGet request = new HttpGet(connectUrl + "/connectors");
                try (var response = httpClient.execute(request)) {
                    if (response.getCode() == 200) {
                        String responseBody = EntityUtils.toString(response.getEntity());
                        String[] connectorNames = objectMapper.readValue(responseBody, String[].class);
                        
                        List<ConnectorInfo> connectors = new ArrayList<>();
                        for (String name : connectorNames) {
                            try {
                                ConnectorInfo info = getConnectorAsync(connectUrl, name).get();
                                connectors.add(info);
                            } catch (Exception e) {
                                logger.warn("Failed to get details for connector: {}", name, e);
                                // Add a basic connector info even if details fail
                                ConnectorInfo basicInfo = new ConnectorInfo();
                                basicInfo.setName(name);
                                basicInfo.setStatus("UNKNOWN");
                                basicInfo.setType("UNKNOWN");
                                basicInfo.setClassName("UNKNOWN");
                                connectors.add(basicInfo);
                            }
                        }
                        return connectors;
                    } else {
                        throw new RuntimeException("Failed to get connectors: HTTP " + response.getCode());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to get connectors", e);
                throw new CompletionException(e);
            }
        });
    }
    
    @Override
    public CompletableFuture<ConnectorInfo> getConnectorAsync(String connectUrl, String connectorName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpGet request = new HttpGet(connectUrl + "/connectors/" + connectorName);
                try (var response = httpClient.execute(request)) {
                    if (response.getCode() == 200) {
                        String responseBody = EntityUtils.toString(response.getEntity());
                        JsonNode jsonNode = objectMapper.readTree(responseBody);
                        
                        ConnectorInfo info = new ConnectorInfo();
                        info.setName(jsonNode.get("name").asText());
                        
                        // Get status info
                        HttpGet statusRequest = new HttpGet(connectUrl + "/connectors/" + connectorName + "/status");
                        try (var statusResponse = httpClient.execute(statusRequest)) {
                            if (statusResponse.getCode() == 200) {
                                String statusBody = EntityUtils.toString(statusResponse.getEntity());
                                JsonNode statusNode = objectMapper.readTree(statusBody);
                                
                                info.setStatus(statusNode.get("connector").get("state").asText());
                                info.setType(statusNode.get("type").asText("UNKNOWN"));
                                
                                if (statusNode.has("tasks")) {
                                    info.setTasksRunning(statusNode.get("tasks").size());
                                }
                            }
                        }
                        
                        // Get config info
                        if (jsonNode.has("config")) {
                            JsonNode config = jsonNode.get("config");
                            if (config.has("connector.class")) {
                                info.setClassName(config.get("connector.class").asText());
                            }
                        }
                        
                        return info;
                    } else {
                        throw new RuntimeException("Failed to get connector: HTTP " + response.getCode());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to get connector: {}", connectorName, e);
                throw new CompletionException(e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Map<String, String>> getConnectorConfigAsync(String connectUrl, String connectorName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpGet request = new HttpGet(connectUrl + "/connectors/" + connectorName + "/config");
                try (var response = httpClient.execute(request)) {
                    if (response.getCode() == 200) {
                        String responseBody = EntityUtils.toString(response.getEntity());
                        return objectMapper.readValue(responseBody, Map.class);
                    } else {
                        throw new RuntimeException("Failed to get connector config: HTTP " + response.getCode());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to get connector config: {}", connectorName, e);
                throw new CompletionException(e);
            }
        });
    }
    
    @Override
    public CompletableFuture<ConnectorInfo> createOrUpdateConnectorAsync(String connectUrl, String connectorName, 
                                                                       Map<String, String> config) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpPut request = new HttpPut(connectUrl + "/connectors/" + connectorName + "/config");
                request.setHeader("Content-Type", "application/json");
                
                String configJson = objectMapper.writeValueAsString(config);
                request.setEntity(new StringEntity(configJson));
                
                try (var response = httpClient.execute(request)) {
                    if (response.getCode() == 200 || response.getCode() == 201) {
                        // Return the updated connector info
                        return getConnectorAsync(connectUrl, connectorName).get();
                    } else {
                        String responseBody = EntityUtils.toString(response.getEntity());
                        throw new RuntimeException("Failed to create/update connector: HTTP " + response.getCode() + " - " + responseBody);
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to create/update connector: {}", connectorName, e);
                throw new CompletionException(e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> deleteConnectorAsync(String connectUrl, String connectorName) {
        return CompletableFuture.runAsync(() -> {
            try {
                HttpDelete request = new HttpDelete(connectUrl + "/connectors/" + connectorName);
                try (var response = httpClient.execute(request)) {
                    if (response.getCode() != 204) {
                        throw new RuntimeException("Failed to delete connector: HTTP " + response.getCode());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to delete connector: {}", connectorName, e);
                throw new CompletionException(e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> pauseConnectorAsync(String connectUrl, String connectorName) {
        return CompletableFuture.runAsync(() -> {
            try {
                HttpPut request = new HttpPut(connectUrl + "/connectors/" + connectorName + "/pause");
                try (var response = httpClient.execute(request)) {
                    if (response.getCode() != 202) {
                        throw new RuntimeException("Failed to pause connector: HTTP " + response.getCode());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to pause connector: {}", connectorName, e);
                throw new CompletionException(e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> resumeConnectorAsync(String connectUrl, String connectorName) {
        return CompletableFuture.runAsync(() -> {
            try {
                HttpPut request = new HttpPut(connectUrl + "/connectors/" + connectorName + "/resume");
                try (var response = httpClient.execute(request)) {
                    if (response.getCode() != 202) {
                        throw new RuntimeException("Failed to resume connector: HTTP " + response.getCode());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to resume connector: {}", connectorName, e);
                throw new CompletionException(e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> restartConnectorAsync(String connectUrl, String connectorName) {
        return CompletableFuture.runAsync(() -> {
            try {
                HttpPost request = new HttpPost(connectUrl + "/connectors/" + connectorName + "/restart");
                try (var response = httpClient.execute(request)) {
                    if (response.getCode() != 204) {
                        throw new RuntimeException("Failed to restart connector: HTTP " + response.getCode());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to restart connector: {}", connectorName, e);
                throw new CompletionException(e);
            }
        });
    }
    
    @Override
    public CompletableFuture<List<Map<String, Object>>> getConnectorTasksAsync(String connectUrl, String connectorName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpGet request = new HttpGet(connectUrl + "/connectors/" + connectorName + "/tasks");
                try (var response = httpClient.execute(request)) {
                    if (response.getCode() == 200) {
                        String responseBody = EntityUtils.toString(response.getEntity());
                        return objectMapper.readValue(responseBody, List.class);
                    } else {
                        throw new RuntimeException("Failed to get connector tasks: HTTP " + response.getCode());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to get connector tasks: {}", connectorName, e);
                throw new CompletionException(e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> restartTaskAsync(String connectUrl, String connectorName, int taskId) {
        return CompletableFuture.runAsync(() -> {
            try {
                HttpPost request = new HttpPost(connectUrl + "/connectors/" + connectorName + "/tasks/" + taskId + "/restart");
                try (var response = httpClient.execute(request)) {
                    if (response.getCode() != 204) {
                        throw new RuntimeException("Failed to restart task: HTTP " + response.getCode());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to restart task: {}/{}", connectorName, taskId, e);
                throw new CompletionException(e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Map<String, Object>> getConnectorStatusAsync(String connectUrl, String connectorName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpGet request = new HttpGet(connectUrl + "/connectors/" + connectorName + "/status");
                try (var response = httpClient.execute(request)) {
                    if (response.getCode() == 200) {
                        String responseBody = EntityUtils.toString(response.getEntity());
                        return objectMapper.readValue(responseBody, Map.class);
                    } else {
                        throw new RuntimeException("Failed to get connector status: HTTP " + response.getCode());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to get connector status: {}", connectorName, e);
                throw new CompletionException(e);
            }
        });
    }
    
    @Override
    public CompletableFuture<List<Map<String, Object>>> getConnectorPluginsAsync(String connectUrl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpGet request = new HttpGet(connectUrl + "/connector-plugins");
                try (var response = httpClient.execute(request)) {
                    if (response.getCode() == 200) {
                        String responseBody = EntityUtils.toString(response.getEntity());
                        return objectMapper.readValue(responseBody, List.class);
                    } else {
                        throw new RuntimeException("Failed to get connector plugins: HTTP " + response.getCode());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to get connector plugins", e);
                throw new CompletionException(e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Map<String, Object>> validateConnectorConfigAsync(String connectUrl, String connectorClass, 
                                                                               Map<String, String> config) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpPut request = new HttpPut(connectUrl + "/connector-plugins/" + connectorClass + "/config/validate");
                request.setHeader("Content-Type", "application/json");
                
                String configJson = objectMapper.writeValueAsString(config);
                request.setEntity(new StringEntity(configJson));
                
                try (var response = httpClient.execute(request)) {
                    if (response.getCode() == 200) {
                        String responseBody = EntityUtils.toString(response.getEntity());
                        return objectMapper.readValue(responseBody, Map.class);
                    } else {
                        throw new RuntimeException("Failed to validate connector config: HTTP " + response.getCode());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to validate connector config", e);
                throw new CompletionException(e);
            }
        });
    }
}