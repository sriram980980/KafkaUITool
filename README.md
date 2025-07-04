# KafkaTool  --built by COPILOT 

A Windows Forms application for managing and interacting with Apache Kafka clusters.

![KafkaTool UI Screenshot](./screenshot.png)

## Requirements (Detailed)
- **.NET 9.0 SDK**: Install the latest .NET 9.0 SDK from the official Microsoft website.
- **Windows OS**: Application is designed for Windows 10/11 (x64) with Windows Forms support.
- **Open Source Only**: All dependencies must be open source and non-proprietary.
- **Kafka Integration**: Uses [Confluent.Kafka](https://github.com/confluentinc/confluent-kafka-dotnet) for Kafka client operations.
- **JSON Handling**: Uses [Newtonsoft.Json](https://www.newtonsoft.com/json) for parsing and formatting JSON.
- **Visual Studio or VS Code**: Recommended for development and debugging.

## Features & Progress
| Step | Feature Description                                                                 | Status      |
|------|-------------------------------------------------------------------------------------|-------------|
| 1    | Create .NET 9.0 Project structure                                                   | ✅ Complete |
| 2    | File menu: Add clusters (accept name and broker URLs)                               | ✅ Complete |
| 3    | Display topics/partitions, add new, change config (retention, etc.)                 | ✅ Complete |
| 4    | Display producers, onboard new                                                      | ⬜ Pending  |
| 5    | Display consumers, show/set offsets                                                 | ⬜ Pending  |
| 6    | Show messages in partition, display as ASCII/JSON/JSONPath                          | ⬜ Pending  |
| 7    | Search messages by offset/ header value search (slow   operation)                   | ⬜ Pending  |
| 8    | Search for message body  with partition + offset or time range --slowest operation  | ⬜ Pending  |

## Work Progress (as of 2025-06-05)
- UI for topic/partition message viewing now uses a resizable SplitContainer: users can resize the message table and message preview area horizontally.
- Message preview area always matches the table height and updates live as you select a row.
- Fixed SplitContainer splitter errors for robust resizing and layout.
- Shift+Enter now inserts a new line in all multiline textboxes (topic properties, headers, value, etc.), while Enter alone submits the form.
- All previous features (cluster management, topic CRUD, logging, etc.) remain stable.
- See screenshot above for the latest UI.

## Subtasks & Clear Requirements
### 1. Project Setup
- [x] Initialize .NET 9.0 Windows Forms project
- [x] Add open source dependencies (Confluent.Kafka, Newtonsoft.Json)
- [x] Add .gitignore and README

### 2. Cluster Management
- [x] Add File menu with "Add Cluster" option
- [x] Prompt user for cluster name and broker URLs
- [x] Store and display added clusters in the UI (with name and brokers)
- [x] Allow editing/removing clusters
- [x] Add option to save clusters (in-memory for now)
- [x] Show cluster connection status (color/icon) and allow double-click to connect
- [x] Log all user actions and errors to both console and a log file

### 3. Topic & Partition Management
- [x] List topics for each cluster
- [x] Show partitions for each topic
- [x] Add new topic
- [x] Change topic config (e.g., retention)
- [ ] Filter topic if lot of topics to display 

### 4. Producer Management
- [ ] List producers for each cluster
- [ ] Onboard (add) new producer
- [ ] Filter Producers if lot of # to display 

### 5. Consumer Management
- [ ] List consumers for each cluster
- [ ] Show consumer offsets
- [ ] Set/Reset consumer committed offsets

### 6. Message Viewing & Search
- [x] Show messages in a partition
- [x] Display message in ASCII, JSON, or JSONPath format
- [ ] Search messages by key or offset
- [ ] Search message body (full text)

## Build & Run Steps
1. **Clone the repository:**
   ```sh
   git clone https://github.com/sriram980980/KafkaUITool.git
   cd KafkaUITool
   ```
2. **Install .NET 9.0 SDK** (if not already installed):
   - Download from https://dotnet.microsoft.com/download/dotnet/9.0
3. **Restore dependencies:**
   ```sh
   dotnet restore
   ```
4. **Build the project:**
   ```sh
   dotnet build
   ```
5. **Run the application:**
   ```sh
   dotnet run --project KafkaTool/KafkaTool.csproj
   ```

## User Guide
### Adding a Kafka Cluster
- Go to the `File` menu and select `Add Cluster`.
- Enter a name for the cluster and one or more broker URLs (comma separated, e.g., `localhost:9092,localhost:9093`).
- Click OK. The cluster will be added and shown in the list.

### Editing or Removing a Cluster
- Right-click a cluster in the list to edit its name or broker URLs, or to remove it.
- Use the context menu's "Save Changes" to save the current in-memory list (future: persistent storage).

### Connecting to a Cluster
- Double-click a cluster to attempt a connection. The status and icon will update to show connecting, connected, or failed.
- While connecting, the cluster will show a yellow color and be temporarily unclickable.
- Connection attempts and results are logged to both the console and a `KafkaTool.log` file in the app directory.

### Next Steps
- After adding clusters, you will be able to view topics, partitions, producers, consumers, and messages as features are implemented.
- Use the menu options to manage Kafka resources and inspect messages.

### Troubleshooting
- Ensure your Kafka brokers are reachable from your machine.
- If you encounter errors, check the application output and logs for details (see `KafkaTool.log`).

---
For questions or contributions, please open an issue or pull request on GitHub.
