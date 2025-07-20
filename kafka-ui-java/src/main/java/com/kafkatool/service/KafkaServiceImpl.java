package com.kafkatool.service;

import com.kafkatool.model.KafkaMessage;
import com.kafkatool.model.TopicInfo;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Implementation of KafkaService using Apache Kafka Java client
 */
public class KafkaServiceImpl implements KafkaService {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaServiceImpl.class);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final String CONSUMER_GROUP_ID = "kafka-ui-tool-consumer";
    
    @Override
    public CompletableFuture<Boolean> testConnectionAsync(String brokerUrls) {
        return CompletableFuture.supplyAsync(() -> {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");
            props.put(AdminClientConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, "10000");
            
            try (AdminClient adminClient = AdminClient.create(props)) {
                DescribeClusterResult result = adminClient.describeCluster();
                result.clusterId().get();
                logger.info("Successfully connected to Kafka cluster at {}", brokerUrls);
                return true;
            } catch (Exception e) {
                logger.error("Failed to connect to Kafka cluster at {}: {}", brokerUrls, e.getMessage());
                return false;
            }
        });
    }
    
    @Override
    public CompletableFuture<String> getKafkaVersionAsync(String brokerUrls) {
        return CompletableFuture.supplyAsync(() -> {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            
            try (AdminClient adminClient = AdminClient.create(props)) {
                DescribeClusterResult result = adminClient.describeCluster();
                String clusterId = result.clusterId().get();
                return "Cluster ID: " + clusterId;
            } catch (Exception e) {
                logger.error("Failed to get Kafka version: {}", e.getMessage());
                return "Unknown";
            }
        });
    }
    
    @Override
    public CompletableFuture<List<TopicInfo>> getTopicsAsync(String brokerUrls) {
        return CompletableFuture.supplyAsync(() -> {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            
            try (AdminClient adminClient = AdminClient.create(props)) {
                ListTopicsResult topicsResult = adminClient.listTopics();
                Set<String> topicNames = topicsResult.names().get();
                
                List<TopicInfo> topics = new ArrayList<>();
                for (String topicName : topicNames) {
                    if (!topicName.startsWith("__")) { // Skip internal topics
                        DescribeTopicsResult describeResult = adminClient.describeTopics(Collections.singleton(topicName));
                        TopicDescription description = describeResult.all().get().get(topicName);
                        
                        TopicInfo topicInfo = new TopicInfo();
                        topicInfo.setName(topicName);
                        topicInfo.setPartitions(description.partitions().size());
                        if (!description.partitions().isEmpty()) {
                            topicInfo.setReplicationFactor(description.partitions().get(0).replicas().size());
                        }
                        topics.add(topicInfo);
                    }
                }
                
                logger.info("Retrieved {} topics from cluster", topics.size());
                return topics;
            } catch (Exception e) {
                logger.error("Failed to get topics: {}", e.getMessage());
                return new ArrayList<>();
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> createTopicAsync(String brokerUrls, String topicName, 
                                                    int partitions, int replicationFactor) {
        return CompletableFuture.runAsync(() -> {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            
            try (AdminClient adminClient = AdminClient.create(props)) {
                NewTopic newTopic = new NewTopic(topicName, partitions, (short) replicationFactor);
                CreateTopicsResult result = adminClient.createTopics(Collections.singleton(newTopic));
                result.all().get();
                logger.info("Successfully created topic: {}", topicName);
            } catch (Exception e) {
                logger.error("Failed to create topic {}: {}", topicName, e.getMessage());
                throw new RuntimeException("Failed to create topic: " + e.getMessage(), e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> deleteTopicAsync(String brokerUrls, String topicName) {
        return CompletableFuture.runAsync(() -> {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            
            try (AdminClient adminClient = AdminClient.create(props)) {
                DeleteTopicsResult result = adminClient.deleteTopics(Collections.singleton(topicName));
                result.all().get();
                logger.info("Successfully deleted topic: {}", topicName);
            } catch (Exception e) {
                logger.error("Failed to delete topic {}: {}", topicName, e.getMessage());
                throw new RuntimeException("Failed to delete topic: " + e.getMessage(), e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Map<String, String>> getTopicConfigAsync(String brokerUrls, String topicName) {
        return CompletableFuture.supplyAsync(() -> {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            
            try (AdminClient adminClient = AdminClient.create(props)) {
                ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
                DescribeConfigsResult result = adminClient.describeConfigs(Collections.singleton(resource));
                Config config = result.all().get().get(resource);
                
                Map<String, String> configMap = new HashMap<>();
                for (ConfigEntry entry : config.entries()) {
                    if (!entry.isDefault()) {
                        configMap.put(entry.name(), entry.value());
                    }
                }
                return configMap;
            } catch (Exception e) {
                logger.error("Failed to get topic config for {}: {}", topicName, e.getMessage());
                return new HashMap<>();
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> updateTopicConfigAsync(String brokerUrls, String topicName, 
                                                          Map<String, String> config) {
        return CompletableFuture.runAsync(() -> {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            
            try (AdminClient adminClient = AdminClient.create(props)) {
                ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
                
                List<ConfigEntry> entries = config.entrySet().stream()
                    .map(entry -> new ConfigEntry(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
                
                Config newConfig = new Config(entries);
                Map<ConfigResource, Config> configs = Collections.singletonMap(resource, newConfig);
                
                AlterConfigsResult result = adminClient.alterConfigs(configs);
                result.all().get();
                logger.info("Successfully updated config for topic: {}", topicName);
            } catch (Exception e) {
                logger.error("Failed to update topic config for {}: {}", topicName, e.getMessage());
                throw new RuntimeException("Failed to update topic config: " + e.getMessage(), e);
            }
        });
    }
    
    @Override
    public CompletableFuture<List<Integer>> getPartitionsAsync(String brokerUrls, String topicName) {
        return CompletableFuture.supplyAsync(() -> {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_ID);
            
            try (Consumer<String, String> consumer = new KafkaConsumer<>(props)) {
                List<PartitionInfo> partitionInfos = consumer.partitionsFor(topicName);
                return partitionInfos.stream()
                    .map(PartitionInfo::partition)
                    .sorted()
                    .collect(Collectors.toList());
            } catch (Exception e) {
                logger.error("Failed to get partitions for topic {}: {}", topicName, e.getMessage());
                return new ArrayList<>();
            }
        });
    }
    
    @Override
    public CompletableFuture<PartitionOffsets> getPartitionOffsetsAsync(String brokerUrls, 
                                                                        String topicName, int partition) {
        return CompletableFuture.supplyAsync(() -> {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_ID);
            
            try (Consumer<String, String> consumer = new KafkaConsumer<>(props)) {
                TopicPartition topicPartition = new TopicPartition(topicName, partition);
                
                Map<TopicPartition, Long> beginningOffsets = consumer.beginningOffsets(
                    Collections.singleton(topicPartition));
                Map<TopicPartition, Long> endOffsets = consumer.endOffsets(
                    Collections.singleton(topicPartition));
                
                long lowWatermark = beginningOffsets.get(topicPartition);
                long highWatermark = endOffsets.get(topicPartition);
                
                return new PartitionOffsets(lowWatermark, highWatermark);
            } catch (Exception e) {
                logger.error("Failed to get partition offsets for {}:{}: {}", 
                    topicName, partition, e.getMessage());
                return new PartitionOffsets(0, 0);
            }
        });
    }
    
    @Override
    public CompletableFuture<List<KafkaMessage>> getLatestMessagesAsync(String brokerUrls, 
                                                                       String topicName, 
                                                                       int partition, int count) {
        return CompletableFuture.supplyAsync(() -> {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_ID + "-" + UUID.randomUUID());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
            
            try (Consumer<String, String> consumer = new KafkaConsumer<>(props)) {
                TopicPartition topicPartition = new TopicPartition(topicName, partition);
                consumer.assign(Collections.singleton(topicPartition));
                
                // Get end offset and seek to count messages before it
                long endOffset = consumer.endOffsets(Collections.singleton(topicPartition)).get(topicPartition);
                long startOffset = Math.max(0, endOffset - count);
                consumer.seek(topicPartition, startOffset);
                
                List<KafkaMessage> messages = new ArrayList<>();
                long deadline = System.currentTimeMillis() + 5000; // 5 second timeout
                
                while (messages.size() < count && System.currentTimeMillis() < deadline) {
                    var records = consumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord<String, String> record : records) {
                        if (record.partition() == partition) {
                            messages.add(convertToKafkaMessage(record));
                            if (messages.size() >= count) break;
                        }
                    }
                }
                
                return messages;
            } catch (Exception e) {
                logger.error("Failed to get latest messages for {}:{}: {}", 
                    topicName, partition, e.getMessage());
                return new ArrayList<>();
            }
        });
    }
    
    @Override
    public CompletableFuture<List<KafkaMessage>> getMessagesBetweenOffsetsAsync(String brokerUrls,
                                                                               String topicName,
                                                                               int partition,
                                                                               long fromOffset,
                                                                               long toOffset) {
        return CompletableFuture.supplyAsync(() -> {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_ID + "-" + UUID.randomUUID());
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
            
            try (Consumer<String, String> consumer = new KafkaConsumer<>(props)) {
                TopicPartition topicPartition = new TopicPartition(topicName, partition);
                consumer.assign(Collections.singleton(topicPartition));
                consumer.seek(topicPartition, fromOffset);
                
                List<KafkaMessage> messages = new ArrayList<>();
                long deadline = System.currentTimeMillis() + 10000; // 10 second timeout
                
                while (System.currentTimeMillis() < deadline) {
                    var records = consumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord<String, String> record : records) {
                        if (record.partition() == partition && record.offset() <= toOffset) {
                            messages.add(convertToKafkaMessage(record));
                        }
                        if (record.offset() >= toOffset) {
                            return messages;
                        }
                    }
                    if (records.isEmpty()) break;
                }
                
                return messages;
            } catch (Exception e) {
                logger.error("Failed to get messages between offsets for {}:{}: {}", 
                    topicName, partition, e.getMessage());
                return new ArrayList<>();
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> produceMessageAsync(String brokerUrls, String topicName,
                                                      String key, String value,
                                                      Map<String, String> headers, int partition) {
        return CompletableFuture.runAsync(() -> {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            
            try (Producer<String, String> producer = new KafkaProducer<>(props)) {
                ProducerRecord<String, String> record = new ProducerRecord<>(topicName, partition, key, value);
                
                if (headers != null) {
                    headers.forEach((k, v) -> record.headers().add(k, v.getBytes()));
                }
                
                producer.send(record).get();
                logger.info("Successfully produced message to {}:{}", topicName, partition);
            } catch (Exception e) {
                logger.error("Failed to produce message to {}:{}: {}", topicName, partition, e.getMessage());
                throw new RuntimeException("Failed to produce message: " + e.getMessage(), e);
            }
        });
    }
    
    @Override
    public CompletableFuture<List<KafkaMessage>> searchMessagesAsync(String brokerUrls,
                                                                    String topicName,
                                                                    int partition,
                                                                    String searchPattern,
                                                                    boolean searchInKey,
                                                                    boolean searchInValue,
                                                                    int maxResults) {
        return CompletableFuture.supplyAsync(() -> {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_ID + "-search-" + UUID.randomUUID());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
            
            try (Consumer<String, String> consumer = new KafkaConsumer<>(props)) {
                TopicPartition topicPartition = new TopicPartition(topicName, partition);
                consumer.assign(Collections.singleton(topicPartition));
                consumer.seekToBeginning(Collections.singleton(topicPartition));
                
                List<KafkaMessage> matchingMessages = new ArrayList<>();
                long deadline = System.currentTimeMillis() + 30000; // 30 second timeout for search
                
                while (matchingMessages.size() < maxResults && System.currentTimeMillis() < deadline) {
                    var records = consumer.poll(Duration.ofMillis(1000));
                    if (records.isEmpty()) break;
                    
                    for (ConsumerRecord<String, String> record : records) {
                        if (record.partition() == partition) {
                            boolean matches = false;
                            
                            if (searchInKey && record.key() != null && 
                                record.key().toLowerCase().contains(searchPattern.toLowerCase())) {
                                matches = true;
                            }
                            
                            if (searchInValue && record.value() != null && 
                                record.value().toLowerCase().contains(searchPattern.toLowerCase())) {
                                matches = true;
                            }
                            
                            if (matches) {
                                matchingMessages.add(convertToKafkaMessage(record));
                                if (matchingMessages.size() >= maxResults) break;
                            }
                        }
                    }
                }
                
                logger.info("Found {} matching messages for pattern '{}' in {}:{}", 
                    matchingMessages.size(), searchPattern, topicName, partition);
                return matchingMessages;
            } catch (Exception e) {
                logger.error("Failed to search messages in {}:{}: {}", topicName, partition, e.getMessage());
                return new ArrayList<>();
            }
        });
    }
    
    private KafkaMessage convertToKafkaMessage(ConsumerRecord<String, String> record) {
        Map<String, String> headers = new HashMap<>();
        for (Header header : record.headers()) {
            headers.put(header.key(), new String(header.value()));
        }
        
        LocalDateTime timestamp = null;
        if (record.timestamp() > 0) {
            timestamp = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(record.timestamp()), ZoneId.systemDefault());
        }
        
        return new KafkaMessage(
            record.offset(),
            record.partition(),
            record.key(),
            record.value(),
            timestamp,
            headers
        );
    }
}