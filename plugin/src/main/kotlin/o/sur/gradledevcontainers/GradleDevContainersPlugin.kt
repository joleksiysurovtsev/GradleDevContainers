package o.sur.gradledevcontainers

import com.github.dockerjava.api.command.LogContainerCmd
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.core.command.LogContainerResultCallback
import java.util.concurrent.CountDownLatch
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.core.DockerClientBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project


class GradleDevContainersPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("RUN_COMPOSE") { task ->
            task.doLast {
                DockerComposeExample.runDevContainers()
                println("Hello from plugin 'o.sur.gradledevcontainers.greeting'")
            }
            task.group = "DevContainersPlugin"
        }
    }
}

object DockerComposeExample {
    private val dockerClient = DockerClientBuilder.getInstance("tcp://localhost:2375").build()

    private fun waitForZookeeper(containerId: String) {
        val latch = CountDownLatch(1)
        val logCmd: LogContainerCmd =
            dockerClient.logContainerCmd(containerId).withStdErr(true).withStdOut(true).withFollowStream(true)
                .withSince(0)

        logCmd.exec(object : LogContainerResultCallback() {
            override fun onNext(item: Frame?) {
                val logLine = String(item!!.payload)
                if (logLine.contains("binding to port 0.0.0.0/0.0.0.0:2181")) {
                    println("!!!!!!!!!!!!!!!")
                    latch.countDown()

                }
            }
        })
        latch.await()
    }

    private fun runZookiper(): String? {
        val container = dockerClient.createContainerCmd("confluentinc/cp-zookeeper:7.3.2")
            .withName("zookeeper")
            .withHostConfig(
                HostConfig.newHostConfig().withPortBindings(PortBinding.parse("2181:2181")))
            .withEnv(
                mutableListOf(
                    "ZOOKEEPER_CLIENT_PORT=2181",
                    "ZOOKEEPER_TICK_TIME=2000"
                )
            ).exec()
        dockerClient.startContainerCmd(container.id).exec()
        return container.id
    }


    private fun runKafka() {
        val container = dockerClient.createContainerCmd("confluentinc/cp-kafka:7.3.2").withName("kafka-broker")
            .withHostConfig(HostConfig.newHostConfig().withPortBindings(PortBinding.parse("9092:9092"))).withEnv(
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
        dockerClient.startContainerCmd(container.id).exec()
        println("Kafka container started with ID: " + container.id)
    }

    fun runDevContainers() {
        runZookiper().also { zookiperId ->
            waitForZookeeper(zookiperId!!)
            runKafka()
        }
    }
}
