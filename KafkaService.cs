using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Text;
using Confluent.Kafka;
using Confluent.Kafka.Admin;

namespace KafkaTool
{
    public class KafkaService : IKafkaService
    {
        // Simulate a real connection to a Kafka cluster
        public Task<bool> ConnectAsync(string brokerUrls)
        {
            var config = new AdminClientConfig { BootstrapServers = brokerUrls };
            try
            {
                using (var adminClient = new AdminClientBuilder(config).Build())
                {
                    // Try to get metadata for the cluster (non-intrusive, no topic creation)
                    var meta = adminClient.GetMetadata(TimeSpan.FromSeconds(3));
                    return Task.FromResult(meta.Brokers.Count > 0);
                }
            }
            catch
            {
                return Task.FromResult(false);
            }
        }

        // Get topic config (properties) for a topic
        public async Task<Dictionary<string, string>> GetTopicConfigAsync(string brokerUrls, string topicName)
        {
            var config = new AdminClientConfig { BootstrapServers = brokerUrls };
            using (var adminClient = new AdminClientBuilder(config).Build())
            {
                var resource = new ConfigResource { Type = ResourceType.Topic, Name = topicName };
                var configs = await adminClient.DescribeConfigsAsync(new List<ConfigResource> { resource });
                var dict = new Dictionary<string, string>();
                foreach (var entry in configs[0].Entries)
                {
                    // Only show non-defaults (isDefault == false)
                    if (!entry.Value.IsDefault)
                        dict[entry.Key] = entry.Value.Value;
                }
                return dict;
            }
        }

        // Alter topic config (update properties)
        public async Task AlterTopicConfigAsync(string brokerUrls, string topicName, Dictionary<string, string> newConfig)
        {
            var config = new AdminClientConfig { BootstrapServers = brokerUrls };
            using (var adminClient = new AdminClientBuilder(config).Build())
            {
                var resource = new ConfigResource { Type = ResourceType.Topic, Name = topicName };
                var configEntries = newConfig.Select(kv => {
                    var entry = new ConfigEntry();
                    entry.Name = kv.Key;
                    entry.Value = kv.Value;
                    return entry;
                }).ToList();
                var configDict = new Dictionary<ConfigResource, List<ConfigEntry>>
                {
                    [resource] = configEntries
                };
                await adminClient.AlterConfigsAsync(configDict);
            }
        }

        // Delete a topic
        public async Task DeleteTopicAsync(string brokerUrls, string topicName)
        {
            var config = new AdminClientConfig { BootstrapServers = brokerUrls };
            using (var adminClient = new AdminClientBuilder(config).Build())
            {
                await adminClient.DeleteTopicsAsync(new[] { topicName });
            }
        }

        public Task<List<int>> GetPartitionsAsync(string brokerUrls, string topic)
        {
            return Task.Run(() => {
                var config = new Confluent.Kafka.AdminClientConfig { BootstrapServers = brokerUrls };
                using (var adminClient = new Confluent.Kafka.AdminClientBuilder(config).Build())
                {
                    var meta = adminClient.GetMetadata(topic, TimeSpan.FromSeconds(5));
                    var topicMeta = meta.Topics.FirstOrDefault(t => t.Topic == topic);
                    if (topicMeta == null) return new List<int>();
                    return topicMeta.Partitions.Select(p => p.PartitionId).ToList();
                }
            });
        }

        public Task<List<string>> GetLatestMessagesAsync(string brokerUrls, string topic, int partition, int count)
        {
            return Task.Run(() => {
                var config = new Confluent.Kafka.ConsumerConfig
                {
                    BootstrapServers = brokerUrls,
                    GroupId = $"KafkaTool-{DateTimeOffset.UtcNow.ToUnixTimeMilliseconds()}"
                };
                var messages = new List<string>();
                using (var consumer = new Confluent.Kafka.ConsumerBuilder<Ignore, string>(config).Build())
                {
                    var topicPartition = new Confluent.Kafka.TopicPartition(topic, new Confluent.Kafka.Partition(partition));
                    consumer.Assign(topicPartition);
                    consumer.Consume(TimeSpan.Zero); // Activate assignment
                    var endOffsets = consumer.QueryWatermarkOffsets(topicPartition, TimeSpan.FromSeconds(5));
                    if (endOffsets.High <= endOffsets.Low) // No messages
                        return messages;
                    long start = Math.Max(endOffsets.High - count, endOffsets.Low);
                    consumer.Seek(new Confluent.Kafka.TopicPartitionOffset(topicPartition, start));
                    int fetched = 0;
                    while (fetched < count)
                    {
                        var cr = consumer.Consume(TimeSpan.FromMilliseconds(1000));
                        if (cr == null) break;
                        if (cr.Offset < start) continue;
                        if (cr.Offset >= endOffsets.High) break;
                        var key = ""; // Key is Ignore type
                        var value = cr.Message.Value ?? string.Empty;
                        messages.Add($"Offset: {cr.Offset}\nKey: {key}\nValue: {value}");
                        fetched++;
                        if (cr.Offset == endOffsets.High - 1) break;
                    }
                }
                return messages;
            });
        }

        public Task<List<string>> GetMessagesBetweenOffsetsAsync(string brokerUrls, string topic, int partition, long fromOffset, long toOffset)
        {
            return Task.Run(() => {
                var config = new Confluent.Kafka.ConsumerConfig
                {
                    BootstrapServers = brokerUrls,
                    GroupId = $"KafkaTool-{DateTimeOffset.UtcNow.ToUnixTimeMilliseconds()}"
                };
                var messages = new List<string>();
                using (var consumer = new Confluent.Kafka.ConsumerBuilder<Ignore, string>(config).Build())
                {
                    var topicPartition = new Confluent.Kafka.TopicPartition(topic, new Confluent.Kafka.Partition(partition));
                    consumer.Assign(topicPartition);
                    consumer.Consume(TimeSpan.Zero); // Activate assignment
                    var endOffsets = consumer.QueryWatermarkOffsets(topicPartition, TimeSpan.FromSeconds(5));
                    if (endOffsets.High <= endOffsets.Low || fromOffset > toOffset || fromOffset >= endOffsets.High)
                        return messages;
                    long start = Math.Max(fromOffset, endOffsets.Low);
                    long end = Math.Min(toOffset, endOffsets.High - 1);
                    consumer.Seek(new Confluent.Kafka.TopicPartitionOffset(topicPartition, start));
                    while (true)
                    {
                        var cr = consumer.Consume(TimeSpan.FromMilliseconds(1000));
                        if (cr == null) break;
                        if (cr.Offset < start) continue;
                        if (cr.Offset > end) break;
                        var key = ""; // Key is Ignore type
                        var value = cr.Message.Value ?? string.Empty;
                        messages.Add($"Offset: {cr.Offset}\nKey: {key}\nValue: {value}");
                        if (cr.Offset == end) break;
                    }
                }
                return messages;
            });
        }

        public Task<(long Low, long High)> GetPartitionDepthAsync(string brokerUrls, string topic, int partition)
        {
            return Task.Run(() => {
                var config = new Confluent.Kafka.ConsumerConfig
                {
                    BootstrapServers = brokerUrls,
                    GroupId = $"KafkaTool-Depth-{Guid.NewGuid()}"
                };
                using (var consumer = new Confluent.Kafka.ConsumerBuilder<Ignore, string>(config).Build())
                {
                    var tpo = new Confluent.Kafka.TopicPartition(topic, new Confluent.Kafka.Partition(partition));
                    var offsets = consumer.QueryWatermarkOffsets(tpo, TimeSpan.FromSeconds(5));
                    return (offsets.Low.Value, offsets.High.Value);
                }
            });
        }

        // Add method to get Kafka version
        public Task<string> GetKafkaVersionAsync(string brokerUrls)
        {
            var config = new AdminClientConfig { BootstrapServers = brokerUrls };
            using (var adminClient = new AdminClientBuilder(config).Build())
            {
                var meta = adminClient.GetMetadata(TimeSpan.FromSeconds(3));
                var broker = meta.Brokers.FirstOrDefault();
                if (broker != null)
                {
                    try
                    {
                        var handle = adminClient.Handle;
                        var apiVersion = handle.GetType().GetProperty("ApiVersion")?.GetValue(handle)?.ToString();
                        if (!string.IsNullOrEmpty(apiVersion))
                            return Task.FromResult(apiVersion);
                    }
                    catch { }
                    return Task.FromResult($"Broker {broker.BrokerId} {broker.Host}:{broker.Port}");
                }
                return Task.FromResult("Unknown");
            }
        }

        public async Task ProduceMessageAsync(string brokerUrls, string topic, string key, string value, List<(string, string)> headers, int partition)
        {
            var config = new Confluent.Kafka.ProducerConfig { BootstrapServers = brokerUrls };
            using (var producer = new Confluent.Kafka.ProducerBuilder<string, string>(config).Build())
            {
                var msg = new Confluent.Kafka.Message<string, string>
                {
                    Key = key,
                    Value = value,
                    Headers = new Confluent.Kafka.Headers()
                };
                if (headers != null)
                {
                    foreach (var (hKey, hVal) in headers)
                    {
                        msg.Headers.Add(hKey, Encoding.UTF8.GetBytes(hVal));
                    }
                }
                var topicPartition = new Confluent.Kafka.TopicPartition(topic, new Confluent.Kafka.Partition(partition));
                await producer.ProduceAsync(topicPartition, msg);
            }
        }
    }
}
