package com.kafkatool.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkatool.model.SchemaInfo;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of SchemaRegistryService for Confluent Schema Registry integration
 */
public class SchemaRegistryServiceImpl implements SchemaRegistryService {
    
    private static final Logger logger = LoggerFactory.getLogger(SchemaRegistryServiceImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public CompletableFuture<Boolean> testConnectionAsync(String schemaRegistryUrl) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(schemaRegistryUrl.endsWith("/") ? 
                    schemaRegistryUrl + "subjects" : schemaRegistryUrl + "/subjects");
                
                var response = client.execute(request);
                boolean connected = response.getCode() == HttpStatus.SC_OK;
                logger.info("Schema Registry connection test: {} - {}", 
                    schemaRegistryUrl, connected ? "SUCCESS" : "FAILED");
                return connected;
            } catch (Exception e) {
                logger.error("Failed to connect to Schema Registry {}: {}", schemaRegistryUrl, e.getMessage());
                return false;
            }
        });
    }
    
    @Override
    public CompletableFuture<List<String>> getSubjectsAsync(String schemaRegistryUrl) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(buildUrl(schemaRegistryUrl, "/subjects"));
                
                var response = client.execute(request);
                if (response.getCode() == HttpStatus.SC_OK) {
                    String responseBody = new String(response.getEntity().getContent().readAllBytes());
                    JsonNode jsonArray = objectMapper.readTree(responseBody);
                    
                    List<String> subjects = new ArrayList<>();
                    for (JsonNode node : jsonArray) {
                        subjects.add(node.asText());
                    }
                    return subjects;
                }
                return new ArrayList<>();
            } catch (Exception e) {
                logger.error("Failed to get subjects from Schema Registry: {}", e.getMessage());
                return new ArrayList<>();
            }
        });
    }
    
    @Override
    public CompletableFuture<List<Integer>> getSubjectVersionsAsync(String schemaRegistryUrl, String subject) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(buildUrl(schemaRegistryUrl, "/subjects/" + subject + "/versions"));
                
                var response = client.execute(request);
                if (response.getCode() == HttpStatus.SC_OK) {
                    String responseBody = new String(response.getEntity().getContent().readAllBytes());
                    JsonNode jsonArray = objectMapper.readTree(responseBody);
                    
                    List<Integer> versions = new ArrayList<>();
                    for (JsonNode node : jsonArray) {
                        versions.add(node.asInt());
                    }
                    return versions;
                }
                return new ArrayList<>();
            } catch (Exception e) {
                logger.error("Failed to get versions for subject {}: {}", subject, e.getMessage());
                return new ArrayList<>();
            }
        });
    }
    
    @Override
    public CompletableFuture<SchemaInfo> getSchemaAsync(String schemaRegistryUrl, String subject, int version) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(buildUrl(schemaRegistryUrl, 
                    "/subjects/" + subject + "/versions/" + version));
                
                var response = client.execute(request);
                if (response.getCode() == HttpStatus.SC_OK) {
                    String responseBody = new String(response.getEntity().getContent().readAllBytes());
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    
                    SchemaInfo schemaInfo = new SchemaInfo();
                    schemaInfo.setId(jsonNode.get("id").asInt());
                    schemaInfo.setSubject(jsonNode.get("subject").asText());
                    schemaInfo.setVersion(jsonNode.get("version").asInt());
                    schemaInfo.setSchemaType(jsonNode.has("schemaType") ? 
                        jsonNode.get("schemaType").asText() : "AVRO");
                    schemaInfo.setSchema(jsonNode.get("schema").asText());
                    
                    return schemaInfo;
                }
                return null;
            } catch (Exception e) {
                logger.error("Failed to get schema for {}:{}: {}", subject, version, e.getMessage());
                return null;
            }
        });
    }
    
    @Override
    public CompletableFuture<SchemaInfo> getLatestSchemaAsync(String schemaRegistryUrl, String subject) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(buildUrl(schemaRegistryUrl, 
                    "/subjects/" + subject + "/versions/latest"));
                
                var response = client.execute(request);
                if (response.getCode() == HttpStatus.SC_OK) {
                    String responseBody = new String(response.getEntity().getContent().readAllBytes());
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    
                    SchemaInfo schemaInfo = new SchemaInfo();
                    schemaInfo.setId(jsonNode.get("id").asInt());
                    schemaInfo.setSubject(jsonNode.get("subject").asText());
                    schemaInfo.setVersion(jsonNode.get("version").asInt());
                    schemaInfo.setSchemaType(jsonNode.has("schemaType") ? 
                        jsonNode.get("schemaType").asText() : "AVRO");
                    schemaInfo.setSchema(jsonNode.get("schema").asText());
                    
                    return schemaInfo;
                }
                return null;
            } catch (Exception e) {
                logger.error("Failed to get latest schema for subject {}: {}", subject, e.getMessage());
                return null;
            }
        });
    }
    
    @Override
    public CompletableFuture<Integer> registerSchemaAsync(String schemaRegistryUrl, String subject, 
                                                         String schemaType, String schema) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost request = new HttpPost(buildUrl(schemaRegistryUrl, "/subjects/" + subject + "/versions"));
                
                String requestBody = String.format(
                    "{\"schemaType\":\"%s\",\"schema\":\"%s\"}", 
                    schemaType, schema.replace("\"", "\\\""));
                    
                request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
                
                var response = client.execute(request);
                if (response.getCode() == HttpStatus.SC_OK) {
                    String responseBody = new String(response.getEntity().getContent().readAllBytes());
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    return jsonNode.get("id").asInt();
                }
                return -1;
            } catch (Exception e) {
                logger.error("Failed to register schema for subject {}: {}", subject, e.getMessage());
                return -1;
            }
        });
    }
    
    @Override
    public CompletableFuture<List<Integer>> deleteSubjectAsync(String schemaRegistryUrl, String subject) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpDelete request = new HttpDelete(buildUrl(schemaRegistryUrl, "/subjects/" + subject));
                
                var response = client.execute(request);
                if (response.getCode() == HttpStatus.SC_OK) {
                    String responseBody = new String(response.getEntity().getContent().readAllBytes());
                    JsonNode jsonArray = objectMapper.readTree(responseBody);
                    
                    List<Integer> deletedVersions = new ArrayList<>();
                    for (JsonNode node : jsonArray) {
                        deletedVersions.add(node.asInt());
                    }
                    return deletedVersions;
                }
                return new ArrayList<>();
            } catch (Exception e) {
                logger.error("Failed to delete subject {}: {}", subject, e.getMessage());
                return new ArrayList<>();
            }
        });
    }
    
    @Override
    public CompletableFuture<Integer> deleteSchemaVersionAsync(String schemaRegistryUrl, String subject, int version) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpDelete request = new HttpDelete(buildUrl(schemaRegistryUrl, 
                    "/subjects/" + subject + "/versions/" + version));
                
                var response = client.execute(request);
                if (response.getCode() == HttpStatus.SC_OK) {
                    String responseBody = new String(response.getEntity().getContent().readAllBytes());
                    return Integer.parseInt(responseBody);
                }
                return -1;
            } catch (Exception e) {
                logger.error("Failed to delete version {} for subject {}: {}", version, subject, e.getMessage());
                return -1;
            }
        });
    }
    
    @Override
    public CompletableFuture<String> getCompatibilityAsync(String schemaRegistryUrl, String subject) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(buildUrl(schemaRegistryUrl, "/config/" + subject));
                
                var response = client.execute(request);
                if (response.getCode() == HttpStatus.SC_OK) {
                    String responseBody = new String(response.getEntity().getContent().readAllBytes());
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    return jsonNode.get("compatibility").asText();
                }
                return "FULL"; // Default
            } catch (Exception e) {
                logger.error("Failed to get compatibility for subject {}: {}", subject, e.getMessage());
                return "FULL";
            }
        });
    }
    
    @Override
    public CompletableFuture<String> setCompatibilityAsync(String schemaRegistryUrl, String subject, String compatibility) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPut request = new HttpPut(buildUrl(schemaRegistryUrl, "/config/" + subject));
                
                String requestBody = String.format("{\"compatibility\":\"%s\"}", compatibility);
                request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
                
                var response = client.execute(request);
                if (response.getCode() == HttpStatus.SC_OK) {
                    String responseBody = new String(response.getEntity().getContent().readAllBytes());
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    return jsonNode.get("compatibility").asText();
                }
                return compatibility;
            } catch (Exception e) {
                logger.error("Failed to set compatibility for subject {}: {}", subject, e.getMessage());
                return compatibility;
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> testCompatibilityAsync(String schemaRegistryUrl, String subject, 
                                                            String schemaType, String schema) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost request = new HttpPost(buildUrl(schemaRegistryUrl, 
                    "/compatibility/subjects/" + subject + "/versions/latest"));
                
                String requestBody = String.format(
                    "{\"schemaType\":\"%s\",\"schema\":\"%s\"}", 
                    schemaType, schema.replace("\"", "\\\""));
                    
                request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
                
                var response = client.execute(request);
                if (response.getCode() == HttpStatus.SC_OK) {
                    String responseBody = new String(response.getEntity().getContent().readAllBytes());
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    return jsonNode.get("is_compatible").asBoolean();
                }
                return false;
            } catch (Exception e) {
                logger.error("Failed to test compatibility for subject {}: {}", subject, e.getMessage());
                return false;
            }
        });
    }
    
    @Override
    public CompletableFuture<String> getGlobalCompatibilityAsync(String schemaRegistryUrl) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(buildUrl(schemaRegistryUrl, "/config"));
                
                var response = client.execute(request);
                if (response.getCode() == HttpStatus.SC_OK) {
                    String responseBody = new String(response.getEntity().getContent().readAllBytes());
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    return jsonNode.get("compatibility").asText();
                }
                return "FULL"; // Default
            } catch (Exception e) {
                logger.error("Failed to get global compatibility: {}", e.getMessage());
                return "FULL";
            }
        });
    }
    
    @Override
    public CompletableFuture<String> setGlobalCompatibilityAsync(String schemaRegistryUrl, String compatibility) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPut request = new HttpPut(buildUrl(schemaRegistryUrl, "/config"));
                
                String requestBody = String.format("{\"compatibility\":\"%s\"}", compatibility);
                request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
                
                var response = client.execute(request);
                if (response.getCode() == HttpStatus.SC_OK) {
                    String responseBody = new String(response.getEntity().getContent().readAllBytes());
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    return jsonNode.get("compatibility").asText();
                }
                return compatibility;
            } catch (Exception e) {
                logger.error("Failed to set global compatibility: {}", e.getMessage());
                return compatibility;
            }
        });
    }
    
    private String buildUrl(String baseUrl, String path) {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return base + path;
    }
}