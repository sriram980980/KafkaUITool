# KafkaTool

A Windows Forms application for managing and interacting with Apache Kafka clusters.

## Requirements
- .NET 9.0 SDK
- Windows OS
- Open source dependencies only (no proprietary libraries)
- Uses [Confluent.Kafka](https://github.com/confluentinc/confluent-kafka-dotnet) for Kafka integration
- Uses [Newtonsoft.Json](https://www.newtonsoft.com/json) for JSON handling

## Features & Status
| Step | Feature Description                                                                 | Status      |
|------|-------------------------------------------------------------------------------------|-------------|
| 1    | Create .NET 9.0 Project structure                                                   | ✅ Complete |
| 2    | File menu: Add clusters (accept broker URLs)                                        | ⬜ Pending  |
| 3    | Display topics/partitions, add new, change config (retention, etc.)                 | ⬜ Pending  |
| 4    | Display producers, onboard new                                                      | ⬜ Pending  |
| 5    | Display consumers, show/set offsets                                                  | ⬜ Pending  |
| 6    | Show messages in partition, display as ASCII/JSON/JSONPath                          | ⬜ Pending  |
| 7    | Search messages by key/offset                                                        | ⬜ Pending  |
| 8    | Search message body                                                                 | ⬜ Pending  |

## How to Contribute
- Fork the repository
- Create a feature branch
- Submit a pull request with a clear description

## License
This project is open source and uses only non-proprietary dependencies.
