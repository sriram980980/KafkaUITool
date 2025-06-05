namespace KafkaTool
{
    public class ClusterInfo
    {
        public string Name { get; set; } = string.Empty;
        public string BrokerUrls { get; set; } = string.Empty;
        public string Status { get; set; } = string.Empty;
        public bool ConnectByDefault { get; set; } = false;
        public string KafkaVersion { get; set; } = string.Empty;
        public override string ToString()
        {
            return $"{Name} ({BrokerUrls})";
        }
    }
}
