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
    }
}
