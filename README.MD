# Gradle Dev Containers Plugin

## Description

This Gradle plugin automatically spins up Docker containers for Zookeeper and Kafka. It is perfect for local
development, testing, and debugging purposes.

## Features

- Automatic startup of Zookeeper and Kafka containers.
- Awaits full initialization of Zookeeper before launching Kafka.
- Dynamically detects the Docker Engine URI based on the operating system.

## Installation

1. Add the plugin code to your Gradle project.
2. Apply the plugin in your `build.gradle` file:

```groovy
apply plugin: 'o.sur.gradledevcontainers'
```

## Usage

To start the containers, execute the following command:

```bash
./gradlew RUN_KAFKA
```

## Dependencies

- Docker must be installed and running on your machine.

## Configuration

By default, the plugin will use `tcp` to connect to the Docker Engine on Windows and `unix` for other operating systems.

---
