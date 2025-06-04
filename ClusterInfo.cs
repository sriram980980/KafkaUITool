namespace KafkaTool
{
    public class ClusterInfo
    {
        public string Name { get; set; } = string.Empty;
        public string BrokerUrls { get; set; } = string.Empty;
        public string Status { get; set; } = "Disconnected"; // Possible values: Connected, Failed, Disconnected
        public override string ToString()
        {
            return $"{Name} ({BrokerUrls})";
        }
    }
}
