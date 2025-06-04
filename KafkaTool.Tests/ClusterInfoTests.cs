using System;
using System.Collections.Generic;
using Xunit;

namespace KafkaTool.Tests
{
    public class ClusterInfoTests
    {
        [Fact]
        public void ToString_ReturnsExpectedFormat()
        {
            var cluster = new KafkaTool.ClusterInfo { Name = "TestCluster", BrokerUrls = "localhost:9092" };
            Assert.Equal("TestCluster (localhost:9092)", cluster.ToString());
        }

        [Fact]
        public void ClusterList_AddsAndRemovesCorrectly()
        {
            var list = new List<KafkaTool.ClusterInfo>();
            var cluster1 = new KafkaTool.ClusterInfo { Name = "A", BrokerUrls = "a:1" };
            var cluster2 = new KafkaTool.ClusterInfo { Name = "B", BrokerUrls = "b:2" };
            list.Add(cluster1);
            list.Add(cluster2);
            Assert.Equal(2, list.Count);
            list.Remove(cluster1);
            Assert.Single(list);
            Assert.Equal(cluster2, list[0]);
        }
    }
}
