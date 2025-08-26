# Log Producer with LogStash Pipeline

This project demonstrates a simple log producer that writes JSON logs to a file, which LogStash then reads and sends to Kafka.

## Project Structure

```
log_batcher/
├── build.gradle                 # Gradle build configuration
├── src/main/java/io/immutlog/producer/
│   └── LogProducerApp.java      # Main application that generates logs
├── src/main/resources/
│   └── logback.xml              # Logging configuration
├── logstash.conf                # LogStash configuration
├── logs.raw                     # Output log file (created by app)
└── README.md                    # This file
```

## Prerequisites

1. **Java 11+**
2. **Gradle**
3. **LogStash** (with Kafka output plugin)
4. **Kafka** (running on localhost:9092)

## Setup

### 1. Install LogStash Kafka Plugin

```bash
# Install LogStash Kafka output plugin
logstash-plugin install logstash-output-kafka
```

### 2. Build the Java Application

```bash
./gradlew build
```

## Usage

### 1. Start the Log Producer

```bash
# Run the Java application
./gradlew run
```

This will:
- Create a `logs.raw` file in the project root
- Write JSON log records every second
- Each log contains: record_id, timestamp, source, level, message, and metadata

### 2. Start LogStash

```bash
# Run LogStash with the configuration
logstash -f logstash.conf
```

This will:
- Read from `logs.raw` file
- Parse JSON logs
- Send them to Kafka topic `logs.raw`
- Also output to console for debugging

## Sample Log Output

The application generates logs like this:

```json
{
  "record_id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2024-01-15T10:30:45.123Z",
  "source": "example-app",
  "level": "INFO",
  "message": "Sample log message at 2024-01-15T10:30:45.123Z",
  "metadata": {
    "user_id": "user123",
    "session_id": "550e8400-e29b-41d4-a716-446655440001",
    "request_id": "550e8400-e29b-41d4-a716-446655440002"
  }
}
```

## Configuration

### LogStash Configuration

The `logstash.conf` file configures:

- **Input**: Reads from `logs.raw` file with JSON codec
- **Filter**: Adds processing metadata and timestamps
- **Output**: Sends to Kafka topic `logs.raw` and console

### Customization

You can modify:
- Log generation frequency in `LogProducerApp.java`
- Log content structure in the `generateLogRecord()` method
- Kafka configuration in `logstash.conf`
- Log file path in `LogProducerApp.java`

## Troubleshooting

1. **LogStash can't connect to Kafka**: Ensure Kafka is running on localhost:9092
2. **Permission denied**: Check file permissions for `logs.raw`
3. **Plugin not found**: Install the Kafka output plugin for LogStash

## Stopping

- Press `Ctrl+C` to stop the Java application
- Press `Ctrl+C` to stop LogStash
