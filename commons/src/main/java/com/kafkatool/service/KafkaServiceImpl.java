package com.kafkatool.service;

import com.kafkatool.model.*;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
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
                logger.error("Failed to connect to Kafka cluster at {}", brokerUrls, e);
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
                logger.error("Failed to get Kafka version", e);
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
                        topicInfo.setReplicationFactor((short) description.partitions().get(0).replicas().size());
                        }
                        topics.add(topicInfo);
                    }
                }
                
                logger.info("Retrieved {} topics from cluster", topics.size());
                return topics;
            } catch (Exception e) {
                logger.error("Failed to get topics", e);
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
                logger.error("Failed to create topic {}", topicName, e);
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
                logger.error("Failed to delete topic {}", topicName, e);
                throw new RuntimeException("Failed to delete topic: " + e.getMessage(), e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> addPartitionsToTopicAsync(String brokerUrls, String topicName, int newPartitionCount) {
        return CompletableFuture.runAsync(() -> {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            
            try (AdminClient adminClient = AdminClient.create(props)) {
                Map<String, NewPartitions> partitionsMap = new HashMap<>();
                partitionsMap.put(topicName, NewPartitions.increaseTo(newPartitionCount));
                
                CreatePartitionsResult result = adminClient.createPartitions(partitionsMap);
                result.all().get();
                logger.info("Successfully added partitions to topic {}: new count = {}", topicName, newPartitionCount);
            } catch (Exception e) {
                logger.error("Failed to add partitions to topic {}", topicName, e);
                throw new RuntimeException("Failed to add partitions to topic: " + e.getMessage(), e);
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
                logger.error("Failed to get topic config for {}", topicName, e);
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
                logger.error("Failed to update topic config for {}", topicName, e);
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
                logger.error("Failed to get partitions for topic {}", topicName, e);
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
                logger.error("Failed to get partition offsets for {}:{}", topicName, partition, e);
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
                logger.error("Failed to get latest messages for {}:{}", topicName, partition, e);
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
                logger.error("Failed to get messages between offsets for {}:{}", topicName, partition, e);
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
                logger.error("Failed to produce message to {}:{}", topicName, partition, e);
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
                logger.error("Failed to search messages in {}:{}", topicName, partition, e);
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
            record.topic(),
            record.offset(),
            record.partition(),
            record.key(),
            record.value(),
            timestamp,
            headers
        );
    }
    
    // ===== CONSUMER GROUP MANAGEMENT IMPLEMENTATION =====
    
    @Override
    public CompletableFuture<List<ConsumerGroupInfo>> getConsumerGroupsAsync(String brokerUrls) {
        return CompletableFuture.supplyAsync(() -> {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            
            try (AdminClient adminClient = AdminClient.create(props)) {
                ListConsumerGroupsResult result = adminClient.listConsumerGroups();
                Collection<ConsumerGroupListing> groupListings = result.all().get();
                
                List<ConsumerGroupInfo> consumerGroups = new ArrayList<>();
                for (ConsumerGroupListing listing : groupListings) {
                    ConsumerGroupInfo groupInfo = new ConsumerGroupInfo();
                    groupInfo.setGroupId(listing.groupId());
                    
                    // Basic group state - will be filled in later or left as default
                    groupInfo.setState("Active");
                    
                    // Get detailed info for each group
                    try {
                        DescribeConsumerGroupsResult describeResult = adminClient.describeConsumerGroups(
                            Collections.singleton(listing.groupId()));
                        ConsumerGroupDescription description = describeResult.all().get().get(listing.groupId());
                        
                        groupInfo.setMemberCount(description.members().size());
                        groupInfo.setCoordinator(description.coordinator().toString());
                        groupInfo.setState(description.state().toString());
                        // protocolType() and protocol() methods may not be available in all Kafka versions
                        groupInfo.setProtocolType("consumer");
                        groupInfo.setProtocol("range");
                    } catch (Exception e) {
                        logger.warn("Failed to get details for consumer group {}: {}", 
                            listing.groupId(), e.getMessage());
                        groupInfo.setMemberCount(0);
                    }
                    
                    consumerGroups.add(groupInfo);
                }
                
                logger.info("Retrieved {} consumer groups", consumerGroups.size());
                return consumerGroups;
            } catch (Exception e) {
                logger.error("Failed to get consumer groups", e);
                return new ArrayList<>();
            }
        });
    }
    
    @Override
    public CompletableFuture<ConsumerGroupInfo> getConsumerGroupDetailsAsync(String brokerUrls, String groupId) {
        return CompletableFuture.supplyAsync(() -> {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            
            try (AdminClient adminClient = AdminClient.create(props)) {
                DescribeConsumerGroupsResult result = adminClient.describeConsumerGroups(
                    Collections.singleton(groupId));
                ConsumerGroupDescription description = result.all().get().get(groupId);
                
                ConsumerGroupInfo groupInfo = new ConsumerGroupInfo();
                groupInfo.setGroupId(description.groupId());
                groupInfo.setState(description.state().toString());
                groupInfo.setMemberCount(description.members().size());
                groupInfo.setCoordinator(description.coordinator().toString());
                groupInfo.setState(description.state().toString());
                // protocolType() and protocol() methods may not be available in all Kafka versions
                groupInfo.setProtocolType("consumer");
                groupInfo.setProtocol("range");
                
                return groupInfo;
            } catch (Exception e) {
                logger.error("Failed to get consumer group details for {}", groupId, e);
                return new ConsumerGroupInfo();
            }
        });
    }
    
    @Override
    public CompletableFuture<List<ConsumerGroupOffsets>> getConsumerGroupOffsetsAsync(String brokerUrls, String groupId) {
        return CompletableFuture.supplyAsync(() -> {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            
            try (AdminClient adminClient = AdminClient.create(props)) {
                // Get committed offsets for the consumer group
                ListConsumerGroupOffsetsResult offsetsResult = adminClient.listConsumerGroupOffsets(groupId);
                Map<TopicPartition, OffsetAndMetadata> offsets = offsetsResult.partitionsToOffsetAndMetadata().get();
                
                List<ConsumerGroupOffsets> groupOffsets = new ArrayList<>();
                
                // Get log end offsets for comparison
                Properties consumerProps = new Properties();
                consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
                consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
                consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
                consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_ID + "-offset-check");
                
                try (Consumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
                    Map<TopicPartition, Long> endOffsets = consumer.endOffsets(offsets.keySet());
                    
                    for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : offsets.entrySet()) {
                        TopicPartition partition = entry.getKey();
                        OffsetAndMetadata offsetMetadata = entry.getValue();
                        
                        Long endOffset = endOffsets.get(partition);
                        if (endOffset == null) endOffset = 0L;
                        
                        ConsumerGroupOffsets cgOffsets = new ConsumerGroupOffsets();
                        cgOffsets.setGroupId(groupId);
                        cgOffsets.setTopicName(partition.topic());
                        cgOffsets.setPartition(partition.partition());
                        cgOffsets.setCurrentOffset(offsetMetadata.offset());
                        cgOffsets.setLogEndOffset(endOffset);
                        cgOffsets.setClientId(offsetMetadata.metadata());
                        
                        groupOffsets.add(cgOffsets);
                    }
                }
                
                logger.info("Retrieved offset information for {} partitions in consumer group {}", 
                    groupOffsets.size(), groupId);
                return groupOffsets;
            } catch (Exception e) {
                logger.error("Failed to get consumer group offsets for {}", groupId, e);
                return new ArrayList<>();
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> resetConsumerGroupOffsetsToEarliestAsync(String brokerUrls, String groupId, String topicName) {
        return CompletableFuture.runAsync(() -> {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            
            try (AdminClient adminClient = AdminClient.create(props)) {
                // Get all partitions for the topic
                DescribeTopicsResult topicsResult = adminClient.describeTopics(Collections.singleton(topicName));
                TopicDescription description = topicsResult.all().get().get(topicName);
                
                // Get beginning offsets for all partitions
                Properties consumerProps = new Properties();
                consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
                consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
                consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
                consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_ID + "-reset");
                
                try (Consumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
                    List<TopicPartition> partitions = description.partitions().stream()
                        .map(p -> new TopicPartition(topicName, p.partition()))
                        .collect(Collectors.toList());
                    
                    Map<TopicPartition, Long> beginningOffsets = consumer.beginningOffsets(partitions);
                    
                    Map<TopicPartition, OffsetAndMetadata> offsetsToReset = beginningOffsets.entrySet().stream()
                        .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> new OffsetAndMetadata(entry.getValue())
                        ));
                    
                    AlterConsumerGroupOffsetsResult result = adminClient.alterConsumerGroupOffsets(groupId, offsetsToReset);
                    result.all().get();
                    
                    logger.info("Successfully reset offsets to earliest for consumer group {} on topic {}", groupId, topicName);
                }
            } catch (Exception e) {
                logger.error("Failed to reset consumer group offsets for {}:{}", groupId, topicName, e);
                throw new RuntimeException("Failed to reset offsets: " + e.getMessage(), e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> resetConsumerGroupOffsetsToLatestAsync(String brokerUrls, String groupId, String topicName) {
        return CompletableFuture.runAsync(() -> {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            
            try (AdminClient adminClient = AdminClient.create(props)) {
                // Get all partitions for the topic
                DescribeTopicsResult topicsResult = adminClient.describeTopics(Collections.singleton(topicName));
                TopicDescription description = topicsResult.all().get().get(topicName);
                
                // Get end offsets for all partitions
                Properties consumerProps = new Properties();
                consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
                consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
                consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
                consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_ID + "-reset");
                
                try (Consumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
                    List<TopicPartition> partitions = description.partitions().stream()
                        .map(p -> new TopicPartition(topicName, p.partition()))
                        .collect(Collectors.toList());
                    
                    Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);
                    
                    Map<TopicPartition, OffsetAndMetadata> offsetsToReset = endOffsets.entrySet().stream()
                        .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> new OffsetAndMetadata(entry.getValue())
                        ));
                    
                    AlterConsumerGroupOffsetsResult result = adminClient.alterConsumerGroupOffsets(groupId, offsetsToReset);
                    result.all().get();
                    
                    logger.info("Successfully reset offsets to latest for consumer group {} on topic {}", groupId, topicName);
                }
            } catch (Exception e) {
                logger.error("Failed to reset consumer group offsets for {}:{}", groupId, topicName, e);
                throw new RuntimeException("Failed to reset offsets: " + e.getMessage(), e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> resetConsumerGroupOffsetsToOffsetAsync(String brokerUrls, String groupId, 
                                                                         String topicName, int partition, long offset) {
        return CompletableFuture.runAsync(() -> {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            
            try (AdminClient adminClient = AdminClient.create(props)) {
                TopicPartition topicPartition = new TopicPartition(topicName, partition);
                Map<TopicPartition, OffsetAndMetadata> offsetsToReset = 
                    Collections.singletonMap(topicPartition, new OffsetAndMetadata(offset));
                
                AlterConsumerGroupOffsetsResult result = adminClient.alterConsumerGroupOffsets(groupId, offsetsToReset);
                result.all().get();
                
                logger.info("Successfully reset offset to {} for consumer group {} on {}:{}", 
                    offset, groupId, topicName, partition);
            } catch (Exception e) {
                logger.error("Failed to reset consumer group offset for {}:{}:{}: {}", 
                    groupId, topicName, partition, e.getMessage());
                throw new RuntimeException("Failed to reset offset: " + e.getMessage(), e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> deleteConsumerGroupAsync(String brokerUrls, String groupId) {
        return CompletableFuture.runAsync(() -> {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            
            try (AdminClient adminClient = AdminClient.create(props)) {
                DeleteConsumerGroupsResult result = adminClient.deleteConsumerGroups(Collections.singleton(groupId));
                result.all().get();
                logger.info("Successfully deleted consumer group: {}", groupId);
            } catch (Exception e) {
                logger.error("Failed to delete consumer group {}", groupId, e);
                throw new RuntimeException("Failed to delete consumer group: " + e.getMessage(), e);
            }
        });
    }
    
    // ===== BROKER AND CLUSTER MANAGEMENT IMPLEMENTATION =====
    
    @Override
    public CompletableFuture<List<BrokerInfo>> getBrokersAsync(String brokerUrls) {
        return CompletableFuture.supplyAsync(() -> {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            
            try (AdminClient adminClient = AdminClient.create(props)) {
                DescribeClusterResult clusterResult = adminClient.describeCluster();
                Collection<Node> nodes = clusterResult.nodes().get();
                Node controller = clusterResult.controller().get();
                
                List<BrokerInfo> brokers = new ArrayList<>();
                for (Node node : nodes) {
                    BrokerInfo brokerInfo = new BrokerInfo();
                    brokerInfo.setId(node.id());
                    brokerInfo.setHost(node.host());
                    brokerInfo.setPort(node.port());
                    brokerInfo.setRack(node.rack());
                    brokerInfo.setController(controller != null && controller.id() == node.id());
                    
                    brokers.add(brokerInfo);
                }
                
                logger.info("Retrieved {} brokers from cluster", brokers.size());
                return brokers;
            } catch (Exception e) {
                logger.error("Failed to get brokers", e);
                return new ArrayList<>();
            }
        });
    }
    
    @Override
    public CompletableFuture<List<ClusterConfig>> getClusterConfigAsync(String brokerUrls) {
        return CompletableFuture.supplyAsync(() -> {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            
            try (AdminClient adminClient = AdminClient.create(props)) {
                ConfigResource resource = new ConfigResource(ConfigResource.Type.BROKER, "");
                DescribeConfigsResult result = adminClient.describeConfigs(Collections.singleton(resource));
                Config config = result.all().get().get(resource);
                
                List<ClusterConfig> configs = new ArrayList<>();
                for (ConfigEntry entry : config.entries()) {
                    ClusterConfig clusterConfig = new ClusterConfig();
                    clusterConfig.setName(entry.name());
                    clusterConfig.setValue(entry.value());
                    clusterConfig.setSource(entry.source().toString());
                    clusterConfig.setReadOnly(entry.isReadOnly());
                    clusterConfig.setSensitive(entry.isSensitive());
                    clusterConfig.setDocumentation(entry.documentation());
                    clusterConfig.setType(entry.type() != null ? entry.type().toString() : "");
                    
                    configs.add(clusterConfig);
                }
                
                logger.info("Retrieved {} cluster configuration properties", configs.size());
                return configs;
            } catch (Exception e) {
                logger.error("Failed to get cluster config", e);
                return new ArrayList<>();
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> updateClusterConfigAsync(String brokerUrls, Map<String, String> config) {
        return CompletableFuture.runAsync(() -> {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            
            try (AdminClient adminClient = AdminClient.create(props)) {
                ConfigResource resource = new ConfigResource(ConfigResource.Type.BROKER, "");
                
                List<ConfigEntry> entries = config.entrySet().stream()
                    .map(entry -> new ConfigEntry(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
                
                Config newConfig = new Config(entries);
                Map<ConfigResource, Config> configs = Collections.singletonMap(resource, newConfig);
                
                AlterConfigsResult result = adminClient.alterConfigs(configs);
                result.all().get();
                logger.info("Successfully updated cluster configuration");
            } catch (Exception e) {
                logger.error("Failed to update cluster config", e);
                throw new RuntimeException("Failed to update cluster config: " + e.getMessage(), e);
            }
        });
    }
    
    @Override
    public CompletableFuture<List<ClusterConfig>> getBrokerConfigAsync(String brokerUrls, int brokerId) {
        return CompletableFuture.supplyAsync(() -> {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            
            try (AdminClient adminClient = AdminClient.create(props)) {
                ConfigResource resource = new ConfigResource(ConfigResource.Type.BROKER, String.valueOf(brokerId));
                DescribeConfigsResult result = adminClient.describeConfigs(Collections.singleton(resource));
                Config config = result.all().get().get(resource);
                
                List<ClusterConfig> configs = new ArrayList<>();
                for (ConfigEntry entry : config.entries()) {
                    ClusterConfig brokerConfig = new ClusterConfig();
                    brokerConfig.setName(entry.name());
                    brokerConfig.setValue(entry.value());
                    brokerConfig.setSource(entry.source().toString());
                    brokerConfig.setReadOnly(entry.isReadOnly());
                    brokerConfig.setSensitive(entry.isSensitive());
                    brokerConfig.setDocumentation(entry.documentation());
                    brokerConfig.setType(entry.type() != null ? entry.type().toString() : "");
                    
                    configs.add(brokerConfig);
                }
                
                logger.info("Retrieved {} configuration properties for broker {}", configs.size(), brokerId);
                return configs;
            } catch (Exception e) {
                logger.error("Failed to get broker config for {}", brokerId, e);
                return new ArrayList<>();
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> updateBrokerConfigAsync(String brokerUrls, int brokerId, Map<String, String> config) {
        return CompletableFuture.runAsync(() -> {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
            
            try (AdminClient adminClient = AdminClient.create(props)) {
                ConfigResource resource = new ConfigResource(ConfigResource.Type.BROKER, String.valueOf(brokerId));
                
                List<ConfigEntry> entries = config.entrySet().stream()
                    .map(entry -> new ConfigEntry(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
                
                Config newConfig = new Config(entries);
                Map<ConfigResource, Config> configs = Collections.singletonMap(resource, newConfig);
                
                AlterConfigsResult result = adminClient.alterConfigs(configs);
                result.all().get();
                logger.info("Successfully updated configuration for broker {}", brokerId);
            } catch (Exception e) {
                logger.error("Failed to update broker config for {}", brokerId, e);
                throw new RuntimeException("Failed to update broker config: " + e.getMessage(), e);
            }
        });
    }
}