#!/bin/bash
# Demo script to test the REST API server

echo "=========================================="
echo "Kafka UI Tool - REST API Server Demo"
echo "=========================================="

cd /home/runner/work/KafkaUITool/KafkaUITool/kafka-ui-java

echo "Starting REST API server on port 8082"
java -cp target/kafka-ui-tool-2.0.0-jar-with-dependencies.jar com.kafkatool.RestApiMain --port 8082 &
SERVER_PID=$!

# Wait for server to start
sleep 3

echo "Testing health endpoint"
curl -s http://localhost:8082/api/health | json_pp || echo "JSON formatting not available, raw output:"
curl -s http://localhost:8082/api/health

echo -e "\n\nTesting info endpoint"
curl -s http://localhost:8082/api/info | json_pp || echo "JSON formatting not available, raw output:"
curl -s http://localhost:8082/api/info

echo -e "\n\nStopping server"
kill $SERVER_PID

echo "Demo completed!"