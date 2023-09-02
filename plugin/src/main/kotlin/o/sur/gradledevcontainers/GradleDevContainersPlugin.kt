package o.sur.gradledevcontainers

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.core.DockerClientBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project


class GradleDevContainersPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("RUN_COMPOSE") { task ->
            task.doLast {
                val dockerClient = DockerClientBuilder.getInstance().build()
                DockerComposeExample.runKafka(dockerClient)
                DockerComposeExample.runZookiper(dockerClient)
                println("Hello from plugin 'o.sur.gradledevcontainers.greeting'")
            }
        }
    }
}

object DockerComposeExample {
    fun runZookiper(dockerClient: DockerClient) {
        val container = dockerClient.createContainerCmd("confluentinc/cp-zookeeper:7.3.2")
            .withName("zookeeper")
            .withHostConfig(HostConfig.newHostConfig().withPortBindings(PortBinding.parse("2181:2181")))
            .withEnv(
                mutableListOf(
                    "ZOOKEEPER_CLIENT_PORT: 2181",
                    "ZOOKEEPER_TICK_TIME: 2000"
                )
            ).exec()
        dockerClient.startContainerCmd(container.id).exec()
    }


    fun runKafka(dockerClient: DockerClient) {
        val container = dockerClient.createContainerCmd("confluentinc/cp-kafka:7.3.2")
            .withName("kafka-broker")
            .withHostConfig(HostConfig.newHostConfig().withPortBindings(PortBinding.parse("9092:9092")))
            .withEnv(
                mutableListOf(
                    "KAFKA_BROKER_ID:1",
                    "KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:2181'",
                    "KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_INTERNAL:PLAINTEXT",
                    "KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092,PLAINTEXT_INTERNAL://broker:29092",
                    "KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1",
                    "KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1",
                    "KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1"
                )
            ).exec()
        dockerClient.startContainerCmd(container.id).exec()
    }
}