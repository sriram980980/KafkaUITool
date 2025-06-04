using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Confluent.Kafka;
using Confluent.Kafka.Admin;

namespace KafkaTool
{
    public class KafkaService
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
                    GroupId = $"KafkaTool-Preview-{Guid.NewGuid()}"
                };
                var messages = new List<string>();
                using (var consumer = new Confluent.Kafka.ConsumerBuilder<Ignore, string>(config).Build())
                {
                    consumer.Assign(new Confluent.Kafka.TopicPartition(topic, new Confluent.Kafka.Partition(partition)));
                    consumer.Consume(TimeSpan.Zero); // Workaround: activate assignment
                    var endOffsets = consumer.QueryWatermarkOffsets(new Confluent.Kafka.TopicPartition(topic, new Confluent.Kafka.Partition(partition)), TimeSpan.FromSeconds(5));
                    long start = Math.Max(endOffsets.High - count, endOffsets.Low);
                    consumer.Seek(new Confluent.Kafka.TopicPartitionOffset(topic, new Confluent.Kafka.Partition(partition), start));
                    int fetched = 0;
                    while (fetched < count)
                    {
                        var cr = consumer.Consume(TimeSpan.FromMilliseconds(500));
                        if (cr == null) break;
                        messages.Add($"Offset: {cr.Offset}\nKey: {cr.Message.Key}\nValue: {cr.Message.Value}");
                        fetched++;
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
                    GroupId = $"KafkaTool-Range-{Guid.NewGuid()}"
                };
                var messages = new List<string>();
                using (var consumer = new Confluent.Kafka.ConsumerBuilder<Ignore, string>(config).Build())
                {
                    consumer.Assign(new Confluent.Kafka.TopicPartition(topic, new Confluent.Kafka.Partition(partition)));
                    consumer.Consume(TimeSpan.Zero); // Workaround: activate assignment
                    consumer.Seek(new Confluent.Kafka.TopicPartitionOffset(topic, new Confluent.Kafka.Partition(partition), fromOffset));
                    while (true)
                    {
                        var cr = consumer.Consume(TimeSpan.FromMilliseconds(500));
                        if (cr == null || cr.Offset > toOffset) break;
                        messages.Add($"Offset: {cr.Offset}\nKey: {cr.Message.Key}\nValue: {cr.Message.Value}");
                        if (cr.Offset == toOffset) break;
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
    }
}
