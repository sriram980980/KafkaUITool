namespace KafkaTool
{
    public interface IKafkaService
    {
        Task<bool> ConnectAsync(string brokerUrls);
        Task<Dictionary<string, string>> GetTopicConfigAsync(string brokerUrls, string topicName);
        Task AlterTopicConfigAsync(string brokerUrls, string topicName, Dictionary<string, string> newConfig);
        Task DeleteTopicAsync(string brokerUrls, string topicName);
        Task<List<int>> GetPartitionsAsync(string brokerUrls, string topic);
        Task<List<string>> GetLatestMessagesAsync(string brokerUrls, string topic, int partition, int count);
        Task<List<string>> GetMessagesBetweenOffsetsAsync(string brokerUrls, string topic, int partition, long fromOffset, long toOffset);
        Task<(long Low, long High)> GetPartitionDepthAsync(string brokerUrls, string topic, int partition);
        Task<string> GetKafkaVersionAsync(string brokerUrls);
        Task ProduceMessageAsync(string brokerUrls, string topic, string key, string value, List<(string, string)> headers, int partition);
    }
}
