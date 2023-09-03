package o.sur.gradledevcontainers

import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.command.LogContainerCmd
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.core.command.LogContainerResultCallback
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.concurrent.CountDownLatch


class GradleDevContainersPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("RUN_KAFKA") { task ->
            task.group = "DevContainersPlugin"
            task.doLast {
                KafkaComposeRunner.runDevContainers()
            }
        }
    }
}

object KafkaComposeRunner {

    private fun waitForZookeeper(containerId: String) {
        val latch = CountDownLatch(1)
        val logCmd: LogContainerCmd = PluginDockerClient.dockerClient
            .logContainerCmd(containerId)
            .withStdErr(true)
            .withStdOut(true)
            .withFollowStream(true)
            .withSince(0)

        logCmd.exec(object : LogContainerResultCallback() {
            override fun onNext(item: Frame?) {
                val logLine = String(item!!.payload)
                if (logLine.contains("binding to port 0.0.0.0/0.0.0.0:2181")) {
                    latch.countDown()
                }
            }
        })
        latch.await()
    }

    private fun runZookeeper(): String? {
        val container = createZookeeperContainer()
        PluginDockerClient.dockerClient.startContainerCmd(container.id).exec()
        return container.id
    }

    private fun runKafka() {
        val container = createKafkaContainer()
        PluginDockerClient.dockerClient.startContainerCmd(container.id).exec()
        println("Kafka container started with ID: " + container.id)
    }

    private fun createKafkaContainer(): CreateContainerResponse {
        return PluginDockerClient.dockerClient.createContainerCmd("confluentinc/cp-kafka:7.3.2")
            .withName("kafka-broker")
            .withHostConfig(
                HostConfig.newHostConfig()
                    .withNetworkMode(PluginDockerClient.networkId)
                    .withPortBindings(PortBinding.parse("9092:9092"))
            ).withEnv(
                mutableListOf(
                    "KAFKA_BROKER_ID=1",
                    "KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181",
                    "KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,PLAINTEXT_INTERNAL:PLAINTEXT",
                    "KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092,PLAINTEXT_INTERNAL://broker:29092",
                    "KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1",
                    "KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=1",
                    "KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1"
                )
            ).exec()
    }

    private fun createZookeeperContainer(): CreateContainerResponse =
        PluginDockerClient.dockerClient.createContainerCmd("confluentinc/cp-zookeeper:7.3.2")
            .withName("zookeeper")
            .withHostConfig(
                HostConfig.newHostConfig()
                    .withNetworkMode(PluginDockerClient.networkId)
                    .withPortBindings(PortBinding.parse("2181:2181"))
            )
            .withEnv(
                mutableListOf(
                    "ZOOKEEPER_CLIENT_PORT=2181",
                    "ZOOKEEPER_TICK_TIME=2000"
                )
            ).exec()

    fun runDevContainers() {
        runZookeeper().also { zookeeperId ->
            waitForZookeeper(zookeeperId!!)
            runKafka()
        }
    }
}
